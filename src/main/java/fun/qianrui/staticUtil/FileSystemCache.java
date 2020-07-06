package fun.qianrui.staticUtil;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 88382571
 * 2019/4/22
 */
@SuppressWarnings("all")
public class FileSystemCache {
    public static final List<String> FILE_ENDS = new ArrayList<>(Arrays.asList("ts", "key", "jpg", "mp4", "m3u8"));
    private final ConcurrentHashMap<String, ConcurrentHashMap> CACHE;
    private static final ConcurrentHashMap<?, ?> END = new ConcurrentHashMap<>();
    public final String root;

    public FileSystemCache(String file) {
        this(new File(file));
    }

    public FileSystemCache(File root) {
        String tmp = root.toString();
        this.root = tmp.endsWith("\\") ? tmp : tmp + "\\";
        CACHE = file(root);
    }


    private String[] check(String file) {
        String s = file.replaceAll("/", "\\\\");
        ExceptionUtil.isTrue(s.startsWith(this.root));
        String[] split = s.substring(this.root.length())
                .split("\\\\");
        ExceptionUtil.isTrue(split.length > 0);
        return split;
    }

    public void add(String file) {
        String[] rotes = check(file);
        ConcurrentHashMap<String, ConcurrentHashMap> tmp = CACHE;
        for (int i = 0, l = rotes.length - 1; i < l; i++) {
            String rote = rotes[i];
            tmp = tmp.computeIfAbsent(rote, k -> new ConcurrentHashMap<>());
            ExceptionUtil.isTrue(tmp != END);
        }
        tmp.put(rotes[rotes.length - 1], END);
    }

    public boolean exist(String file) {
        String[] rotes = check(file);
        ConcurrentHashMap<String, ConcurrentHashMap> tmp = CACHE;
        for (int i = 0, l = rotes.length; i < l; i++) {
            String rote = rotes[i];
            tmp = tmp.get(rote);
            if (tmp == null) return false;
        }
        return true;
    }

    public boolean prefix(String file) {
        String[] rotes = check(file);
        ConcurrentHashMap<String, ConcurrentHashMap> tmp = CACHE;
        for (int i = 0, l = rotes.length - 1; i < l; i++) {
            String rote = rotes[i];
            tmp = tmp.get(rote);
            if (tmp == null) return false;
        }
        for (String name : tmp.keySet()) {
            if (name.startsWith(rotes[rotes.length - 1])) return true;
        }
        return false;
    }

    private static ConcurrentHashMap<String, ConcurrentHashMap> file(File file) {
        final long l = System.currentTimeMillis();
        File[] files = file.listFiles();
        final ConcurrentHashMap<String, ConcurrentHashMap> map = new ConcurrentHashMap<>();
        if (files == null) return map;
        for (File sub : files) {
            final String name = sub.getName();
            final int i = name.lastIndexOf(".");
            map.put(name, i < 0 || !FILE_ENDS.contains(name.substring(i + 1)) ? file(sub) : END);
        }
        return map;
    }
}
