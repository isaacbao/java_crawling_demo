import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import utils.CustomFileUtil;
import utils.ImageCutInfo;
import utils.ImageHelper;
import web.Browser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by rongyang_lu on 2016/9/6.
 */
public class crawlLiepin {
    private static final String HOME_PAGE_URL = "https://passport.liepin.com/e/account/#sfrom=click-pc_homepage-front_navigation-ecomphr_new";
    private static final String CAPTCHA_URL = "https://passport.liepin.com/captcha/word/getcap/";

    private static Browser browser = new Browser(BrowserVersion.CHROME);

    public static void main(String[] args) throws Exception {
        getCaptcha();
    }

    private static void getCaptcha() throws IOException {
        String homePage = browser.getData(HOME_PAGE_URL).getContentAsString();
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "image/webp,image/*,*/*;q=0.8");
        header.put("Referer", "https://passport.liepin.com/captcha/word/iframe/");
        WebResponse captchaResponse = browser.getData(CAPTCHA_URL, "t" + new Date().getTime(), header);
        String captchaDir = CustomFileUtil.getRootPath() + File.separator;
        String captchaPath = captchaDir + "captcha.jpg";
        CustomFileUtil.saveImage(captchaResponse.getContentAsStream(), captchaPath);

        splitOriginCaptcha(captchaDir, captchaPath);

        String trueCaptchaPath = combineCaptcha(captchaDir);

        // 验证码中选择汉字的区域
        String captchaSelectPath = captchaDir + "captchaSelect.jpg";
        ImageCutInfo iciSelect = new ImageCutInfo(120, 200, 256, 256, 0, 0);
        ImageHelper.cut(trueCaptchaPath, captchaSelectPath, iciSelect);

        // 四个要被选出来的汉字
        String captchaTargetPath = captchaDir + "captchaTarget.jpg";
        ImageCutInfo iciTarget = new ImageCutInfo(40, 200, 256, 256, 0, 120);
        ImageHelper.cut(trueCaptchaPath, captchaTargetPath, iciTarget);

        List<Grid> grids = new ArrayList<>();
        // 存放切成3行之后的验证码的路径
        List<String> captchaLines = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String captchaSplit = captchaDir + "captchaSplit" + i + ".jpg";
            ImageCutInfo ici = new ImageCutInfo(40, 120, 256, 256, 0, (i - 1) * 40);
            ImageHelper.cut(captchaSelectPath, captchaSplit, ici);
            captchaLines.add(captchaSplit);
            String binaryPath = captchaDir + "captchaSplit" + i + "-binary.jpg";
            grids.addAll(keepBlack(binaryPath, captchaSplit, i));
        }


        grids.sort(new GridComparatorByBlackPixel());
        System.out.println(grids);
        while (grids.size() > 8) {
            grids.remove(0);
        }

        List<List<Grid>> rowGroup = Grid.groupByRow(grids);

        getCharInGrids(rowGroup, captchaLines);


        System.out.println(grids);
    }

    private static void getCharInGrids(List<List<Grid>> rowGroup, List<String> captchaLines) {
        int rowNumber = rowGroup.size();
        if (rowNumber != captchaLines.size()) {
            return;
        }
        CloudCaptchaBreaker.doLogin();
        for (int i = 0; i < rowNumber; i++) {
            String captchaPath = captchaLines.get(i);
            String captchaResult = CloudCaptchaBreaker.breakCaptcha(captchaPath, CloudCaptchaBreaker.CHINESE_CHAR, "GBK");
            List<Grid> currentRow = rowGroup.get(i);
            int resultLength = captchaResult.length();
            if (resultLength != currentRow.size()) {
                System.out.println("识别失败");
                break;
            }
            currentRow.sort(new GridComparatorByColunm());
            for (int j = 0; j < resultLength; j++) {
                currentRow.get(j).character = captchaResult.charAt(j) + "";
            }
        }
    }

    /**
     * 像素点
     */
    static class Pixel {
        /**
         * 水平位置坐标(x,y)，左上角那个点是(0,0)
         */
        public int x;
        public int y;

        /**
         * 三原色
         */
        public int red;
        public int green;
        public int blue;

        public Pixel(int x, int y, int red, int green, int blue) {
            this.x = x;
            this.y = y;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
        public String toString() {
            return "Pixel{" +
                    "blue=" + blue +
                    ", x=" + x +
                    ", y=" + y +
                    ", red=" + red +
                    ", green=" + green +
                    '}';
        }
    }

    /**
     * 保留所有黑色的像素，其他像素涂白
     *
     * @param binaryPath   处理后的图片路径
     * @param captchaSplit 原图路径
     * @param rowBase      第几行图片
     * @throws IOException
     */
    private static List<Grid> keepBlack(String binaryPath, String captchaSplit, int rowBase)
            throws
            IOException {
        BufferedImage originImage = ImageIO.read(new File(captchaSplit));

        BufferedImage binaryImage = new BufferedImage(256, 40,
                BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < originImage.getWidth(); x++) {
            for (int y = 0; y < originImage.getHeight(); y++) {
                Pixel pixel = getPixel(originImage, x, y);
//                        System.out.println(x + " : " + y + " " + r + "," + g + "," + b);
                // 二值化
                int newPixel;
                if (pixel.red < 50 && pixel.green < 100 && pixel.blue < 100) {
                    newPixel = colorToRGB(0, 0, 0, 0);
                } else {
                    newPixel = colorToRGB(255, 255, 255, 255);
                }
                binaryImage.setRGB(x, y, newPixel);
            }
        }
        ImageIO.write(binaryImage, "jpg", new File(binaryPath));
        List<Grid> grids = new ArrayList<>();
        getCharPosition(binaryImage, 8, 1, grids, binaryPath, rowBase);
        System.out.println(grids.toString());
        return grids;
    }


    /**
     * 获取图片中某一像素点的RGB值
     *
     * @param originImage 原图
     * @param x           横向坐标【坐标为(x,y)，左上角为(0,0)】
     * @param y           纵向坐标
     * @return
     */
    private static Pixel getPixel(BufferedImage originImage, int x, int y) {
        final int color = originImage.getRGB(x, y);
        // 从原来32位的颜色值中分离出各占8位的R,G,B值
        final int r = (color >> 16) & 0xff;
        final int g = (color >> 8) & 0xff;
        final int b = color & 0xff;
        return new Pixel(x, y, r, g, b);
    }

    public static String captchaDir = CustomFileUtil.getRootPath() + File.separator;

    /**
     * 把图片分割成column*row格，通过素描每一格中黑像素的个数获取有字符的格，推算各个字符在图片中的大致位置
     *
     * @param binaryImage 要分析的图片
     * @param column      总列数
     * @param row         总行数
     * @param rowBase     分析的图片在整个验证码中的行数
     */
    private static void getCharPosition(BufferedImage binaryImage, int column, int row, List<Grid> grids, String
            binaryPath, int rowBase) throws IOException {
        int width = binaryImage.getWidth() / column;
        int height = binaryImage.getHeight() / row;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                //这一格当中黑像素的个数
                int blackPixelAmount = 0;
                //从左上角开始扫描
                int top = i * height;
                int left = j * width;
                int clickPointX = 0;
                int clickPointY = 0;
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        int x = left + w;
                        int y = top + h;
                        Pixel pixel = getPixel(binaryImage, x, y);
//                        System.out.println(pixel.toString());
                        //RGB越小就越是黑不溜秋的
                        if (pixel.red < 10 && pixel.green < 10 && pixel.blue < 10) {
                            blackPixelAmount++;
                        }
                        if (blackPixelAmount == 50) {
                            clickPointX = x;
                            clickPointY = y;
                        }
                    }
                }

                //一个格子里的黑像素超过50个，这个格子里可能就有字了
                if (blackPixelAmount > 50) {
                    Grid grid = new Grid(i + rowBase - 1, j, blackPixelAmount, clickPointX, clickPointY);
                    grids.add(grid);
                    String captchaSplit = binaryPath + grid.row + "-" + grid.colunm + ".jpg";
                    ImageCutInfo ici = new ImageCutInfo(40, 40, 256, 32, left, top);
                    ImageHelper.cut(binaryPath, captchaSplit, ici);
                }

            }
        }
    }

    /**
     * 把代表颜色的四通道，4个8bit的整型合成为ImageIO能识别的一个32位的整型
     *
     * @param alpha alpha通道的值
     * @param red   红色值
     * @param green 绿色值
     * @param blue  蓝色值
     * @return ImageIO能识别的32位颜色值
     */

    private static int colorToRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;

    }


    /**
     * 把打散的验证码图重新组合起来
     *
     * @param captchaDir 防验证码的目录
     * @throws IOException
     * @Return 重新组合后的验证码路径
     */
    private static String combineCaptcha(String captchaDir) throws IOException {
        String captchaPath = captchaDir + "trueCaptcha.jpg";
        BufferedImage trueCaptchaBuffer = new BufferedImage(256, 200, BufferedImage.TYPE_INT_RGB);
        FileOutputStream out = new FileOutputStream(captchaPath);
        Graphics g = trueCaptchaBuffer.createGraphics();
        for (int i = 1; i <= 16; i++) {
            String captchaPathTemp = captchaDir + "captcha" + i + ".jpg";
            File captcha = new File(captchaPathTemp);
            Image src = javax.imageio.ImageIO.read(captcha);
            g.drawImage(src, (i - 1) * 16, 0, 16, 100, null);
            captcha.delete();

            String captchaPathTempB = captchaDir + "captchaB" + i + ".jpg";
            File captchaB = new File(captchaPathTempB);
            Image srcB = javax.imageio.ImageIO.read(captchaB);
            g.drawImage(srcB, (i - 1) * 16, 100, 16, 100, null);
            captchaB.delete();
        }

        g.dispose();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        encoder.encode(trueCaptchaBuffer);
        out.close();

        return captchaPath;
    }

    /**
     * 把图片切割成一个一个栅格
     */
    static class Grid {
        public int row;
        public int colunm;
        //黑色像素个数
        public int blackPixel;

        public String character;

        // 该格中像素比较密集的一个点，在整幅验证码图当中的坐标
        public int clickPointX;
        public int clickPointY;

        public Grid(int row, int colunm, int blackPixel) {
            this.colunm = colunm;
            this.row = row;
            this.blackPixel = blackPixel;
        }

        public Grid(int row, int colunm, int blackPixel, int clickPointX, int clickPointY) {
            this.row = row;
            this.colunm = colunm;
            this.blackPixel = blackPixel;
            this.clickPointX = clickPointX;
            this.clickPointY = clickPointY;
        }

        public static Grid getByChar(String character, List<Grid> grids) {
            for (Grid grid : grids) {
                if (grid.character.equals(character)) {
                    return grid;
                }
            }
            return null;
        }

        public static Grid getByPosition(int row, int colunm, List<Grid> grids) {
            for (Grid grid : grids) {
                if (grid.row == row && grid.colunm == colunm) {
                    return grid;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "Grid{" +
                    "row=" + row +
                    ", colunm=" + colunm +
                    ", blackPixel=" + blackPixel +
                    ", character=" + character +
                    ", clickPointX=" + clickPointX +
                    ", clickPointY=" + clickPointY +
                    '}';
        }

        public static List<Grid> getByRow(int row, List<Grid> grids) {
            List<Grid> girdInRow = new ArrayList<>();
            grids.forEach(grid -> {
                if (grid.row == row) {
                    girdInRow.add(grid);
                }
            });
            return girdInRow;
        }

        public static int getMaxRow(List<Grid> grids) {
            grids.sort(new GridComparatorByRow());
            return grids.get(grids.size() - 1).row;
        }

        public static List<List<Grid>> groupByRow(List<Grid> grids) {
            int maxRow = getMaxRow(grids);
            List<List<Grid>> gridGroup = new ArrayList<>();
            for (int i = 0; i <= maxRow; i++) {
                gridGroup.add(new ArrayList<>());
            }
            grids.forEach(grid -> {
                gridGroup.get(grid.row).add(grid);
            });
            return gridGroup;
        }
    }

    /**
     * 根据黑色像素的大小，从小到大排
     */
    static class GridComparatorByBlackPixel implements java.util.Comparator<Grid> {
        @Override
        public int compare(Grid g1, Grid g2) {
            if (g1.blackPixel < g2.blackPixel) return -1;
            if (g1.blackPixel > g2.blackPixel) return 1;

            return 0;
        }
    }

    /**
     * 根据行号的大小，从小到大排
     */
    static class GridComparatorByRow implements java.util.Comparator<Grid> {
        @Override
        public int compare(Grid g1, Grid g2) {
            if (g1.row < g2.row) return -1;
            if (g1.row > g2.row) return 1;

            return 0;
        }
    }

    /**
     * 根据行号的大小，从小到大排
     */
    static class GridComparatorByColunm implements java.util.Comparator<Grid> {
        @Override
        public int compare(Grid g1, Grid g2) {
            if (g1.colunm < g2.colunm) return -1;
            if (g1.colunm > g2.colunm) return 1;

            return 0;
        }
    }

    /**
     * 猎聘网的验证码是被切成32等分，打乱后拼起来的，所以现在先要把这32份切出来
     *
     * @param captchaDir  存放验证码的目录
     * @param captchaPath 原始验证码的地址
     * @throws IOException
     */
    private static void splitOriginCaptcha(String captchaDir, String captchaPath) throws IOException {
        String captchaPath1 = captchaDir + "captcha1.jpg";
        ImageCutInfo ici1 = new ImageCutInfo(100, 200, 256, 16, 96, 0);
        ImageHelper.cut(captchaPath, captchaPath1, ici1);

        String captchaPath2 = captchaDir + "captcha2.jpg";
        ImageCutInfo ici2 = new ImageCutInfo(100, 200, 256, 16, 16, 0);
        ImageHelper.cut(captchaPath, captchaPath2, ici2);

        String captchaPath3 = captchaDir + "captcha3.jpg";
        ImageCutInfo ici3 = new ImageCutInfo(100, 200, 256, 16, 80, 0);
        ImageHelper.cut(captchaPath, captchaPath3, ici3);

        String captchaPath4 = captchaDir + "captcha4.jpg";
        ImageCutInfo ici4 = new ImageCutInfo(100, 200, 256, 16, 48, 0);
        ImageHelper.cut(captchaPath, captchaPath4, ici4);

        String captchaPath5 = captchaDir + "captcha5.jpg";
        ImageCutInfo ici5 = new ImageCutInfo(100, 200, 256, 16, 0, 0);
        ImageHelper.cut(captchaPath, captchaPath5, ici5);

        String captchaPath6 = captchaDir + "captcha6.jpg";
        ImageCutInfo ici6 = new ImageCutInfo(100, 200, 256, 16, 112, 0);
        ImageHelper.cut(captchaPath, captchaPath6, ici6);

        String captchaPath7 = captchaDir + "captcha7.jpg";
        ImageCutInfo ici7 = new ImageCutInfo(100, 200, 256, 16, 32, 0);
        ImageHelper.cut(captchaPath, captchaPath7, ici7);

        String captchaPath8 = captchaDir + "captcha8.jpg";
        ImageCutInfo ici8 = new ImageCutInfo(100, 200, 256, 16, 64, 0);
        ImageHelper.cut(captchaPath, captchaPath8, ici8);

        String captchaPath9 = captchaDir + "captcha9.jpg";
        ImageCutInfo ici9 = new ImageCutInfo(100, 200, 256, 16, 192, 0);
        ImageHelper.cut(captchaPath, captchaPath9, ici9);

        String captchaPath10 = captchaDir + "captcha10.jpg";
        ImageCutInfo ici10 = new ImageCutInfo(100, 200, 256, 16, 160, 0);
        ImageHelper.cut(captchaPath, captchaPath10, ici10);

        String captchaPath11 = captchaDir + "captcha11.jpg";
        ImageCutInfo ici11 = new ImageCutInfo(100, 200, 256, 16, 240, 0);
        ImageHelper.cut(captchaPath, captchaPath11, ici11);

        String captchaPath12 = captchaDir + "captcha12.jpg";
        ImageCutInfo ici12 = new ImageCutInfo(100, 200, 256, 16, 128, 0);
        ImageHelper.cut(captchaPath, captchaPath12, ici12);

        String captchaPath13 = captchaDir + "captcha13.jpg";
        ImageCutInfo ici13 = new ImageCutInfo(100, 200, 256, 16, 176, 0);
        ImageHelper.cut(captchaPath, captchaPath13, ici13);

        String captchaPath14 = captchaDir + "captcha14.jpg";
        ImageCutInfo ici14 = new ImageCutInfo(100, 200, 256, 16, 208, 0);
        ImageHelper.cut(captchaPath, captchaPath14, ici14);

        String captchaPath15 = captchaDir + "captcha15.jpg";
        ImageCutInfo ici15 = new ImageCutInfo(100, 200, 256, 16, 144, 0);
        ImageHelper.cut(captchaPath, captchaPath15, ici15);

        String captchaPath16 = captchaDir + "captcha16.jpg";
        ImageCutInfo ici16 = new ImageCutInfo(100, 200, 256, 16, 224, 0);
        ImageHelper.cut(captchaPath, captchaPath16, ici16);

        String captchaPathB1 = captchaDir + "captchaB1.jpg";
        ImageCutInfo iciB1 = new ImageCutInfo(100, 200, 256, 16, 64, 100);
        ImageHelper.cut(captchaPath, captchaPathB1, iciB1);

        String captchaPathB2 = captchaDir + "captchaB2.jpg";
        ImageCutInfo iciB2 = new ImageCutInfo(100, 200, 256, 16, 32, 100);
        ImageHelper.cut(captchaPath, captchaPathB2, iciB2);

        String captchaPathB3 = captchaDir + "captchaB3.jpg";
        ImageCutInfo iciB3 = new ImageCutInfo(100, 200, 256, 16, 112, 100);
        ImageHelper.cut(captchaPath, captchaPathB3, iciB3);

        String captchaPathB4 = captchaDir + "captchaB4.jpg";
        ImageCutInfo iciB4 = new ImageCutInfo(100, 200, 256, 16, 0, 100);
        ImageHelper.cut(captchaPath, captchaPathB4, iciB4);

        String captchaPathB5 = captchaDir + "captchaB5.jpg";
        ImageCutInfo iciB5 = new ImageCutInfo(100, 200, 256, 16, 48, 100);
        ImageHelper.cut(captchaPath, captchaPathB5, iciB5);

        String captchaPathB6 = captchaDir + "captchaB6.jpg";
        ImageCutInfo iciB6 = new ImageCutInfo(100, 200, 256, 16, 80, 100);
        ImageHelper.cut(captchaPath, captchaPathB6, iciB6);

        String captchaPathB7 = captchaDir + "captchaB7.jpg";
        ImageCutInfo iciB7 = new ImageCutInfo(100, 200, 256, 16, 16, 100);
        ImageHelper.cut(captchaPath, captchaPathB7, iciB7);

        String captchaPathB8 = captchaDir + "captchaB8.jpg";
        ImageCutInfo iciB8 = new ImageCutInfo(100, 200, 256, 16, 96, 100);
        ImageHelper.cut(captchaPath, captchaPathB8, iciB8);

        String captchaPathB9 = captchaDir + "captchaB9.jpg";
        ImageCutInfo iciB9 = new ImageCutInfo(100, 200, 256, 16, 192, 100);
        ImageHelper.cut(captchaPath, captchaPathB9, iciB9);

        String captchaPathB10 = captchaDir + "captchaB10.jpg";
        ImageCutInfo iciB10 = new ImageCutInfo(100, 200, 256, 16, 160, 100);
        ImageHelper.cut(captchaPath, captchaPathB10, iciB10);

        String captchaPathB11 = captchaDir + "captchaB11.jpg";
        ImageCutInfo iciB11 = new ImageCutInfo(100, 200, 256, 16, 240, 100);
        ImageHelper.cut(captchaPath, captchaPathB11, iciB11);

        String captchaPathB12 = captchaDir + "captchaB12.jpg";
        ImageCutInfo iciB12 = new ImageCutInfo(100, 200, 256, 16, 128, 100);
        ImageHelper.cut(captchaPath, captchaPathB12, iciB12);

        String captchaPathB13 = captchaDir + "captchaB13.jpg";
        ImageCutInfo iciB13 = new ImageCutInfo(100, 200, 256, 16, 176, 100);
        ImageHelper.cut(captchaPath, captchaPathB13, iciB13);

        String captchaPathB14 = captchaDir + "captchaB14.jpg";
        ImageCutInfo iciB14 = new ImageCutInfo(100, 200, 256, 16, 208, 100);
        ImageHelper.cut(captchaPath, captchaPathB14, iciB14);

        String captchaPathB15 = captchaDir + "captchaB15.jpg";
        ImageCutInfo iciB15 = new ImageCutInfo(100, 200, 256, 16, 144, 100);
        ImageHelper.cut(captchaPath, captchaPathB15, iciB15);

        String captchaPathB16 = captchaDir + "captchaB16.jpg";
        ImageCutInfo iciB16 = new ImageCutInfo(100, 200, 256, 16, 224, 100);
        ImageHelper.cut(captchaPath, captchaPathB16, iciB16);
    }

}
