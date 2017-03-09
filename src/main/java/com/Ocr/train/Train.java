package com.Ocr.train;

import com.Ocr.binary.ImageUtil;
import com.Ocr.train.svm.svm_train;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 训练模型
 * Created by Administrator on 2016/12/14.
 */

public class Train {

    /**
     * 类标号map，如：a=>1 b=>2
     */
    private HashMap<String, Integer> labelMap = null;

    /**
     * 所有图像分类的map，key为当前类标号， value为对应的图片，图片以二维数组的形式保存
     */
    private HashMap<String, ArrayList<Integer[][]>> imageMap = null;

    public Train() {
        init();
    }

    private void init() {
        labelMap = new HashMap<>();
        imageMap = new HashMap<>();
        loadImageLabel();
        loadImage();
        svmFormat();
    }


    private void loadImageLabel() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("svm/label.txt")))) {
            String buff = null;
            while ((buff = reader.readLine()) != null) {
                String[] arr = buff.split(" ");
                labelMap.put(arr[0], Integer.parseInt(arr[1]));
            }

            System.out.println("load image label finish!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImage() {
        File dir = new File("4_scale/");
        //只列出jpg
        File[] files = dir.listFiles((K, v) -> v.endsWith("bmp"));

        for (File file : files) {
            try {
                transferToMap(file);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("load mage end");

    }

    /**
     * 将image 转换到 map中
     */
    private void transferToMap(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        Integer[][] imgArr = new Integer[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //黑色点标记为1
                int value = ImageUtil.isBlack(image.getRGB(x, y)) ? 1 : 0;
                imgArr[y][x] = value;
            }
        }

        ArrayList<Integer[][]> imgList = null;
        String className = ImageUtil.getImgClass(file.getName());
        if (imageMap.containsKey(className)) {
            imgList = imageMap.get(className);
            imgList.add(imgArr);
        } else {
            imgList = new ArrayList<>();
            imgList.add(imgArr);
            imageMap.put(className, imgList);
        }
    }


    /**
     * 转换成svm预料的格式
     */
    public void svmFormat() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new File("svm/svm.train"));
            for (String className : this.imageMap.keySet()) {
                int classLabel = labelMap.get(className);
                ArrayList<Integer[][]> list = imageMap.get(className);
                System.out.println(className);
                for (Integer[][] img : list) {
                    String tmpLine = classLabel + " ";
                    int index = 1;
                    for (Integer[] anImg : img) {
                        for (Integer anAnImg : anImg) {
                            tmpLine += index + ":" + anAnImg + " ";
                            index++;
                        }
                    }
//					System.out.println(tmpLine);
                    writer.write(tmpLine + "\r\n");
                    writer.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }


    /**
     * 生成模型
     *
     */
    public static void run() throws IOException {
        //train参数
        String[] arg = {"-t", "0", "svm/svm.train", "svm/svm.model"};

        //predict参数
        String[] parg = {"svm/svmscale.test", "svm/svm.model", "svm/result.txt"};

        System.out.println("训练开始");
        svm_train.main(arg);
        System.out.println("训练结束");
    }

    private static void produceLabel() {
        FileWriter writer = null;
        try {

            writer = new FileWriter(new File("svm/label.txt"));
            String charactor = "1234567890abcdefghijklmnopqrstuvwxyz";

            for (int i = 0; i < charactor.length(); i++) {
                char c = charactor.charAt(i);
                String str = c + " " + (i + 1) + "\r\n";
                writer.write(str);
            }
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) throws IOException {
        System.out.println("begin");
//		Train model = new Train();

        run();
        System.out.println("end");
    }
}
