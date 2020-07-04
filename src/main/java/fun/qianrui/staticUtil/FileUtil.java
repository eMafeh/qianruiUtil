package fun.qianrui.staticUtil;

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
            map.get(bytes);
            return bytes;
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        }
    }

    public static void main(String[] args) {
        final ArrayList<String> strings = readAllLines("F:\\Downloads\\m3u8\\video2.feimanzb.com.8091\\20180420\\AEG0TKO012\\650kb\\hls\\index.m3u8");
        for (String string : strings) {
            System.out.println(string);
        }
    }

    public static class Writer {
        private static class Task {
            String file;
            byte[] bytes;
            Runnable success;

            public Task(String file, byte[] bytes, Runnable success) {
                this.file = file;
                this.bytes = bytes;
                this.success = success;
            }

            public void write() {
                try {
                    File file = new File(this.file);
                    file.getParentFile()
                            .mkdirs();
                    RandomAccessFile rw = new RandomAccessFile(file, "rw");
                    FileChannel channel = rw.getChannel();
                    MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length);
                    map.put(bytes);
                    success.run();
                } catch (IOException e) {
                    ExceptionUtil.throwT(e);
                }
            }
        }

        private static final LinkedBlockingQueue<Task> queue
                = new LinkedBlockingQueue<>(Integer.parseInt(System.getProperty(
                "fun.qianrui.staticUtil.FileUtil.Writer.queue.capacity", "3000")));

        static {
            ThreadUtil.createLoopThread("write file", () -> queue.take()
                    .write())
                    .start();
        }

        public static void async(String file, byte[] bytes, Runnable success) throws InterruptedException {
            queue.put(new Task(file, bytes, success));
        }

        public static void now(String file, byte[] bytes) {
            new Task(file, bytes, () -> {
            }).write();
        }
    }
}
