package fun.qianrui.staticUtil;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 88382571
 * 2019/4/22
 */
@SuppressWarnings("all")
public class FileSystemCache {
    private final ConcurrentHashMap<String, ConcurrentHashMap> CACHE;
    private static final ConcurrentHashMap<?, ?> END = new ConcurrentHashMap<>();
    public final String root;

    public FileSystemCache(Path path) {
        this(path.toFile());
    }

    public FileSystemCache(File root) {
        String tmp = root.toString();
        this.root = tmp.endsWith("\\") ? tmp : tmp + "\\";
        long l = System.currentTimeMillis();
        CACHE = file(root);
        System.out.println(root + " cache handler:" + (System.currentTimeMillis() - l));
    }


    private String[] check(File file) {
        String s = file.toString()
                .replaceAll("/", "\\");
        ExceptionUtil.isTrue(s.startsWith(this.root));
        String[] split = s.substring(this.root.length())
                .split("\\\\");
        ExceptionUtil.isTrue(split.length > 0);
        return split;
    }

    public void add(Path path) {
        add(path.toFile());
    }

    public boolean exist(Path path) {
        return exist(path.toFile());
    }

    public void add(File file) {
        String[] paths = check(file);
        ConcurrentHashMap<String, ConcurrentHashMap> tmp = CACHE;
        for (int i = 0, pathsLength = paths.length - 1; i < pathsLength; i++) {
            String path = paths[i];
            tmp = tmp.computeIfAbsent(path, k -> new ConcurrentHashMap<>());
            ExceptionUtil.isTrue(tmp != END);
        }
        tmp.put(paths[paths.length - 1], END);
    }

    public boolean exist(File file) {
        String[] paths = check(file);
        ConcurrentHashMap<String, ConcurrentHashMap> tmp = CACHE;
        for (int i = 0, pathsLength = paths.length; i < pathsLength; i++) {
            String path = paths[i];
            tmp = tmp.get(path);
            if (tmp == null) return false;
        }
        return true;
    }

    private static ConcurrentHashMap<String, ConcurrentHashMap> file(File file) {
        File[] files = file.listFiles();
        ConcurrentHashMap<String, ConcurrentHashMap> result = new ConcurrentHashMap<>();
        if (files == null) return result;
        for (File sub : files) {
            result.put(sub.getName(), sub.isDirectory() ? file(sub) : END);
        }
        return result;
    }

    //性能贼垃圾
    private static void walkTree(Path path) throws IOException {
        Files.walkFileTree(path, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void main(String[] args) {
        FileSystemCache cache = new FileSystemCache(Paths.get("F:\\Downloads\\m3u8result"));
        System.out.println(cache.exist(Paths.get("F:\\Downloads\\m3u8result\\平胸")));
        System.out.println(cache.exist(Paths.get("F:\\Downloads\\m3u8result\\平胸\\video1.posh-hotels.com.8091-99920191228-5526id00046-A-1000kb-hls-.mp4")));
        System.out.println(cache.exist(Paths.get("F:\\Downloads\\m3u8result\\平胸\\text.fadf")));
        cache.add(Paths.get("F:\\Downloads\\m3u8result\\平胸\\text.fadf"));
        System.out.println(cache.exist(Paths.get("F:\\Downloads\\m3u8result\\平胸\\text.fadf")));
    }
}
