package fun.qianrui.staticUtil.sys;

import fun.qianrui.staticUtil.computer.DateUtil;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 88382571
 * 2019/4/25
 */
public class ThreadUtil {
    private static final ThreadGroup GROUP = System.getSecurityManager() == null ? Thread.currentThread()
            .getThreadGroup() : System.getSecurityManager()
            .getThreadGroup();

    public static Thread createThread(String name, RunnableCanException runnable) {
        return new Thread(GROUP, () -> {
            System.out.println("thread is start(" + Thread.currentThread()
                    .getName() + ")");
            final long start = System.currentTimeMillis();
            try {
                runnable.run();
            } catch (InterruptedException e) {
                //
            } catch (Exception e) {
                ExceptionUtil.throwT(e);
            } finally {
                System.out.println("thread is end (" + Thread.currentThread()
                        .getName() + ")" + DateUtil.format("yyyyMMdd HH:mm:ss", start)
                        + " to " + DateUtil.format("yyyyMMdd HH:mm:ss", System.currentTimeMillis()));
            }
        }, name, 0);
    }

    public static Thread createLoopThread(String name, RunnableCanException runnable) {
        return createThread(name, () -> {
            while (true) runnable.run();
        });
    }

    public static ThreadPoolExecutor createPool(int corePoolSize, int maximumPoolSize, String prefix) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000), new ThreadFactory() {
            AtomicLong aLong = new AtomicLong();

            @Override
            public Thread newThread(Runnable r) {
                return createThread(prefix + aLong.incrementAndGet(), r::run);
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @FunctionalInterface
    public interface RunnableCanException {
        void run() throws Exception;
    }
}
