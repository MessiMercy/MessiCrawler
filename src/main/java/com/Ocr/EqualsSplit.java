package com.Ocr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/11/1.
 */
public class EqualsSplit {

    private File file;
    private BufferedImage image;

    public EqualsSplit(File image) {
        this.file = image;
    }

    public EqualsSplit(BufferedImage image) {
        this.image = image;
    }

    public List<BufferedImage> run() throws IOException {
        List<BufferedImage> list = new ArrayList<>();
        if (image == null) {
            image = ImageIO.read(this.file);
        }
        BufferedImage img1 = image.getSubimage(6, 8, 12, 16);//文书网专用分割法。。
        BufferedImage img2 = image.getSubimage(19, 8, 12, 16);
        BufferedImage img3 = image.getSubimage(32, 8, 12, 16);
        BufferedImage img4 = image.getSubimage(45, 8, 12, 16);
        list.add(img1);
        list.add(img2);
        list.add(img3);
        list.add(img4);
//        try {
//            ImageIO.write((img1), "bmp", new File("test", StringUtils.substringBefore(file.getName(), ".") + "_1.bmp"));
//            ImageIO.write((img2), "bmp", new File("test", StringUtils.substringBefore(file.getName(), ".") + "_2.bmp"));
//            ImageIO.write((img3), "bmp", new File("test", StringUtils.substringBefore(file.getName(), ".") + "_3.bmp"));
//            ImageIO.write((img4), "bmp", new File("test", StringUtils.substringBefore(file.getName(), ".") + "_4.bmp"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return list;
    }


    public static void main(String[] args) throws IOException {
        EqualsSplit split = new EqualsSplit(new File("D:\\workspace\\MessiCralwer\\wenshu\\79.bmp"));
        List<BufferedImage> run = split.run();
        Random random = new Random();
        run.forEach(p -> {
            try {
                ImageIO.write(p, "bmp", new File("WenshuImageLib", random.nextDouble() + ".bmp"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
