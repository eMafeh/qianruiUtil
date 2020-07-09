package fun.qianrui.staticUtil.computer;

import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.util.List;

public class StringUtil {
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
}
