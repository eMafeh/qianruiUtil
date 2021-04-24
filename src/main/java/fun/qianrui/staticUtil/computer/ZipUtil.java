package fun.qianrui.staticUtil.computer;

import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZipUtil {
    private static final Inflater INFLATER = new Inflater();
    private static final Deflater DEFLATER9 = new Deflater(9);

    public static boolean add(Map<String, List<String>> substitutes) {
        substitutes.put("STSongStd-Light",
                Arrays.asList("STSong","华文宋体"));
        return true;
    }
    public static synchronized byte[] zip(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DEFLATER9.setInput(bytes);
            DEFLATER9.finish();
            byte[] buffer = new byte[1 << 20];
            int n;
            while ((n = DEFLATER9.deflate(buffer)) != 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        } finally {
            DEFLATER9.reset();
        }
    }

    public static synchronized byte[] unZip(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            INFLATER.setInput(bytes);
            byte[] buffer = new byte[1 << 20];
            int n;
            while ((n = INFLATER.inflate(buffer)) != 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException | DataFormatException e) {
            return ExceptionUtil.throwT(e);
        } finally {
            INFLATER.reset();
        }
    }
}
