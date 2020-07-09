package fun.qianrui.staticUtil.computer;

import fun.qianrui.staticUtil.sys.ExceptionUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {
    private final static String AES = "AES";

    public static Cipher encrypt(byte[] keyByte) {
        return encrypt(keyByte, null);
    }

    private static Cipher encrypt(byte[] keyByte, byte[] iv) {
        return create(keyByte, iv, Cipher.ENCRYPT_MODE);
    }


    public static Cipher decrypt(byte[] keyByte) {
        return decrypt(keyByte, null);
    }

    public static Cipher decrypt(byte[] keyByte, byte[] iv) {
        return create(keyByte, iv, Cipher.DECRYPT_MODE);
    }

    //    cipher.doFinal(byte[])  
    private static Cipher create(byte[] keyByte, byte[] iv, int mode) {
        //初始化一个密钥对象
        SecretKeySpec keySpec = new SecretKeySpec(keyByte, AES);
        try {   // 指定加密的算法、工作模式和填充方式
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            if (iv == null)
                cipher.init(mode, keySpec, cipher.getParameters());
            else
                cipher.init(mode, keySpec, new IvParameterSpec(iv));
            return cipher;
        } catch (Exception e) {
            return ExceptionUtil.throwT(e);
        }
    }
}