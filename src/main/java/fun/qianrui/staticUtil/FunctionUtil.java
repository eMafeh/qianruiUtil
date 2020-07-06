package fun.qianrui.staticUtil;

public class FunctionUtil {
    public static <T> T self(T t) {
        return t;
    }

    public static final Runnable EMPTY = () -> {
    };
}
