package com.ccjiuhong.gui.swing;

import com.ccjiuhong.gui.common.Configuration;

import javax.swing.*;
import java.awt.*;

import static com.ccjiuhong.gui.common.Configuration.*;

/**
 * Swing版本的下载器GUI
 *
 * @author G.Seinfeld
 * @date 2019/06/30
 */
public class DownloaderSwing extends JFrame {

    public DownloaderSwing() throws HeadlessException {
        // 设置标题
        setTitle(TITLE);

        // 设置尺寸
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        //在屏幕中央显示
        setLocationRelativeTo(null);

        //窗口关闭后自动停止程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        add(jPanel);

        JButton jButton = new JButton();
        jButton.setText("ABC");
        jPanel.add(jButton);


        setVisible(true);
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        new DownloaderSwing();
    }
}
