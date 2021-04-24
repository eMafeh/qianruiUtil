package fun.qianrui.staticUtil.sys;


import java.io.*;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author 88382571
 * 2019/5/8
 */
public class ExceptionUtil {
    public static Consumer<String> consumer = System.out::println;

    public static void isTrue(boolean b) {
        if (!b) {
            throw new RuntimeException();
        }
    }

    public static void isTrue(boolean b, Supplier<String> msg) {
        if (!b) {
            throw new RuntimeException(msg.get());
        }
    }

    @SuppressWarnings("all")
    public static <T extends Throwable, E> E throwT(Throwable t) throws T {
        throw (T) t;
    }

    public static <T> T throwT(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            return throwT(e);
        }
    }

    public static <T> T hidden(Callable<T> callable, T def) {
        try {
            return callable.call();
        } catch (Exception e) {
            return def;
        }
    }

    public static void print(Exception e) {
        if (consumer != null && e != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            consumer.accept(out.toString());
        }
    }

    public static StreamTokenizer in = new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));

    public static void main(String[] args) throws Exception {
        in.nextToken();
        int l = (int) in.nval;
        in.nextToken();
        int[] m = new int[2 * (int) in.nval];
        for (int i = 0; i < m.length; i++) {
            in.nextToken();
            m[i] = 2 * (int) in.nval + i % 2;
        }
        Arrays.sort(m);
        int result = 0, count = 0, end = -1;
        for (int i : m) {
            int r = i / 2;
            if (r > l) break;
            if (i % 2 == 0) {
                if (count == 0) result += r - end - 1;
                count++;
            } else {
                count--;
                end = r;
            }
        }
        System.out.print(count == 0 ? Math.max(result, result + l - end) : result);
    }
}
