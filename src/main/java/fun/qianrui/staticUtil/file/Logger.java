package fun.qianrui.staticUtil.file;


import fun.qianrui.staticUtil.data.ByteList;
import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Logger {
    private static final int SIZE = 1 << 27;
    private final int size;
    private final String rootStr;
    private MappedByteBuffer map;
    private int nowIndex;
    private long length;

    public Logger(String dir) {
        this(dir, SIZE);
    }

    public Logger(String dir, int size) {
        this.rootStr = dir.endsWith("\\") ? dir : dir + "\\";
        this.size = size;
        File root = new File(rootStr);
        if (!root.exists()) root.mkdirs();
        final String[] files = root.list();
        nowIndex = files != null ? Stream.of(files)
                .filter(f -> f.endsWith(".log"))
                .mapToInt(f -> Integer.parseInt(f.substring(0, f.length() - 4)))
                .max()
                .orElse(1) - 1 : 0;
        updateMmap();
    }

    public synchronized void put(String line) {
        line += "\n";
        byte[] bytes = line.getBytes();
        int l = bytes.length;
        if (l > size) throw new RuntimeException("bytes.length:" + l + " is large than max file size:" + size);
        if (l + this.length > size) updateMmap();
        for (byte b : bytes) {
            if (b == 0) throw new RuntimeException("log not support byte 0");
            map.put(b);
        }
        this.length += l;
    }

    public long logSize() {
        return (nowIndex - 1) * size + length;
    }

    public Stream<String> logStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new LogStream(),
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    private void updateMmap() {
        nowIndex++;
        final File now = new File(rootStr + nowIndex + ".log");
        map = ExceptionUtil.throwT(() -> new RandomAccessFile(now, "rw").getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, size));
        int i = 0;
        for (; i < size; i++) if (map.get() == 0) break;
        length = i;
        map.position(i);
    }

    private class LogStream implements Iterator<String> {
        private MappedByteBuffer map;
        private int _index;
        private int _length;

        public LogStream() {
            updateMmap();
        }

        private void updateMmap() {
            _index++;
            final File file = new File(rootStr + _index + ".log");
            map = !file.exists() ? null :
                    ExceptionUtil.throwT(() -> new RandomAccessFile(file, "r").getChannel()
                            .map(FileChannel.MapMode.READ_ONLY, 0, size));
            if (map != null) {
                int i = 0;
                for (; i < size; i++) if (map.get() == 0) break;
                _length = i;
                map.rewind();
            }
        }

        @Override
        public boolean hasNext() {
            return map != null && _length > map.position();
        }

        @Override
        public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            final ByteList byteList = new ByteList();
            while (true) {
                if (map.position() == _length) break;
                final byte b = map.get();
                if (b == '\n') break;
                byteList.add(b);
            }
            if (map.position() == _length) {
                updateMmap();
            }
            return new String(byteList.toArray());
        }
    }
}
