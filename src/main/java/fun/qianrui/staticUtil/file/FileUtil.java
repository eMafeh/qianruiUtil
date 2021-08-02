package fun.qianrui.staticUtil.file;

import fun.qianrui.staticUtil.sys.ExceptionUtil;
import fun.qianrui.staticUtil.sys.ThreadUtil;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class FileUtil {

    /**
     * @param file dir or singleFile
     * @return <parentDir,subFile>
     */
    public static Map<File, List<File>> getAllFiles(File file) {
        final long begin = System.currentTimeMillis();
        Map<File, List<File>> map = new HashMap<>();
        // check only once
        if (file != null && file.exists()) {
            if (file.isFile()) {
                map.put(null, Collections.singletonList(file));
            } else {
                getAllFiles(file, map);
            }
        }
        System.out.println(file + " dir :" + map.size() + " file:" + map.values()
                .stream()
                .mapToInt(List::size)
                .sum() + " time:" + (System.currentTimeMillis() - begin));
        return map;
    }

    private static void getAllFiles(File file, Map<File, List<File>> map) {
        File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return;
        }
        List<File> subs = new ArrayList<>(listFiles.length);
        for (File sub : listFiles) {
            if (sub.isFile()) {
                subs.add(sub);
            } else {
                getAllFiles(sub, map);
            }
        }
        if (subs.size() > 0) {
            map.put(file, subs);
        }
    }

    public static String goodPath(String name) {
        return name.replaceAll("[\t\\\\/:*?\"<>| ]", "");
    }

    public static ArrayList<String> readAllLines(byte[] bytes) {
        return readAllLines(bytes, StandardCharsets.UTF_8);
    }

    public static ArrayList<String> readAllLines(byte[] bytes, Charset cs) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream, cs);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            ArrayList<String> result = new ArrayList<>();
            for (; ; ) {
                String line = reader.readLine();
                if (line == null)
                    break;
                result.add(line);
            }
            return result;
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        }
    }

    public static ArrayList<String> readAllLines(File f) {
        final byte[] bytes = readAllBytes(f);
        return readAllLines(bytes);
    }

    public static ArrayList<String> readAllLines(String file) {
        final byte[] bytes = readAllBytes(file);
        return readAllLines(bytes);
    }

    public static byte[] readAllBytes(String file) {
        return readAllBytes(new File(file));
    }

    public static byte[] readAllBytes(File f) {
        byte[] bytes = new byte[(int) f.length()];
        try {
            RandomAccessFile rw = new RandomAccessFile(f, "r");
            FileChannel channel = rw.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, bytes.length);
            map.load();
            map.get(bytes);
            return bytes;
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        }
    }

    public static void mmap(String file, byte[] bytes, Runnable success) {
        final MappedByteBuffer write = mmap(file, bytes.length);
        write.put(bytes);
        success.run();
    }

    public static MappedByteBuffer mmap(String file, long length) {
        return mmap(new File(file), length);
    }

    public static MappedByteBuffer mmap(File file, long length) {
        try {
            file.getParentFile()
                    .mkdirs();
            RandomAccessFile rw = new RandomAccessFile(file, "rw");
            FileChannel channel = rw.getChannel();
            return channel.map(FileChannel.MapMode.READ_WRITE, 0, length);
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        }

    }

    public static class Writer {
        private static final LinkedBlockingQueue<Task> queue
                = new LinkedBlockingQueue<>(Integer.parseInt(System.getProperty(
                "fun.qianrui.staticUtil.file.FileUtil.Writer.queue.capacity", "3000")));

        static {
            ThreadUtil.createLoopThread("write file", () -> {
                final Task take = queue.take();
                mmap(take.file, take.bytes, take.success);
            })
                    .start();
        }

        public static void async(String file, byte[] bytes, Runnable success) throws InterruptedException {
            queue.put(new Task(file, bytes, success));
        }

        private static class Task {
            String file;
            byte[] bytes;
            Runnable success;

            public Task(String file, byte[] bytes, Runnable success) {
                this.file = file;
                this.bytes = bytes;
                this.success = success;
            }
        }
    }

    private static final int FILE_RECORD = 1 << 10;//1K
    private static final int[] GOOD_BLOCK_LIST = {
            /*1K*/ FILE_RECORD,/*FILE_RECORD*/
            /*4K*/ (1 << 2) * FILE_RECORD,/*BLOCK*/
            /*32K*/  (1 << 5) * FILE_RECORD,
            /*256K*/  (1 << 8) * FILE_RECORD,
            /*1M*/    (1 << 10) * FILE_RECORD,
            /*4M*/    (1 << 12) * FILE_RECORD,
            /*8M*/    (1 << 13) * FILE_RECORD,
            /*16M*/    (1 << 14) * FILE_RECORD,
            /*32M*/   (1 << 15) * FILE_RECORD,
            /*64M*/   (1 << 16) * FILE_RECORD,
            /*256M*/   (1 << 18) * FILE_RECORD
    };

    public static int goodBlockSize(int now) {
        for (int i : GOOD_BLOCK_LIST) {
            if (i > now) return i;
        }
        return now;
    }
}
