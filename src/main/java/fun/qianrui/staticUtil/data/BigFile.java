package fun.qianrui.staticUtil.data;

import fun.qianrui.staticUtil.file.Logger;
import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author 钱睿
 */
public class BigFile {
    public final String file;
    private final RandomAccessFile rw;
    private final FileChannel channel;
    private final Logger logger;
    private final HashMap<String, long[]> cache = new HashMap<>();
    private volatile long length;
    private long lastModified = -1;
    private boolean out = false;

    public BigFile(String file, long posSize) {
        if (out) {
            System.out.println("BigFile create: " + file);
        }
        this.file = file;
        this.logger = new Logger(this.file + "\\index");
        logStream().forEach(h -> {
            lastModified = h.lastModified;
            if (h.isRemove()) {
                //删除
                this.cache.remove(h.name);
            } else {
                //覆盖
                this.cache.put(h.name, new long[]{h.pos, h.size});
                //最右端更新
                if (this.length < h.pos + h.size) {
                    this.length = h.pos + h.size;
                }
            }
        });
        rw = ExceptionUtil.throwT(() -> new RandomAccessFile(this.file + "\\data", "rw"));
        if (posSize > length) {
            setLength(posSize);
        }
        channel = rw.getChannel();
    }

    public Stream<ChangeHistory> logStream() {
        return logger.logStream()
                .map(ChangeHistory::new);
    }

    public synchronized boolean put(String name, byte[] data) {
        final long[] longs = cache.get(name);
        //文件已存在 长度相同 原先的数据相同
        if (longs != null && longs[1] == data.length && Arrays.equals(data, get(name))) {
            //数据没有变动
            return false;
        }
        //文件如果是变小了，那就复用原来的空间
        long pos = longs != null && longs[1] >= data.length ? longs[0] : length;
        //放入 mMap
        getMap(FileChannel.MapMode.READ_WRITE, pos, data.length).put(data);
        //记录日志
        logger(name, pos, data.length);
        //更新缓存
        cache.put(name, new long[]{pos, data.length});
        //如果是追加的，更新右端坐标
        if (length == pos) {
            length += data.length;
        }
        return true;
    }

    private void logger(String name, long pos, int length) {
        lastModified = System.currentTimeMillis();
        logger.put(name + " " + pos + " " + length +
                " " + lastModified + "l");
    }

    public synchronized byte[] get(String name) {
        final long[] longs = cache.get(name);
        //缓存不存在
        if (longs == null) {
            return null;
        }
        //从 mMap 取实际内容
        MappedByteBuffer map = getMap(FileChannel.MapMode.READ_ONLY, longs[0], longs[1]);
        final byte[] bytes = new byte[(int) longs[1]];
        //load不执行 真会什么都拿不到
        map.load();
        map.get(bytes);
        return bytes;
    }

    public synchronized boolean exist(String name) {
        return cache.get(name) != null;
    }

    public synchronized boolean remove(String name) {
        byte[] old = get(name);
        if (old != null) {
            cache.remove(name);
            //日志标识后删除
            logger(name, -1, 0);
            return true;
        }
        return false;
    }

    private static final int MAX_READ_SIZE = 1_500_000_000;

    public synchronized HashMap<String, byte[]> getAll() {
        final long begin = out ? System.currentTimeMillis() : -1;
        final List<byte[]> allData = getAllData();
        final HashMap<String, byte[]> result = new HashMap<>(cache.size(), 1);
        cache.forEach((key, longs) -> {
            long start = longs[0];
            int needSize = (int) longs[1];
            int destPos = 0;
            final byte[] bytes = new byte[needSize];

            long left;
            long right = -1;
            for (byte[] data : allData) {
                left = right + 1;
                right += data.length;

                if (right < start) {
                    continue;
                }

                final int canGive = (int) (right - start + 1);
                if (canGive < needSize) {
                    System.arraycopy(data, (int) (start - left), bytes, destPos, canGive);
                    start += canGive;
                    destPos += canGive;
                    needSize -= canGive;
                } else {
                    System.arraycopy(data, (int) (start - left), bytes, destPos, needSize);
                    break;
                }
            }
            result.put(key, bytes);
        });
        if (out) {
            System.out.println(file + " getAll size:" + result.size() + " time:" + (System.currentTimeMillis() - begin));
        }
        return result;
    }

    public synchronized List<byte[]> getAllData() {
        final long begin = out ? System.currentTimeMillis() : -1;
        List<byte[]> result = new ArrayList<>();
        long[] l = new long[]{length, 0};
        while (l[1] < length) {
            int size = (int) Math.min(l[0] - l[1], MAX_READ_SIZE);
            MappedByteBuffer map = getMap(FileChannel.MapMode.READ_ONLY, l[1], size);
            map.load();
            final byte[] bytes = new byte[size];
            map.get(bytes);
            l[1] += size;
            result.add(bytes);
        }
        if (out) {
            System.out.println(file + " getAllData length:" + length + " time:" + (System.currentTimeMillis() - begin));
        }
        return result;
    }

    public synchronized void perfectLength() {
        setLength(length);
    }

    public synchronized int getSize() {
        return cache.size();
    }

    public synchronized long getLength() {
        return length;
    }

    public synchronized Set<String> getKeys() {
        return cache.keySet();
    }

    public long lastModified() {
        return lastModified;
    }

    public synchronized BigFile perfect() {
        HashMap<String, byte[]> all = getAll();
        long sum = all.values()
                .stream()
                .mapToLong(b -> b.length)
                .sum();
        BigFile bigFile = new BigFile(file + "perfectMap", sum);
        for (Map.Entry<String, byte[]> entry : all.entrySet()) {
            bigFile.put(entry.getKey(), entry.getValue());
        }
        return bigFile;
    }

    private synchronized void setLength(long length) {
        try {
            rw.setLength(length);
        } catch (Exception e) {
            ExceptionUtil.throwT(e);
        }
    }

    private MappedByteBuffer getMap(FileChannel.MapMode mode, long position, long size) {
        try {
            return channel.map(mode, position, size);
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        }
    }

    public boolean isOut() {
        return out;
    }

    public BigFile setOut(boolean out) {
        this.out = out;
        return this;
    }

    public static class ChangeHistory {
        public final String name;
        public final long pos;
        public final long size;
        public final long lastModified;

        private ChangeHistory(String l) {
            int end = l.length() - 1;
            //记录最后变动时间，兼容曾经版本logger是不记录时间的
            if (l.charAt(end) == 'l') {
                final int iLastModified = l.lastIndexOf(' ', end) - 1;
                lastModified = Long.parseLong(l.substring(iLastModified + 2, end));
                end = iLastModified;
            } else {
                lastModified = -1;
            }

            //长度
            final int iSize = l.lastIndexOf(' ', end) - 1;
            size = Long.parseLong(l.substring(iSize + 2, end + 1));
            end = iSize;

            //下标
            final int iPos = l.lastIndexOf(' ', end) - 1;
            pos = Long.parseLong(l.substring(iPos + 2, end + 1));
            end = iPos;

            //内容
            name = l.substring(0, iPos + 1);
        }

        public boolean isRemove() {
            return pos < 0;
        }

        @Override
        public String toString() {
            return "ChangeHistory{" +
                    "name='" + name + '\'' +
                    ", pos=" + pos +
                    ", size=" + size +
                    ", lastModified=" + lastModified +
                    '}';
        }
    }
}
