package com.Ocr.geetest;

import com.pipeline.Filepipeline;
import com.pipeline.impl.SimpleFilepipeline;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/12/21.
 */
public class SampleProcess {
    public static Filepipeline pipe = new SimpleFilepipeline(new File("path.txt"));

    public static void main(String[] args) {
//        secondFunc();
//        Map<Integer, List<Integer[]>> integerListMap = thirdFunc();
//        Integer[] integers = integerListMap.keySet().toArray(new Integer[1]);
//        Arrays.sort(integers);
//        System.out.println(Arrays.toString(integers));
//        int i = Arrays.binarySearch(integers, 2);
//        System.out.println("找到： " + i);
//        System.out.println(integers[Math.abs(i + 2)]);
    }

    public static List<int[]> forthFunc(int xdiff) {
        Map<Integer, List<Integer[]>> integerListMap = thirdFunc();
        Integer[] integers = integerListMap.keySet().toArray(new Integer[1]);
        Arrays.sort(integers);
        System.out.println(Arrays.toString(integers));
        int i = Arrays.binarySearch(integers, xdiff);
        System.out.println("-------------------------------" + i);
        if (i >= 0) {
            return transIntergerToint(integerListMap.get(integers[i]));
        } else {
            Integer integer = integers[Math.abs(i + 2)];
            System.out.println("选出离目标最近似的一副轨迹，目标为" + xdiff + ",而真实轨迹为" + integer);
            List<int[]> ints = transIntergerToint(integerListMap.get(integer));
            ints.add(new int[]{xdiff - integer, 0, 50});
            return ints;
        }
    }

    public static List<int[]> transIntergerToint(List<Integer[]> list) {
        List<int[]> result = new ArrayList<>();
        list.forEach(p -> {
            int[] temp = new int[p.length];
            for (int i = 0; i < p.length; i++) {
                temp[i] = p[i];
            }
            result.add(temp);
        });
        return result;
    }


    /**
     * 将偏移量与轨迹联系起来，返回一个map，直接调用此方法获得轨迹库
     */
    public static Map<Integer, List<Integer[]>> thirdFunc() {
        List<List<String[]>> lists = firstFunc();
        List<List<Integer[]>> collect = lists.stream().map(SampleProcess::secondFunc).collect(Collectors.toList());
        Map<Integer, List<Integer[]>> resultMap = new HashMap<>();
        collect.forEach(p -> {
            final int[] sum = {0};
            p.forEach(l -> sum[0] += l[0]);
            resultMap.put(sum[0], p);
        });
        return resultMap;
    }

    /**
     * 从文本提取原始数据
     */
    public static List<List<String[]>> firstFunc() {
        List<String> list = pipe.readLines(Charset.defaultCharset());
        List<List<String>> sampleList = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        for (String s : list) {
            if (s.contains("black")) {
                if (temp != null || temp.size() == 0) {
                    sampleList.add(temp);
                    temp = new ArrayList<>();
                } else {
                    temp = new ArrayList<>();
                }
            } else {
                temp.add(s);
            }
        }
        System.out.println(sampleList.size());
        List<List<String[]>> collect = sampleList.stream().filter(p -> p.size() != 0).map(p -> p.stream().map(l -> l.split(" ")).collect(Collectors.toList())).collect(Collectors.toList());
        collect.forEach(p -> {
            int y = Integer.valueOf(p.get(0)[1]);
            long ts = Long.valueOf(p.get(0)[2]);
            p.forEach(k -> {
                k[1] = Integer.valueOf(k[1]) - y + "";
                k[2] = Long.valueOf(k[2]) - ts + "";
                System.out.print(String.format("%s,%s,%s", k[0], k[1], k[2]) + "|");
            });
            System.out.println();
        });
        return collect;
    }

    /**
     * @param test 原始数据
     * @return 需要的位移及延时数据
     */
    public static List<Integer[]> secondFunc(List<String[]> test) {
        List<List<Integer>> collectReal = test.stream().map(p -> Arrays.stream(p).map(k -> Integer.parseInt(k.trim())).collect(Collectors.toList())).collect(Collectors.toList());
        for (int i = collectReal.size() - 1; i >= 0; i--) {
            if (i != 0) {
                List<Integer> integersNow = collectReal.get(i);
                List<Integer> integersFore = collectReal.get(i - 1);
                integersNow.set(0, integersNow.get(0) - integersFore.get(0));
                integersNow.set(2, integersNow.get(2) - integersFore.get(2));
            }
        }
        return collectReal.stream().map(p -> p.toArray(new Integer[p.size()])).collect(Collectors.toList());
    }
}
