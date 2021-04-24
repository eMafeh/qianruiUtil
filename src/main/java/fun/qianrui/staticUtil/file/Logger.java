package fun.qianrui.staticUtil.file;

import fun.qianrui.staticUtil.data.ByteList;
import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 硬盘的最小存储单位    扇区  512
 * 操作系统最小单位     簇/群集 文件分配的空间大小必然是4096倍数
 * NTFS主控文件表        MFT  文件索引大小固定1024
 * 小于1024的文件会直接放在MFT内
 * fsutil fsinfo ntfsinfo F:
 * 扇区
 * 总扇区：                7,813,965,823  (3.6 TB)
 * 每扇区字节数  ：                512
 * 簇
 * 总群集：                  976,745,727  (3.6 TB)
 * 空余群集：                843,987,905  (3.1 TB)
 * 总保留群集 ：                  1,024  (4.0 MB)
 * 用于存储备用的保留 ：                 0  (0.0 KB)
 * 每物理扇区字节数 ：            4096
 * 每群集字节数 ：                4096
 * MFT
 * 每 FileRecord 分段字节数    ：  1024
 * 每 FileRecord 分段群集数 ：  0
 * Mft 有效数据长度 ：            3.04 GB
 * Mft 开始 Lcn  ：                   0x00000000000c0000
 * Mft2 开始 Lcn ：                   0x0000000000000002
 * Mft 区域开始 ：                   0x0000000008cd30a0
 * Mft 区域结束   ：                   0x0000000008cdf8c0
 * MFT 区域大小  ：                   200.13 MB
 * 看不懂
 * 最大设备修剪程度计数 ：     31
 * 最大设备修剪字节计数 ：       0xffffffff
 * 最大卷修剪程度计数 ：     31
 * 最大卷修剪字节计数 ：       0x40000000
 * Resource Manager 标识符:     21F817DE-212B-11EA-A1FE-3860773BCF63
 */
public class Logger {
    public static final String LOG_SUFFIX = ".log";
    public static final byte _N = '\n';
    public static final int MAX = 1 << 28;//1 << 28 268_435_456
    public static final int MIN = 1 << 10;//1 << 28 268_435_456
    private final String rootStr;
    private MappedByteBuffer map;
    private int nowIndex;

    public Logger(String file) {
        this(file, MIN);
    }

    public Logger(String file, int initialCapacity) {
        ExceptionUtil.isTrue(!file.endsWith("\\") && !file.endsWith("/"), () -> "cannot not be directory:" + file);

        final File root = new File(file);
        final String name = root.getName();
        ExceptionUtil.isTrue(!name.contains("."), () -> "cannot specify the file format:" + file);
        this.rootStr = file;

        final File parent = root.getParentFile();
        //日志文件父文件夹生成
        parent.mkdirs();
        final String[] files = parent.list((dir, f) -> f.startsWith(name) && f.endsWith(LOG_SUFFIX));
        //获取日志的最大下标
        this.nowIndex = files != null ? Stream.of(files)
                .mapToInt(f -> {
                    final String i = f.substring(name.length(), f.length() - LOG_SUFFIX.length());
                    return i.isEmpty() ? 0 : Integer.parseInt(i.substring(1));
                })
                .max()
                .orElse(0) : 0;

        initialCapacity = Math.min(initialCapacity, MAX);
        initialCapacity = Math.max(initialCapacity, MIN);
        updateMmap(initialCapacity);
    }

    private void updateMmap(final int initialCapacity) {
        //当前文件已经放不下,按日志模式开新文件
        if (initialCapacity > MAX) {
            updateMmap(target(++nowIndex), MAX);
        }
        //当前文件还放的下
        else {
            final File now = target(nowIndex);
            //当前长度
            final int length = (int) now.length();
            final int limit = length == 0 || length < initialCapacity ? FileUtil.goodBlockSize(initialCapacity) : length;
            ExceptionUtil.isTrue(limit <= MAX, () -> "limit not legal:" + limit);
            updateMmap(now, limit);
        }
    }

    private void updateMmap(final File now, final int limit) {
        //增大当前文件
        map = FileUtil.mmap(now, limit);
        int i = 0;
        for (; i < limit; i++) if (map.get() == 0) break;
        map.position(i);
    }

    public synchronized void put(String line) {
        byte[] bytes = line.getBytes();
        int l = bytes.length + 1;
        ExceptionUtil.isTrue(l <= MAX, () -> "bytes.length:" + l + " is large than MAX:" + MAX);
        if (l + map.position() > map.limit()) updateMmap(l + map.position());
        for (byte b : bytes) {
            if (b == 0) throw new RuntimeException("log not support byte 0");
            map.put(b);
        }
        map.put(_N);
    }

    public void reLog() {
        final String collect = logStream().collect(Collectors.joining("\n"));
        final Logger logger = new Logger(rootStr + "reLog");
        logger.put(collect);
    }

    public synchronized Stream<String> logStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new LogStream(),
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    private File target(int i) {
        String mid = i == 0 ? "" : ("_" + i);
        return new File(rootStr + mid + LOG_SUFFIX);
    }

    private class LogStream implements Iterator<String> {
        private MappedByteBuffer map;
        private int _index;
        private int _length;

        public LogStream() {
            updateMmap();
        }

        private void updateMmap() {
            if (_index > nowIndex) {
                map = null;
                return;
            }
            final File file = target(_index);
            final long l = file.length();
            if (l != 0) {
                map = FileUtil.mmap(file, l);
                int i = 0;
                for (; i < l; i++) if (map.get() == 0) break;
                _length = i;
                map.rewind();
            }
            _index++;
            if (l == 0) updateMmap();
        }

        @Override
        public boolean hasNext() {
            return map != null && _length > map.position();
        }

        @Override
        public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            final ByteList byteList = new ByteList();
            while (true) {
                if (map.position() == _length) break;
                final byte b = map.get();
                if (b == _N) break;
                byteList.add(b);
            }
            if (map.position() == _length) {
                _index++;
                updateMmap();
            }
            return new String(byteList.toArray());
        }
    }
}
