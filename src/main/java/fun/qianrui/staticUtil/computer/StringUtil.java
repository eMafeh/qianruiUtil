package fun.qianrui.staticUtil.computer;

import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.util.List;
import java.util.function.Function;

public class StringUtil {
    /**
     * @param strings 一系列字符串
     * @return 共同的前缀
     */
    public static String samePre(List<String> strings) {
        ExceptionUtil.isTrue(strings.size() != 0);
        final String first = strings.get(0);
        if (strings.size() == 1) {
            return first;
        }
        final StringBuilder result = new StringBuilder();
        int index = 0;
        while (first.length() > index) {
            char c = first.charAt(index);
            for (int i = 1; i < strings.size(); i++) {
                String string = strings.get(i);
                if (string.length() == index || string.charAt(index) != c) return result.toString();
            }
            index++;
            result.append(c);
        }
        return first;
    }

    /**
     * @param target 源字符串 如 欢迎您{a}{b}同学
     * @param prefix 前缀 {
     * @param suffix 后缀 }
     * @param map    字典 a->钱 b->睿
     * @return 替换后的字符串
     */
    public static String mapping(String target, String prefix, String suffix, Function<String, String> map) {
        return mapping(target.toCharArray(), prefix.toCharArray(), suffix.toCharArray(), map);
    }

    private static String mapping(final char[] target, final char[] prefix, final char[] suffix, Function<String, String> map) {
        final StringBuilder result = new StringBuilder(target.length);
        for (int i = 0; i < target.length; i++) {
            if (match(target, i, prefix)) {
                final int start = i + prefix.length;
                final int end = end(start, target, suffix);
                final String key = new String(target, start, end - start);
                final String value = map.apply(key);
                result.append(value);
                i = end + suffix.length - 1;
            } else {
                result.append(target[i]);
            }
        }
        return result.toString();
    }

    private static int end(final int start, final char[] chars, final char[] e) {
        for (int i = start; i < chars.length; i++) {
            if (match(chars, i, e)) {
                return i;
            }
        }
        throw new RuntimeException("not end");
    }

    private static boolean match(char[] target, int i, char[] match) {
        for (int j = 0; j < match.length; j++) {
            if (j + i >= target.length || target[j + i] != match[j]) return false;
        }
        return true;
    }

    private static boolean match(byte[] target, int i, byte[] match) {
        for (int j = 0; j < match.length; j++) {
            if (j + i >= target.length || target[j + i] != match[j]) return false;
        }
        return true;
    }

    /**
     * @param bytes  目标   1,2,3,5,6
     * @param prefix 前缀  2,3
     * @return 下标 1
     */
    public static int first(byte[] bytes, byte[] prefix) {
        for (int i = 0; i < bytes.length; i++) {
            if (match(bytes, i, prefix)) return i;
        }
        return -1;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
