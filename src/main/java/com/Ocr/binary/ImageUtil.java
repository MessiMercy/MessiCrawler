package com.Ocr.binary;

import com.jhlabs.image.ScaleFilter;
import com.luciad.imageio.webp.WebPReadParam;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {


    /**
     * 判断是否位黑色像素
     */
    public static boolean isBlack(int rgb) {
        Color color = new Color(rgb);
        return color.getRed() + color.getGreen() + color.getBlue() <= 300;
    }

    /**
     * 缩放图片,默认16x16
     */
    public static BufferedImage scaleImage(BufferedImage img) {
        return scaleImage(img, 16, 16);
    }

    /**
     * 缩放图片
     */
    public static BufferedImage scaleImage(BufferedImage img, int width, int height) {
        ScaleFilter sf = new ScaleFilter(width, height);
        BufferedImage imgdest = new BufferedImage(width, height, img.getType());
        return sf.filter(img, imgdest);
    }


    /**
     * 获得训练集图片的分类，如a-12.jpg，返回a
     */
    public static String getImgClass(String filename) {
        String[] arr = filename.split("-");
        return arr[0];
    }

    /**
     * 左右拼接图片，要求两张图片height相同
     *
     * @param one 第一张
     * @param two 第二张
     * @return 新图
     */
    public static BufferedImage horizontalAddImage(BufferedImage one, BufferedImage two) {
        if (one.getHeight() != two.getHeight()) return null;
        BufferedImage image = new BufferedImage(one.getWidth() + two.getWidth(), one.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[] ImageArrayOne = new int[one.getWidth() * one.getHeight()];
        int[] oneRgb = one.getRGB(0, 0, one.getWidth(), one.getHeight(), ImageArrayOne, 0, one.getWidth());
        int[] twoReArrayOne = new int[two.getWidth() * two.getHeight()];
        int[] twoRgb = two.getRGB(0, 0, two.getWidth(), two.getHeight(), twoReArrayOne, 0, two.getWidth());
        image.setRGB(0, 0, one.getWidth(), one.getHeight(), oneRgb, 0, one.getWidth());
        image.setRGB(one.getWidth(), 0, two.getWidth(), one.getHeight(), twoRgb, 0, two.getWidth());
        return image;
    }

    /**
     * 上下拼接图片，要求两张照片宽度相同
     */
    public static BufferedImage verticalAddImage(BufferedImage one, BufferedImage two) {
        if (one.getWidth() != two.getWidth()) return null;
        BufferedImage result = new BufferedImage(one.getWidth(), one.getHeight() + two.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[] ImageArrayOne = new int[one.getWidth() * one.getHeight()];
        int[] oneRgb = one.getRGB(0, 0, one.getWidth(), one.getHeight(), ImageArrayOne, 0, one.getWidth());
        int[] twoReArrayOne = new int[two.getWidth() * two.getHeight()];
        int[] twoRgb = two.getRGB(0, 0, two.getWidth(), two.getHeight(), twoReArrayOne, 0, two.getWidth());
        result.setRGB(0, 0, one.getWidth(), one.getHeight(), oneRgb, 0, one.getWidth());
        result.setRGB(0, one.getHeight(), two.getWidth(), two.getHeight(), twoRgb, 0, two.getWidth());
        return result;
    }

    public static BufferedImage readWebp(File file) throws IOException {
        ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();
        WebPReadParam readParam = new WebPReadParam();
        reader.setInput(new FileImageInputStream(file));
        return reader.read(0, readParam);
    }


}

