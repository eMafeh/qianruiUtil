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
        long l = System.currentTimeMillis();
        final int[] num = new int[1];
        final Thread loopThread = ThreadUtil.createLoopThread("FileSystemCache handler: " + root, () -> {
            Thread.sleep(3000);
            System.out.println(num[0]);
        });
        loopThread.setDaemon(true);
        loopThread.start();
        num[0]++;
        CACHE = file(root, num);
        loopThread.interrupt();
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

    private static ConcurrentHashMap<String, ConcurrentHashMap> file(File file, int[] num) {
        final long l = System.currentTimeMillis();
        File[] files = file.listFiles();
        System.out.println((System.currentTimeMillis() - l) + " " + file);
        final ConcurrentHashMap<String, ConcurrentHashMap> map = new ConcurrentHashMap<>();
        if (files == null) return map;
        for (File sub : files) {
            num[0]++;
            final String name = sub.getName();
            final int i = name.lastIndexOf(".");
            map.put(name, i < 0 || !FILE_ENDS.contains(name.substring(i + 1)) ? file(sub, num) : END);
        }
        return map;
    }


    public static void main(String[] args) {
        FileSystemCache cache = new FileSystemCache("F:\\Downloads\\m3u8result");
        System.out.println(cache.exist("F:\\Downloads\\m3u8result\\平胸"));
        System.out.println(cache.exist("F:\\Downloads\\m3u8result\\平胸\\video1.posh-hotels.com.8091-99920191228-5526id00046-A-1000kb-hls-.mp4"));
        System.out.println(cache.exist("F:\\Downloads\\m3u8result\\平胸\\text.fadf"));
        cache.add("F:\\Downloads\\m3u8result\\平胸\\text.fadf");
        System.out.println(cache.exist("F:\\Downloads\\m3u8result\\平胸\\text.fadf"));
        System.out.println("a".replaceAll("a", "\\\\"));
    }
}
