package fun.qianrui.staticUtil.data;

import fun.qianrui.staticUtil.file.Logger;
import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class BigFile {
    public final String file;
    private final RandomAccessFile rw;
    private final FileChannel channel;
    private final Logger logger;
    private final HashMap<String, long[]> cache = new HashMap<>();
    private volatile long length;

    public BigFile(String file, long posSize) {
        System.out.println("BigFile create:" + file);
        this.file = file;
        this.logger = new Logger(file + "map");
        logger.logStream()
                .forEach(l -> {
                    final int i1 = l.lastIndexOf(' ');
                    final int i2 = l.lastIndexOf(' ', i1 - 1);
                    final long pos = Long.parseLong(l.substring(i2 + 1, i1));
                    final long size = Long.parseLong(l.substring(i1 + 1));
                    final String name = l.substring(0, i2);
                    this.cache.put(name, new long[]{pos, size});
                    this.length = pos + size;
                });
        rw = ExceptionUtil.throwT(() -> new RandomAccessFile(file, "rw"));
        try {
            if (posSize > length) rw.setLength(posSize);
        } catch (Exception e) {
            ExceptionUtil.throwT(e);
        }
        channel = rw.getChannel();
    }

    public synchronized boolean put(String name, byte[] data) {
        if (exist(name)) return false;
        ExceptionUtil.throwT(() -> channel.map(FileChannel.MapMode.READ_WRITE, length, data.length)
                .put(data));
        logger.put(name + " " + length + " " + data.length);
        cache.put(name, new long[]{length, data.length});
        length += data.length;
        return true;
    }

    public synchronized byte[] get(String name) {
        final long[] longs = cache.get(name);
        if (longs == null) return null;
        MappedByteBuffer map = ExceptionUtil.throwT(() -> channel.map(FileChannel.MapMode.READ_ONLY, longs[0], longs[1]));
        final byte[] bytes = new byte[(int) longs[1]];
        map.load();
        map.get(bytes);
        return bytes;
    }

    public synchronized boolean exist(String name) {
        return cache.get(name) != null;
    }

    private static final int MAX_READ_SIZE = 1_500_000_000;

    public synchronized HashMap<String, byte[]> getAll() {
        final long begin = System.currentTimeMillis();
        final List<byte[]> allData = getAllData();
        final HashMap<String, byte[]> result = new HashMap<>();
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

                if (right < start) continue;

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
        System.out.println(file + " getAll result.size():" + result.size() + " time:" + (System.currentTimeMillis() - begin));
        return result;
    }

    public synchronized List<byte[]> getAllData() {
        final long begin = System.currentTimeMillis();
        List<byte[]> result = new ArrayList<>();
        long[] l = new long[]{length, 0};
        while (l[1] < length) {
            int size = (int) Math.min(l[0] - l[1], MAX_READ_SIZE);
            MappedByteBuffer map = ExceptionUtil.throwT(() -> channel.map(FileChannel.MapMode.READ_ONLY, l[1], size));
            map.load();
            final byte[] bytes = new byte[size];
            map.get(bytes);
            l[1] += size;
            result.add(bytes);
        }
        System.out.println(file + " getAllData length:" + length + " time:" + (System.currentTimeMillis() - begin));
        return result;
    }

    public synchronized void perfectLength() {
        try {
            rw.setLength(length);
        } catch (Exception e) {
            ExceptionUtil.throwT(e);
        }
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
}
