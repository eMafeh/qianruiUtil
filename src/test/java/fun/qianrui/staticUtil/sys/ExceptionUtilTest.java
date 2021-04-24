package fun.qianrui.staticUtil.sys;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ExceptionUtilTest {

    @Test
    public void testThrowT() {
        try {
            ExceptionUtil.throwT(()->1/0);
        } catch (Throwable e1) {
            e1.printStackTrace();
        }

    }
}