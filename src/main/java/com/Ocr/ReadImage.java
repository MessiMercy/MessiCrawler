package com.Ocr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ReadImage {

    public boolean isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() > 300) {
            return true;
        }
        return false;
        // return colorInt > -1644827 ? 1 : 0;
    }

    public boolean isColor(int colorInt) {
        return !(colorInt > -3000000 && colorInt < -2000000);
        // return colorInt > -1644827 ? 1 : 0;
    }

    public int[][] showRgb(BufferedImage image) {
        int[][] result = new int[image.getWidth()][image.getHeight()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                result[i][j] = image.getRGB(i, j);
            }
            System.out.println(Arrays.toString(result[i]));
        }
        return result;
    }

    public BufferedImage removeBackgroud(BufferedImage img) throws Exception {
        int width = img.getWidth();
        int height = img.getHeight();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (isWhite(img.getRGB(x, y))) {
                    img.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    img.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return img;
    }

    public List<BufferedImage> splitImage(BufferedImage img) throws Exception {
        List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
        int imgWidth = img.getWidth();
        int imgNums = imgWidth / 8;
        // subImgs.add(img.getSubimage(0, 0, 8, 10));
        // subImgs.add(img.getSubimage(8, 0, 8, 10));
        // subImgs.add(img.getSubimage(16, 0, 8, 10));
        // subImgs.add(img.getSubimage(24, 0, 8, 10));
        for (int i = 0; i < imgNums; i++) {
            subImgs.add(img.getSubimage(i * 8, 0, 8, 10));
        }
        return subImgs;
    }

    public Map<BufferedImage, String> loadTrainData(String dirName) {
        Map<BufferedImage, String> map = new HashMap<>();
        if (dirName == null) {
            dirName = "3_drop";
        }
        File dir = new File(dirName);
        File[] files = dir.listFiles();
        for (File file : files) {
            try {
                map.put(ImageIO.read(file), file.getName().charAt(0) + "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public String getSingleCharOcr(BufferedImage img, Map<BufferedImage, String> map) {
        String result = "";
        int width = img.getWidth();
        int height = img.getHeight();
        int min = width * height;
        for (BufferedImage bi : map.keySet()) {
            int count = 0;
            Label:
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    if (isWhite(img.getRGB(x, y)) != isWhite(bi.getRGB(x, y))) {
                        count++;
                        if (count >= min)
                            break Label;
                    }
                }
            }
            if (count < min) {
                min = count;
                result = map.get(bi);
            }
        }
        return result;
    }

//    public String getAllOcr(String url) throws Exception {
//        BufferedImage img = removeBackgroud(FetchIps.getImage(url));
//        List<BufferedImage> listImg = splitImage(img);
//        Map<BufferedImage, String> map = loadTrainData("train");
//        String result = "";
//        for (BufferedImage bi : listImg) {
//            result += getSingleCharOcr(bi, map);
//        }
//        return result;
//    }

}
