package com.Ocr;

import com.Ocr.binary.Preprocess;
import com.google.common.io.Files;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * http://wenshu.court.gov.cn/验证码识别
 * Created by Administrator on 2017/1/9.
 */
public class WenshuOcr {
    public static void main(String[] args) throws IOException {
//        Downloader downloader = new Downloader(HttpConstant.UserAgent.CHROME);
//        downloader.setDelayTime(0);
//        for (int i = 0; i < 100; i++) {
//            Request request = new Request("http://wenshu.court.gov.cn/User/ValidateCode");
//            request.setSleepTime(0);
//            HttpResponse response = downloader.downloadEntity(request);
//            java.nio.file.Files.copy(response.getEntity().getContent(), Paths.get("wenshu", i + ".bmp"), StandardCopyOption.REPLACE_EXISTING);
//        }
        File dir = new File("wenshu");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                BufferedImage read = ImageIO.read(file);
                String wenshu = WenshuOcr.predictWenshu(read);
                Files.copy(file, new File("wenshu", wenshu + ".bmp"));
                file.delete();
                System.out.println(wenshu);
            }
        }
    }

    public static String predictWenshu(BufferedImage image) {
        Preprocess pre = new Preprocess();
        BufferedImage binarryimage = pre.getBinaryImage(image);
        StringBuilder result = new StringBuilder();
        ReadImage read = new ReadImage();
        EqualsSplit split = new EqualsSplit(binarryimage);
        List<BufferedImage> run = null;
        try {
            run = split.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<BufferedImage, String> wenshuImageLib = read.loadTrainData("WenshuImageLib");
        if (run != null) {
            run.forEach(p -> result.append(read.getSingleCharOcr(p, wenshuImageLib)));
        }
        return result.toString();
    }
}
