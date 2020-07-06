package fun.qianrui.staticUtil;


import fun.qianrui.staticUtil.data.ByteList;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Logger {
    private static final int size = 1 << 27;
    private final String rootStr;
    private MappedByteBuffer map;
    private int nowIndex;
    private long length;

    public Logger(String dir) {
        rootStr = dir.endsWith("\\") ? dir : dir + "\\";
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
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new LogStream(rootStr),
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

    private static class LogStream implements Iterator<String> {
        private final String rootStr;
        private MappedByteBuffer map;
        private int index = 0;
        private int length;

        public LogStream(String rootStr) {
            this.rootStr = rootStr;
            updateMmap();
        }

        private void updateMmap() {
            index++;
            final File file = new File(rootStr + index + ".log");
            map = !file.exists() ? null :
                    ExceptionUtil.throwT(() -> new RandomAccessFile(file, "r").getChannel()
                            .map(FileChannel.MapMode.READ_ONLY, 0, size));
            if (map != null) {
                int i = 0;
                for (; i < size; i++) if (map.get() == 0) break;
                length = i;
                map.rewind();
            }
        }

        @Override
        public boolean hasNext() {
            return map != null && length > map.position();
        }

        @Override
        public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            final ByteList byteList = new ByteList();
            while (true) {
                if (map.position() == length) {
                    updateMmap();
                    break;
                }
                final byte b = map.get();
                if (b == '\n') break;
                byteList.add(b);
            }
            return new String(byteList.toArray());
        }
    }

    public static void main(String[] args) {
        final Logger logger = new Logger("F:\\Downloads\\log\\ts");
        logger.put("你好啊");
        logger.put("");
        logger.put("啊哈哈 嘿嘿");
        logger.put("这 课 还 行 哎呦");
        logger.put("");
        logger.put("你好啊\n啊哈哈 嘿嘿");
        logger.logStream()
                .forEach(System.out::println);
    }
}
