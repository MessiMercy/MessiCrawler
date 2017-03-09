package com.Ocr;


import com.Ocr.binary.Preprocess;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/1.
 */
public class ZhilianOcr {
    private ReadImage readImage = new ReadImage();
    private Map<BufferedImage, String> trainData;

    public ZhilianOcr(String dirName) {
        System.out.println("正在载入训练数据");
        trainData = readImage.loadTrainData(dirName);
    }

    /**
     * 在参数第一个传入待识别文件路径，第二个传入训练模板集路径
     */
    public static void main(String[] args) throws Exception {
        ZhilianOcr test = new ZhilianOcr(args[1]);
        String str = test.predict(ImageIO.read(new File(args[0])));
        System.out.println("识别结果： " + str);
    }

    public String predict(BufferedImage image) throws Exception {
        ReadImage readImage = new ReadImage();
//        Map<BufferedImage, String> trainData = readImage.loadTrainData();
        StringBuilder builder = new StringBuilder();
        BufferedImage binaryImage = new Preprocess().getBinaryImage(image);
        EqualsSplit split = new EqualsSplit(binaryImage);
        List<BufferedImage> run = split.run();
        run.forEach(p -> builder.append(readImage.getSingleCharOcr(p, trainData)));
        return builder.toString();
    }
}
