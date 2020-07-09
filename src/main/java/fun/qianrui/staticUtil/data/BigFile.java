package fun.qianrui.staticUtil.data;

import fun.qianrui.staticUtil.file.Logger;
import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

public class BigFile {
    public final String file;
    private final RandomAccessFile rw;
    private final FileChannel channel;
    private final Logger logger;
    private final HashMap<String, long[]> cache = new HashMap<>();
    private volatile long length;
    private final LongAdder size = new LongAdder();

    public BigFile(String file, int possibleSize, long possibleOneLength) {
        System.out.println("BigFile create:" + file);
        long possibleLength = possibleSize * possibleOneLength;
        this.file = file;
        this.logger = new Logger(file + "log\\", possibleSize * 100);
        logger.logStream()
                .map(l -> l.split(" "))
                .forEach(l -> {
                    final long pos = Long.parseLong(l[1]);
                    final long size = Long.parseLong(l[2]);
                    this.cache.put(l[0], new long[]{pos, size});
                    this.length = pos + size;
                    this.size.increment();
                });
        rw = ExceptionUtil.throwT(() -> new RandomAccessFile(file, "rw"));
        try {
            if (length == 0) rw.setLength(possibleLength);
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
        this.size.increment();
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

    private static final int MAX_READ_SIZE = 500_000_000;

    public synchronized HashMap<String, byte[]> getAll() {
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
        return size.intValue();
    }

    public synchronized long getLength() {
        return length;
    }

    public synchronized Set<String> getKeys() {
        return cache.keySet();
    }
}
