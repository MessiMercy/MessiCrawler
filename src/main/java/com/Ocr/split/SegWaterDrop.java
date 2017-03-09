package com.Ocr.split;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 水滴分割法
 * Created by Administrator on 2016/12/14.
 */

public class SegWaterDrop {

    private int minD = 8;// 最小字符宽度
    private int maxD = 16;
    private int meanD = 12;// 平均字符宽度

    private int b = 1;// 大水滴的宽度 2*B+1,取0或者1效果最好

    private ArrayList<BufferedImage> imageList;
    private BufferedImage sourceImage;

    public SegWaterDrop() {
        imageList = new ArrayList<>();

    }

    public static void run() {
        try {
            File dir = new File("2_cfs");
            SegWaterDrop model = new SegWaterDrop();
            // 只列出jpg
            File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith("bmp"));

            for (File file : files) {
                BufferedImage sourceImage = ImageIO.read(file);
                ArrayList<BufferedImage> list = model.drop(sourceImage);
                for (int j = 0; j < list.size(); j++) {
                    BufferedImage subImg = list.get(j);
                    String prex = file.getName().split("\\.")[0];
                    String filename = "3_drop/" + prex + "-" + j + ".bmp";
                    ImageIO.write(subImg, "bmp", new File(filename));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void test() {
        try {
            SegWaterDrop model = new SegWaterDrop();
            File file = new File("2_cfs/93-0.bmp");
            PsImage ps = new PsImage(file);
            ps.rotate(350);
            File heh = new File(file.getParentFile(), file.getName() + ".bmp");
            ps.createPic(heh);
            BufferedImage sourceImage = ImageIO.read(heh);
            ArrayList<BufferedImage> list = model.drop(sourceImage);
            System.out.println("共分割出图片张数： " + list.size());
            for (int j = 0; j < list.size(); j++) {
                BufferedImage subImg = list.get(j);
                String prex = file.getName().split("\\.")[0];
                String filename = "3_drop/" + prex + "-" + j + ".bmp";
                ImageIO.write(subImg, "bmp", new File(filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 滴水法入口
     *
     * @return 切割完图片的数组
     */
    public ArrayList<BufferedImage> drop(BufferedImage sourceImage) {
        this.imageList.clear();
        this.sourceImage = sourceImage;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        // if (width <= maxD) {
        // //如果是单个字符，则直接返回
        // this.imageList.add(sourceImage);
        // return this.imageList;
        // }

        // 在x轴的投影
        int[] histData = new int[width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isBlack(sourceImage.getRGB(x, y))) {
                    histData[x]++;
                }
            }
        }

        ArrayList<Integer> extrems = Extremum.getMinExtrem(histData);

        Point[] startRoute = new Point[height];
        Point[] endRoute = null;

        for (int y = 0; y < height; y++) {
            startRoute[y] = new Point(0, y);
        }

        int num = (int) Math.round((double) width / meanD);// 字符的个数
        int lastP = 0; // 上一次分割的位置
        int curSplit = 1;// 分割点的个数，小于等于 num - 1;
        for (Integer extrem : extrems) {
            if (curSplit > num - 1) {
                break;
            }

            // 判断两个分割点之间的距离是否合法
            int curP = extrem;
            int dBetween = curP - lastP + 1;
            if (dBetween < minD || dBetween > maxD) {
                continue;
            }

            // //判断当前分割点与末尾结束点的位置是否合法
            // int dAll = width - curP + 1;
            // if (dAll < minD*(num - curSplit) || dAll > maxD*(num - curSplit))
            // {
            // continue;
            // }
            endRoute = getEndRoute(new Point(curP, 0), height, curSplit);
            doSplit(startRoute, endRoute);
            startRoute = endRoute;
            lastP = curP;
            curSplit++;
            System.out.println(curP);
        }

        endRoute = new Point[height];
        for (int y = 0; y < height; y++) {
            endRoute[y] = new Point(width - 1, y);
        }
        doSplit(startRoute, endRoute);

        System.out.println("=================");
        System.out.println(width + "," + height);

        return this.imageList;
    }

    /**
     * 获得滴水的路径
     */
    private Point[] getEndRoute(Point startP, int height, int curSplit) {

        // 获得分割的路径
        Point[] endRoute = new Point[height];
        Point curP = new Point(startP.x, startP.y);
        Point lastP = curP;
        endRoute[0] = curP;
        while (curP.y < height - 1) {
            int maxW = 0;
            int sum = 0;
            int nextX = curP.x;
            int nextY = curP.y;

            for (int j = 1; j <= 5; j++) {
                try {
                    int curW = getPixelValue(curP.x, curP.y, j) * (6 - j);
                    sum += curW;
                    if (curW > maxW) {
                        maxW = curW;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }

            // 如果全黑，需要看惯性
            if (sum == 0) {
                maxW = 4;
            }
            // 如果周围全白，则默认垂直下落
            if (sum == 15) {
                maxW = 6;
            }

            switch (maxW) {
                case 1:
                    nextX = curP.x - 1;
                    nextY = curP.y;
                    break;
                case 2:
                    nextX = curP.x + 1;
                    nextY = curP.y;
                    break;
                case 3:
                    nextX = curP.x + 1;
                    nextY = curP.y + 1;
                    break;
                case 5:
                    nextX = curP.x - 1;
                    nextY = curP.y + 1;
                    break;
                case 6:
                    nextX = curP.x;
                    nextY = curP.y + 1;
                    break;
                case 4:
                    if (nextX > curP.x) {// 具有向右的惯性
                        nextX = curP.x + 1;
                        nextY = curP.y + 1;
                    }

                    if (nextX < curP.x) {// 向左的惯性或者sum = 0
                        nextX = curP.x;
                        nextY = curP.y + 1;
                    }

                    if (sum == 0) {
                        nextX = curP.x;
                        nextY = curP.y + 1;
                    }
                    break;

                default:

                    break;
            }

            // 如果出现重复运动
            if (lastP.x == nextX && lastP.y == nextY) {
                if (nextX < curP.x) {// 向左重复
                    maxW = 5;
                    nextX = curP.x + 1;
                    nextY = curP.y + 1;
                } else {// 向右重复
                    maxW = 3;
                    nextX = curP.x - 1;
                    nextY = curP.y + 1;
                }
            }

            lastP = curP;
            int rightLimit = meanD * curSplit + 1;
            if (nextX > rightLimit) {
                nextX = rightLimit;
                nextY = curP.y + 1;
            }

            int leftLimit = meanD * (curSplit - 1) + meanD / 2;
            if (nextX < leftLimit) {
                nextX = leftLimit;
                nextY = curP.y + 1;
            }
            curP = new Point(nextX, nextY);

            endRoute[curP.y] = curP;
        }

        return endRoute;
    }

    /**
     * 具体实行切割
     */
    private void doSplit(Point[] starts, Point[] ends) {
        int left = starts[0].x;
        int top = starts[0].y;
        int right = ends[0].x;
        int bottom = ends[0].y;

        for (int i = 0; i < starts.length; i++) {
            left = Math.min(starts[i].x, left);
            top = Math.min(starts[i].y, top);
            right = Math.max(ends[i].x, right);
            bottom = Math.max(ends[i].y, bottom);
        }

        int width = right - left + 1;
        int height = bottom - top + 1;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, new Color(255, 255, 255).getRGB());
            }
        }
        for (int i = 0; i < ends.length; i++) {
            Point start = starts[i];
            Point end = ends[i];
            for (int x = start.x; x < end.x; x++) {
                if (isBlack(sourceImage.getRGB(x, i))) {
                    // System.out.println((x - left) + ", " + (start.y - top));
                    image.setRGB(x - left, start.y - top, new Color(0, 0, 0).getRGB());
                }
            }

        }
        this.imageList.add(image);

        System.out.println("-----------------------");

    }

    /**
     * 判断是否位黑色像素
     */
    private boolean isBlack(int rgb) {
        Color color = new Color(rgb);
        return color.getRed() + color.getGreen() + color.getBlue() <= 300;
    }

    /**
     * 获得大水滴中心点周围的像素值
     *
     * @param j 中心点周围的编号
     */
    private int getPixelValue(int cx, int cy, int j) {
        int rgb = 0;

        if (j == 4) {
            int right = cx + b + 1;
            right = right >= sourceImage.getWidth() - 1 ? sourceImage.getWidth() - 1 : right;
            rgb = sourceImage.getRGB(right, cy);
            return isBlack(rgb) ? 0 : 1;
        }

        if (j == 5) {
            int left = cx - b - 1;
            left = left <= 0 ? 0 : left;
            rgb = sourceImage.getRGB(left, cy);
            return isBlack(rgb) ? 0 : 1;
        }

        // 如果 1<= j <= 3, 则判断下方的区域， 只要有一个黑点，则当做黑点，
        int start = cx - b + j - 2;
        int end = cx + b + j - 2;

        start = start <= 0 ? 0 : start;
        end = end >= sourceImage.getWidth() - 1 ? sourceImage.getWidth() - 1 : end;
        int blackNum = 0;
        int whiteNum = 0;
        for (int i = start; i <= end; i++) {
            rgb = sourceImage.getRGB(i, cy + 1);
            if (isBlack(rgb)) {
                blackNum++;
            } else {
                whiteNum++;
            }
        }

        return (blackNum >= whiteNum) ? 0 : 1;
    }

    public static void main(String[] args) {

        SegWaterDrop.run();
        // test();
    }

}
