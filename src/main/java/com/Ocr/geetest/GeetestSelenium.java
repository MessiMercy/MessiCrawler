package com.Ocr.geetest;

import com.Ocr.binary.ImageUtil;
import com.downloader.selenium.SeleniumDownloader;
import com.google.common.io.Files;
import com.parser.Html;
import com.pipeline.impl.SimpleFilepipeline;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 极验验证码破解
 * Created by Administrator on 2016/11/17.
 */
public class GeetestSelenium extends SeleniumDownloader {

    public static void main(String[] args) throws IOException {
//        func();
        GeetestSelenium geetestSelenium = new GeetestSelenium();
        geetestSelenium.process();
    }

    private static void func() {
        List<String[]> topLeftPointList = new ArrayList<>();
        List<String> imgSrcList = new ArrayList<>();
        try {
            String string = Files.toString(new File("C:\\Users\\Administrator\\Desktop\\abc.html"), Charset.defaultCharset());
            String string1 = StringEscapeUtils.unescapeHtml(string);
            String regex = "(-\\d*)px\\s(.\\d*)px";
            Pattern compile = Pattern.compile(regex);
            Matcher matcher = compile.matcher(string1);
            List<String> list = new ArrayList<>();
            while (matcher.find()) {
                String[] temp = {matcher.group(1), matcher.group(2)};
                topLeftPointList.add(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 52; i++) {
            imgSrcList.add("https://static.geetest.com/pictures/gt/9b872c380/bg/b6dc4abd.webp");
        }
        boolean jpg = combineImages(imgSrcList, topLeftPointList, 26, 10, 58, "temp\\nnn.jpg", "jpg");
        if (jpg) System.out.println("成功！");
    }

    private static List<String[]> getTopLeftPiontList(String html) {
        List<String[]> topLeftPointList = new ArrayList<>();
        String unescapeHtml = StringEscapeUtils.unescapeHtml(html);
        String regex = "(-\\d*)px\\s(.\\d*)px";
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(unescapeHtml);
        while (matcher.find()) {
            String[] temp = {matcher.group(1), matcher.group(2)};
            topLeftPointList.add(temp);
        }
        return topLeftPointList;
    }


    /**
     * 合成指定的多张图片到一张图片
     *
     * @param imgSrcList       图片的地址列表
     * @param topLeftPointList 每张小图片的偏移量
     * @param countOfLine      每行的小图片个数
     * @param cutWidth         每张小图片截取的宽度（像素）
     * @param cutHeight        每张小图片截取的高度（像素）
     * @param savePath         合并后图片的保存路径
     * @param subfix           合并后图片的后缀
     * @return 是否合并成功
     */
    public static boolean combineImages(List<String> imgSrcList, List<String[]> topLeftPointList, int countOfLine, int cutWidth, int cutHeight, String savePath, String subfix) {
        if (imgSrcList == null || savePath == null || savePath.trim().length() == 0) return false;
        BufferedImage lastImage = new BufferedImage(cutWidth * countOfLine, cutHeight * ((int) (Math.floor(imgSrcList.size() / countOfLine))), BufferedImage.TYPE_INT_RGB);
        String prevSrc = "";
        BufferedImage prevImage = null;
        try {
            for (int i = 0; i < imgSrcList.size(); i++) {
                String src = imgSrcList.get(i);
                BufferedImage image;
                if (src.equals(prevSrc)) image = prevImage;
                else {
                    if (src.trim().toLowerCase().startsWith("http"))
                        image = ImageIO.read(new URL(src));
                    else
                        image = ImageUtil.readWebp(new File(src));
                    prevSrc = src;
                    prevImage = image;
                }
                if (image == null) continue;
                String[] topLeftPoint = topLeftPointList.get(i);
                int[] pixArray = image.getRGB(0 - Integer.parseInt(topLeftPoint[0].trim()), 0 - Integer.parseInt(topLeftPoint[1].trim()), cutWidth, cutHeight, null, 0, cutWidth);
                int startX = ((i) % countOfLine) * cutWidth;
                int startY = ((i) / countOfLine) * cutHeight;

                lastImage.setRGB(startX, startY, cutWidth, cutHeight, pixArray, 0, cutWidth);
            }
            File file = new File(savePath);
            return ImageIO.write(lastImage, subfix, file);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static int findXDiffRectangeOfTwoImage(String imgSrc1, String imgSrc2) {
        try {
            BufferedImage image1 = ImageIO.read(new File(imgSrc1));
            BufferedImage image2 = ImageIO.read(new File(imgSrc2));
            int width1 = image1.getWidth();
            int height1 = image1.getHeight();
            int width2 = image2.getWidth();
            int height2 = image2.getHeight();

            if (width1 != width2) return -1;
            if (height1 != height2) return -1;

            int left = 0;
            /*
              从左至右扫描
             */
            boolean flag = false;
            for (int i = 0; i < width1; i++) {
                for (int j = 0; j < height1; j++)
                    if (isPixelNotEqual(image1, image2, i, j)) {
                        left = i;
                        flag = true;
                        break;
                    }
                if (flag) break;
            }
            BufferedImage subimage = image1.getSubimage(left, 0, image1.getWidth() - left, image1.getHeight());
            ImageIO.write(subimage, "bmp", new File("temp", "cut_remain.bmp"));
            return left - 7;//在滑动之前滑块与背景图就已经存在一个距离了，需要做一个位移的调整
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    private static boolean isPixelNotEqual(BufferedImage image1, BufferedImage image2, int i, int j) {
        int pixel1 = image1.getRGB(i, j);
        int pixel2 = image2.getRGB(i, j);

        int[] rgb1 = new int[3];
        rgb1[0] = (pixel1 & 0xff0000) >> 16;
        rgb1[1] = (pixel1 & 0xff00) >> 8;
        rgb1[2] = (pixel1 & 0xff);

        int[] rgb2 = new int[3];
        rgb2[0] = (pixel2 & 0xff0000) >> 16;
        rgb2[1] = (pixel2 & 0xff00) >> 8;
        rgb2[2] = (pixel2 & 0xff);

        for (int k = 0; k < 3; k++)
            if (Math.abs(rgb1[k] - rgb2[k]) > 50)//因为背景图会有一些像素差异
                return true;

        return false;
    }

    private static int[] randomAction(int width, int times) {
        int[] result = new int[times];
        double avg = Math.floor(width / times);
        int average = Double.valueOf(avg).intValue();
        Random r = new Random();
        for (int i = 0; i < result.length; i++) {
            if (i == result.length - 1)
                result[i] = width;
            else
                result[i] = average + r.nextInt(10) - 5;
            width -= result[i];
        }
        return result;
    }

    private void actionMove(Actions actions, int length) {
        Random random = new Random();
        int sum = 0;
        while (sum != length) {
            int temp = random.nextInt(2) + 1;
            actions.moveByOffset(temp, random.nextInt(10) - 5).perform();
            System.out.println("往右移动了" + temp + "像素");
            sum += temp;
            System.out.println("已经移动了" + sum + "像素");
            System.out.println(Math.abs(length - sum));
            if (Math.abs(length - sum) < 3) {
                actions.moveByOffset(length - sum, 0).perform();
                break;
            }
        }
    }

    private void actionMove(Actions actions, List<int[]> Actionlist) {
        Actionlist.forEach(p -> {
            if (p[0] < 50) {
                actions.moveByOffset(p[0], p[1]).perform();
//                actionMove(actions, p[0]);
            } else {
                actions.moveByOffset(p[0] / 2, p[1]).perform();
//                actionMove(actions, p[0] / 2);
                sleep(p[2]);
                actions.moveByOffset(p[0] - p[0] / 2, p[1]).perform();
//                actionMove(actions, p[0] - p[0] / 2);
            }
            System.out.println("横向移动了" + p[0] + "像素");
            sleep(2 * p[2]);
            System.out.println("休息" + 2 * p[2]);
        });
        actions.release().perform();
        sleep(2000);
    }

    protected static List<int[]> get_trail_array(int distance) {
        List<int[]> arr = new ArrayList<>();
        Random r = new Random();
        double[] arrayX = {1.0 / 3, 1.0 / 4, 1.0 / 5, 2.0 / 5, 1.0 / 6, 2.0 / 7, 3.0 / 8, 2.0 / 9};
        double[] arrayY = {-0.1, -0.2, -0.3, -0.4, -0.5, .1, .2, .3, .4, .5};
        int[] move = {-3, 3, -4, 4, -5, 5, -6, 6};
        int lastMove = move[r.nextInt(move.length - 1)];
        distance += lastMove;
        System.out.println("last move: " + lastMove);
        int x = (int) Math.ceil(distance * arrayX[r.nextInt(arrayX.length - 1)]);
        int y = (int) Math.ceil(arrayY[r.nextInt(arrayY.length - 1)]);
        int t = r.nextInt(50) + 50;
        while (distance - x >= 0) {
            arr.add(new int[]{x, y, t});
            System.out.println(x + "," + y + "," + t);
//            System.out.print(x);
            distance -= x;
            if (distance == 0) break;
            x = (int) Math.ceil(distance * arrayX[r.nextInt(arrayX.length - 1)]);
            y = (int) Math.ceil(arrayY[r.nextInt(arrayY.length - 1)]);
            t = r.nextInt(50) + 50;
        }
        x = lastMove < 0 ? 1 : -1;
        lastMove = Math.abs(lastMove);
        for (int i = 0; i < lastMove; i++) {
            arr.add(new int[]{x, -1, 50});
        }
        return arr;
    }

    private List<int[]> generate(int length) {
        Random rd = new Random();
        int sx = rd.nextInt(15) + 15;
        int sy = rd.nextInt(15) + 15;
        List<int[]> arr = new ArrayList<>();
//        arr.add(new int[]{sx, sy, 0});
        int sum = 0;
        int maxCount = 100;
        double x = 0;
        double lx = length - x;
        while (Math.abs(lx) > 0.8 && maxCount-- > 0) {
            double rn = rd.nextDouble();
            double dx = rn * lx * 0.6;
            if (Math.abs(dx) < 0.5) continue;
            double dt = rd.nextDouble() * (rn * 80 + 50) + 10;
            rn = rd.nextDouble();
            double dy = 0;
            if (rn < 0.2 && dx > 10) {
                dy = rn * 20;
                if (rn < 0.05) dy = -rn * 80;
            }
            x += dx;
            int finalX = (int) (dx + 0.5);
            arr.add(new int[]{finalX, (int) (dy + 0.5), (int) (dt + 0.5)});
            lx = length - x;
            sum += finalX;
        }
        double dtlast = 500 * rd.nextDouble() + 100;
        arr.add(new int[]{length - sum, 0, (int) dtlast});
        return arr;
    }

    @Override
    public void process() {
        WebDriver driver = this.getDriver();
        driver.get("https://user.geetest.com/login?url=http:%2F%2Faccount.geetest.com%2Freport");
        findElement(driver, By.cssSelector("#email")).sendKeys("408708006@qq.com");
        findElement(driver, By.cssSelector("#password")).sendKeys("liu920923");
        new SimpleFilepipeline(new File("text.txt")).printResult(driver.getPageSource(), false);
        Html html = new Html(driver.getPageSource());
        String cutImageStyle = html.parse("div.gt_cut_bg_slice", "style", 0);
        System.out.println("cutImageStyle: " + cutImageStyle);
//        String cutImageStyle = findElement(driver, By.cssSelector("div.gt_cut_bg_slice")).getAttribute("style");
        String cutUrl = StringUtils.substringBetween(StringEscapeUtils.unescapeHtml(cutImageStyle), "\"", "\"").trim();
//        String fullImageStyle = findElement(driver, By.cssSelector("div.gt_cut_fullbg_slice")).getAttribute("style");
        String fullImageStyle = html.parse("div.gt_cut_fullbg_slice", "style", 0);
        System.out.println("fullImageStyle: " + fullImageStyle);
        String fullUrl = StringUtils.substringBetween(StringEscapeUtils.unescapeHtml(fullImageStyle), "\"", "\"").trim();
        List<String[]> topLeftPiontList = getTopLeftPiontList(driver.getPageSource());
        topLeftPiontList.forEach(p -> System.out.println(Arrays.toString(p)));
        List<String> imgSrcListcut = new ArrayList<>();
        List<String> imgSrcListfull = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            imgSrcListcut.add(cutUrl);
            imgSrcListfull.add(fullUrl);
        }
        boolean cut = combineImages(imgSrcListcut, topLeftPiontList, 26, 10, 58, "temp\\cut.jpg", "jpg");
        boolean full = combineImages(imgSrcListfull, topLeftPiontList, 26, 10, 58, "temp\\full.jpg", "jpg");
        int offset = 0;
        if (cut && full) {
            offset = findXDiffRectangeOfTwoImage("temp\\cut.jpg", "temp\\full.jpg");
        }
        System.out.println("偏移距离： " + offset);
        Actions actions = new Actions(driver);
        WebElement element = findElement(driver, By.cssSelector("div.gt_slider_knob"));
        Actions actions1 = actions.moveToElement(element).clickAndHold();
//        List<int[]> generate = generate(offset);
        List<int[]> generate = get_trail_array(offset);
//        List<int[]> generate = SampleProcess.forthFunc(offset);
        actionMove(actions1, generate);
        BufferedImage screenShots = getScreenShots();
        boolean isSuccess = true;
        try {
            driver.findElement(By.cssSelector("div.success"));
        } catch (Exception e) {
            isSuccess = false;
        }
        if (isSuccess) {
            System.out.println("拖动成功！");
            saveTrailArray(generate, offset);
        } else {
            System.out.println("拖动失败");
        }
        try {
            ImageIO.write(screenShots, "png", new File("temp", "result.jpg"));
            System.out.println("截图完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        findElement(driver, By.cssSelector("#LogIn")).click();
    }

    private void saveTrailArray(List<int[]> trail_array, int xdiff) {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            jedis.select(10);
            jedis.sadd(xdiff + "", listToString(trail_array));
        }
    }

    private String listToString(List<int[]> trail_array) {
        List<String> collect = trail_array.stream().map(m -> m[0] + "," + m[1] + "," + m[2]).collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        collect.forEach(p -> builder.append(p).append("|"));
        return builder.toString();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
