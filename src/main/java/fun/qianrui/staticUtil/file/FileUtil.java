package fun.qianrui.staticUtil.file;

import fun.qianrui.staticUtil.sys.ExceptionUtil;
import fun.qianrui.staticUtil.sys.ThreadUtil;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class FileUtil {
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

    public static void write(String file, byte[] bytes, Runnable success) {
        final MappedByteBuffer write = write(file, bytes.length);
        write.put(bytes);
        success.run();
    }

    public static MappedByteBuffer write(String file, long length) {
        try {
            File file0 = new File(file);
            file0.getParentFile()
                    .mkdirs();
            RandomAccessFile rw = new RandomAccessFile(file0, "rw");
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
                write(take.file, take.bytes, take.success);
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
}
