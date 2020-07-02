package fun.qianrui.staticUtil;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class FileUtil {
    public static String goodPath(String name) {
        return name.replaceAll("[\t\\\\/:*?\"<>| ]", "");
    }

    public static ArrayList<String> readAllLines(byte[] bytes) {
        return readAllLines(bytes, StandardCharsets.UTF_8);
    }

    public static ArrayList<String> readAllLines(Path path) {
        return readAllLines(path, StandardCharsets.UTF_8);
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

    public static ArrayList<String> readAllLines(Path path, Charset cs) {
        try {
            return (ArrayList<String>) Files.readAllLines(path, cs);
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        }
    }

    public static byte[] readAllBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        }
    }

    public static class Writer {
        private static class Task {
            Path path;
            byte[] bytes;
            Runnable success;

            public Task(Path path, byte[] bytes, Runnable success) {
                this.path = path;
                this.bytes = bytes;
                this.success = success;
            }

            public void write() {
                try {
                    File file = path.toFile();
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
            ThreadUtil.createLoopThread(() -> queue.take()
                    .write(), "write file")
                    .start();
        }

        public static void async(Path path, byte[] bytes, Runnable success) throws InterruptedException {
            queue.put(new Task(path, bytes, success));
        }
    }
}
