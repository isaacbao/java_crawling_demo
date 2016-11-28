package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 图片裁剪助手
 *
 * @author larryfu
 */

public class ImageHelper {

    public static void cut(String tempPhoto, String destPath, ImageCutInfo ici) throws IOException {
        Rectangle rect = new Rectangle(ici.getX(), ici.getY(), ici.getW(), ici.getH());
        cut(tempPhoto, destPath, ici.getViewWidth(), ici.getViewHeight(), rect);
    }

    /**
     * @param srcImageFile  原始图
     * @param destImageFile 目标图
     * @param width         原始图预处理后width
     * @param height        原始图预处理后height
     * @param rect          目标图输出的格式(rect.x, rect.y, rect.width, rect.height)
     * @throws IOException
     * @Description: 将srcImageFile裁剪后生成destImageFile
     */
    public static void cut(String srcImageFile, String destImageFile, int width, int height, Rectangle rect) throws IOException {
        String formatName = srcImageFile.substring(srcImageFile.lastIndexOf('.') + 1);
        Image image = ImageIO.read(new File(srcImageFile));
        Image from = image.getScaledInstance(width, height, BufferedImage.SCALE_AREA_AVERAGING);
        BufferedImage subImage;
        //将image转成bufferdImage
        if (formatName.toLowerCase().equals("png")) {
            BufferedImage bImage = resizePNG(from, width, height);
            Image sub = bImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
            subImage = resizePNG(sub, rect.width, rect.height);
        } else {
            BufferedImage bImage = resizeImage(from, width, height);
            Image sub = bImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
            subImage = resizeImage(sub, rect.width, rect.height);
        }
        ImageIO.write(subImage, formatName, new File(destImageFile));
    }

    private static BufferedImage resizeImage(Image img, int width, int height) throws IOException {
        BufferedImage tag = new BufferedImage(width, height, 1);
        Graphics g = tag.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return tag;
    }

    //为防止png透明背景黑化，需要做特殊处理
    private static BufferedImage resizePNG(Image from, int newWidth, int newHeight) {
        BufferedImage to = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = to.createGraphics();
        to = g2d.getDeviceConfiguration().createCompatibleImage(newWidth, newHeight, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = to.createGraphics();
        g2d.drawImage(from, 0, 0, null);
        g2d.dispose();
        return to;
    }



}
