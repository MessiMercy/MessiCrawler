package com.Ocr.geetest;

import com.pipeline.Filepipeline;
import com.pipeline.impl.SimpleFilepipeline;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Administrator on 2016/12/20.
 */
public class JframeTest extends JFrame implements KeyListener, MouseMotionListener {
    private static Filepipeline pip = new SimpleFilepipeline(new File("path.txt"));


    public JframeTest() {
        this.setTitle("鼠标移动测试");
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        System.out.println(String.format("%d %d %d", e.getX(), e.getY(), System.currentTimeMillis()));
        pip.printResult(String.format("%d %d %d", e.getX(), e.getY(), System.currentTimeMillis()), true);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public static void main(String[] args) {

        JframeTest test = new JframeTest();
//        JTextArea area = new JTextArea();
        JLabel area = new JLabel();
        final BufferedImage[] image = {null};
        try {
            image[0] = ImageIO.read(new File("temp", "full.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Random rd = new Random();
        final int[] i = {rd.nextInt(image[0].getWidth() - 20) + 20};
        System.out.println(String.format("第%d行为黑行", i[0]));
        for (int j = 0; j < image[0].getHeight(); j++) {
            image[0].setRGB(i[0], j, 0);
        }
        JButton refresh = new JButton("refresh");
        ImageIcon imageIcon = new ImageIcon(image[0]);
        refresh.addActionListener(e -> {
            try {
                image[0] = ImageIO.read(new File("temp", "full.jpg"));
                i[0] = rd.nextInt(image[0].getWidth() - 20) + 20;
                for (int j = 0; j < image[0].getHeight(); j++) {
                    image[0].setRGB(i[0], j, 0);
                }
                imageIcon.setImage(image[0]);
                System.out.println("黑行行数为： " + i[0]);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        FlowLayout flowLayout = new FlowLayout();
        area.setIcon(imageIcon);
        area.setSize(200, 200);
        area.setVisible(true);
//        flowLayout.addLayoutComponent("image", area);
//        flowLayout.addLayoutComponent("refresh", refresh);
        test.addKeyListener(test);
//        test.addMouseMotionListener(test);
        area.addMouseMotionListener(test);
        test.setLayout(flowLayout);
        test.add(area);
        test.add(refresh);
    }
}
