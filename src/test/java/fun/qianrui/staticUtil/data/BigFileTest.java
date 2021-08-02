package fun.qianrui.staticUtil.data;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class BigFileTest {
    public static void main(String[] args) {
        {
            final BigFile bigFile = new BigFile("F:\\Downloads\\testBig\\test", 3 * 20);
            bigFile.put("1", "啊哈哈".getBytes());
            bigFile.put("2", "teafhasfkja".getBytes());
            bigFile.put("3", "".getBytes());
            bigFile.put("4", "1".getBytes());
            bigFile.put("5", "特别的长的啊字符串".getBytes());
            final HashMap<String, byte[]> all = bigFile.getAll();
            all.forEach((k, v) -> System.out.println(k + " " + new String(v)));
        }
        {
            final BigFile bigFile = new BigFile("F:\\Downloads\\testBig\\test", 3 * 20);
            bigFile.put("6", "二次运行怕不怕".getBytes());
            final HashMap<String, byte[]> all = bigFile.getAll();
            all.forEach((k, v) -> System.out.println(k + " " + new String(v)));
            System.out.println(bigFile.getSize());
            System.out.println(bigFile.getLength());
            System.out.println(bigFile.exist("0"));
            System.out.println(bigFile.exist("1"));
            System.out.println(new String(bigFile.get("1")));
        }
    }

    @org.testng.annotations.Test
    public void delete() throws IOException {
        final String file = "F:\\Downloads\\testBig\\test";
        final BigFile bigFile = new BigFile(file, 20 * 100_000_000);
        System.out.println(bigFile.getSize());
//        for (int i = 0; i < 100_000_000; i++) {
//            bigFile.put(i + "", "1234567890".getBytes());
//        }
        bigFile.perfectLength();
        final List<byte[]> allData = bigFile.getAllData();
    }
}