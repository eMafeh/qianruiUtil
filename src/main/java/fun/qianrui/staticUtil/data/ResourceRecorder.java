package fun.qianrui.staticUtil.data;

import java.util.function.BiConsumer;

@Deprecated
public class ResourceRecorder {
    private final byte[] dirs = new byte[1_000_000];
    private int index = 0;

    public String poll() {
        for (; index < dirs.length; index++) {
            if (dirs[index] != -1) {
                return pollNew(index, takeHead(index));
            }
        }
        throw new RuntimeException("dirs is not enough");
    }

    public void release(String pre) {
        find(pre, (index, num) -> dirs[index] &= (255 - num));
    }

    public void mark(String pre) {
        find(pre, (index, num) -> dirs[index] |= num);
    }

    private static void find(String pre, BiConsumer<Integer, Integer> work) {
        int num = 1 << (Integer.parseInt(pre.substring(pre.length() - 1)) - 1);
        int index = Integer.parseInt(pre.substring(0, pre.length() - 1));
        work.accept(index, num);
    }

    private int takeHead(int index) {
        final byte dir = dirs[index];
        if ((dir & 1) == 0) {
            dirs[index] |= 1;
            return 1;
        }
        if ((dir & 2) == 0) {
            dirs[index] |= 2;
            return 2;
        }
        if ((dir & 4) == 0) {
            dirs[index] |= 4;
            return 3;
        }
        if ((dir & 8) == 0) {
            dirs[index] |= 8;
            return 4;
        }
        if ((dir & 16) == 0) {
            dirs[index] |= 16;
            return 5;
        }
        if ((dir & 32) == 0) {
            dirs[index] |= 32;
            return 6;
        }
        if ((dir & 64) == 0) {
            dirs[index] |= 64;
            return 7;
        }
        if ((dir & 128) == 0) {
            dirs[index] |= 128;
            return 8;
        }
        throw new RuntimeException("bad index");
    }

    private static String pollNew(int index, int last) {
        final int[] ints = new int[7];
        ints[ints.length - 1] = last;
        for (int i = ints.length - 2; i >= 0; i--) {
            ints[i] = index % 10;
            index /= 10;
        }
        final StringBuilder result = new StringBuilder(ints.length * 2);
        for (int i : ints) {
            result.append(i);
        }
        return result.toString();
    }


    public static void main(String[] args) {
        final ResourceRecorder resourceRecorder = new ResourceRecorder();
        System.out.println(pollNew(98_7654, 1));
        System.out.println(pollNew(100_0000, 2));
        System.out.println(pollNew(123_4567, 3));
        resourceRecorder.mark("123");
        resourceRecorder.mark("121");
        resourceRecorder.mark("126");
        System.out.println(resourceRecorder.dirs[12]);
        for (int i = 0; i < 6; i++) {
            System.out.println(resourceRecorder.takeHead(12));
        }
    }
}
