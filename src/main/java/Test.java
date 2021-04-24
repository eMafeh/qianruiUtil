import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Test {
    //经过测试,dpi为96,100,105,120,150,200中,105显示效果较为清晰,体积稳定,dpi越高图片体积越大,一般电脑显示分辨率为96
    public static final float DEFAULT_DPI = 105;
    //默认转换的图片格式为jpg
    public static final String DEFAULT_FORMAT = "jpg";

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        pdfToImage("F:\\doc\\xingcaidoc\\入职材料\\瑞慈2020体检报告");
    }


    public static void pdfToImage(String pdfPath) {
        try {
            new File(pdfPath).mkdir();
            //利用PdfBox生成图像
            PDDocument pdDocument = PDDocument.load(new File(pdfPath + ".pdf"));
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            //循环每个页码
            for (int i = 0, len = pdDocument.getNumberOfPages(); i < len; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, DEFAULT_DPI, ImageType.RGB);
                boolean write = ImageIO.write(image, DEFAULT_FORMAT, new File(pdfPath + "\\result" + i + "." + DEFAULT_FORMAT));
                System.out.println(write);
            }
            pdDocument.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}