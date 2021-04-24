import java.io.*;
import java.util.Arrays;
import java.util.InputMismatchException;
public class Main {
    private static final String IN = "12512589, 5635853145, 1933042390, 8087405011, 5850093081, 4798730430, 3502914518, 8959624012, 8228400525, 7466048158, 1741080966, 8589434492, 7105014191, 5135349590, 3039948729, 149845882, 914029359, 3644520402, 1473128452, 1658986175, 9885250404, 4456923124, 1190832240, 46693319, 89114048, 3778801843, 5316629536, 5711844234, 6017639698, 6071657460, 1662343211, 6630451369, 4507889035, 3521225623, 570390942, 6076845607, 7833185827, 8026062807, 5198828089, 3019501328, 8759727775, 7266762291, 9559007538, 9257179479, 5393536180, 1423383282, 4620807520, 2353282266, 8622394482, 2096011231, 7796563616, 8436536760, 9967955565, 9996948149, 6114993744, 3924375134, 2662129582, 2972808008, 8401440474, 237434004, 3758659627, 926236763, 6772057253, 562150945, 87893307, 9187902463, 2758873257, 2728965117, 5879085665, 6911832026, 8376110111, 7264931181, 4849391156, 2053590503, 7437360759, 4684591205, 4579607532, 9491561632, 7444380016, 1082796716, 5990478225, 3852351451, 7350077822, 6089663381, 5724051637, 3613391522, 1515549181, 2251045259, 4251533555, 8028809473, 5171056246, 9899899289, 7515488144, 3455610828, 1689809870, 6573076571, 4918973357, 635395367, 6997589038, 5048066652";

    public static void main(String[] args) throws IOException {
//        for (int i = 0; i < 700; i++) {
//            System.out.println(i+" "+(Math.sqrt(7*7*i*i+11100)));
//        }
    }

    private static void doTask(long num, int[] prime) {
        long temp = num;
        long eular = num;
        for (int i : prime) {
            boolean in = false;
            while (temp % i == 0) {
                if (!in) {
                    in = true;
                    eular = eular / i * (i - 1);
                }
                temp /= i;
                if (temp == 1) {
                    doTask1(num, eular);
                    return;
                }
            }
        }
        eular = eular / temp * (temp - 1);
        doTask1(num, eular);
    }

    private static final int T = 1000000007;

    private static void doTask1(long num, long eular) {
        if (num % 2 == 0) {
            System.out.println(t(t(num / 2) * t(num + 1 - eular)));
        } else
            System.out.println(t(t(num) * t((num + 1 - eular) / 2)));
    }

    private static long t(long l) {
        return l % T;
    }
}


/**
 * 范围 2 * 3 * 5 * 7 * 11 * 13 * 17 * 19 * 23
 * 100 次 耗时 90313 ms
 */
class PrimeProducer {
    //    private static final int[] primes = init(2 * 3 * 5 * 7 * 11 * 13 * 17 * 19 * 23);

    public static int[] init(int area) {
        if (area < 15) return new int[]{2, 3, 5, 7, 11, 13};
        boolean[] is = new boolean[area];
        is[0] = true;
        int length = 1;
        int nextP = 1;
        int[] result = new int[(int) (1.3 * is.length / Math.log(is.length))];
        int index = 0;
        while (length != is.length) {
            while (!is[nextP++ % length]) ;
            result[index++] = nextP;
            length = group(nextP, is, length);
            for (int i = length / nextP - 1; i >= 0; i--) {
                if (is[i]) is[(i + 1) * nextP - 1] = false;
            }
        }
        while (length > nextP * nextP) {
            while (!is[nextP++]) ;
            result[index++] = nextP;
            int temp = length / nextP - 1;
            if (temp % 2 != 0) temp--;
            for (int i = temp; i >= 0; i -= 2) {
                if (is[i]) is[(i + 1) * nextP - 1] = false;
            }
        }
        for (int i = nextP + 1; i < is.length; i += 2) {
            if (is[i])
                result[index++] = i + 1;
        }
        return Arrays.copyOf(result, index);
    }

    private static int group(int nextP, boolean[] is, int length) {
        int nowL = length;
        int i = nextP * length;
        length = Math.min(i < 0 ? Integer.MAX_VALUE : i, is.length);
        while (nowL * 2 < length) {
            System.arraycopy(is, 0, is, nowL, nowL);
            nowL *= 2;
        }
        System.arraycopy(is, 0, is, nowL, length - nowL);
        return length;
    }
}


final class Input {
    static final byte[] b = new byte[1024];
    static int l = 0, pos = 0;

    static int readByte() {
        if (l == -1) throw new InputMismatchException();
        if (pos >= l) {
            pos = 0;
            try {
                l = System.in.read(b);
            } catch (IOException e) {
                throw new InputMismatchException();
            }
            if (l <= 0) return -1;
        }
        return b[pos++];
    }

    static boolean isSpaceChar(int c) {
        return !(c >= 33 && c <= 126);
    }

    static int skip() {
        int b;
        while ((b = readByte()) != -1 && isSpaceChar(b)) ;
        return b;
    }

    static String ns() {
        int b = skip();
        StringBuilder sb = new StringBuilder();
        while (!(isSpaceChar(b))) { // when nextLine, (isSpaceChar(b) && b != ' ')
            sb.appendCodePoint(b);
            b = readByte();
        }
        return sb.toString();
    }

    static String nline() {
        int b = skip();
        StringBuilder sb = new StringBuilder();
        while (!isSpaceChar(b) || b == ' ') {
            sb.appendCodePoint(b);
            b = readByte();
        }
        return sb.toString();
    }

    static int ni() {
        int num = 0, b;
        boolean minus = false;
        while ((b = readByte()) != -1 && !((b >= '0' && b <= '9') || b == '-')) ;
        if (b == '-') {
            minus = true;
            b = readByte();
        }
        while (true) {
            if (b >= '0' && b <= '9') num = (num << 3) + (num << 1) + (b - '0');
            else return minus ? -num : num;
            b = readByte();
        }
    }

    static long nl() {
        long num = 0;
        int b;
        boolean minus = false;
        while ((b = readByte()) != -1 && !((b >= '0' && b <= '9') || b == '-')) ;
        if (b == '-') {
            minus = true;
            b = readByte();
        }
        while (true) {
            if (b >= '0' && b <= '9') num = num * 10 + (b - '0');
            else return minus ? -num : num;
            b = readByte();
        }
    }
}