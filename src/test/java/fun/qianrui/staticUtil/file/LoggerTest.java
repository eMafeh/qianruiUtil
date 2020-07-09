package fun.qianrui.staticUtil.file;

public class LoggerTest {
    static final Logger logger = new Logger("F:\\Downloads\\log\\ts");

    @org.testng.annotations.Test
    public void testPut() {
        logger.put("你好啊");
        logger.put("");
        logger.put("啊哈哈 嘿嘿");
        logger.put("这 课 还 行 哎呦");
        logger.put("");
        logger.put("你好啊\n啊哈哈 嘿嘿");
    }

    @org.testng.annotations.Test
    public void testLogSize() {
        System.out.println(logger.logSize());
    }

    @org.testng.annotations.Test
    public void testLogStream() {
        logger.logStream()
                .forEach(System.out::println);
    }
}