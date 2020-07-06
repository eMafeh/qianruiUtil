package fun.qianrui.staticUtil;

import java.io.*;
import java.util.zip.*;

/**
 * @author 88382571
 * 2019/5/8
 */
public class SerializableUtil {
    public static byte[] serializable(Serializable obj) {
        try (ByteArrayOutputStream o1 = new ByteArrayOutputStream(256); ObjectOutputStream o2 = new ObjectOutputStream(
                o1)) {
            o2.writeObject(obj);
            o2.flush();
            return o1.toByteArray();
        } catch (IOException e) {
            return ExceptionUtil.throwT(e);
        }
    }

    public static <T extends Serializable> T deSerializable(byte[] bytes) {
        return deSerializable(bytes, 0, bytes.length);
    }

    public static <T extends Serializable> T deSerializable(byte[] bytes, int offset, int length) {
        try {
            try (InputStream byteInputStream = new ByteArrayInputStream(
                    bytes, offset, length); ObjectInputStream stream = new ObjectInputStream(byteInputStream)) {
                Object o = stream.readObject();
                @SuppressWarnings({"all"}) T object = (T) o;
                return object;
            }
        } catch (IOException | ClassNotFoundException e) {
            return ExceptionUtil.throwT(e);
        }
    }

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

    private static final ThreadLocal<Inflater> INFLATER = ThreadLocal.withInitial(Inflater::new);
    private static final ThreadLocal<Deflater> DEFLATER9 = ThreadLocal.withInitial(() -> new Deflater(9));
}
