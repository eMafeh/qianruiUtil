package fun.qianrui.staticUtil.computer;

import fun.qianrui.staticUtil.sys.ExceptionUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Deprecated
public class ZipUtil {
    private static final ThreadLocal<Inflater> INFLATER = ThreadLocal.withInitial(Inflater::new);
    private static final ThreadLocal<Deflater> DEFLATER9 = ThreadLocal.withInitial(() -> new Deflater(9));

    public static byte[] zip(byte[] bytes) {
        Deflater deflater = DEFLATER9.get();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            deflater.setInput(bytes);
            deflater.finish();
            byte[] buffer = new byte[1 << 20];
            int n;
            while ((n = deflater.deflate(buffer)) != 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        } finally {
            deflater.reset();
        }
    }


    public static byte[] unZip(byte[] bytes) {
        final Inflater inflater = INFLATER.get();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            inflater.setInput(bytes);
            byte[] buffer = new byte[1 << 20];
            int n;
            while ((n = inflater.inflate(buffer)) != 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException | DataFormatException e) {
            return ExceptionUtil.throwT(e);
        } finally {
            inflater.reset();
        }
    }

}
