package com.ccjiuhong.gui.swing.frame;

import com.ccjiuhong.gui.common.Configuration;

import javax.swing.*;
import java.awt.*;

/**
 * 主窗体，通过单例模式来保证全局唯一。
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class MainFrame extends JFrame {

    private static MainFrame instance;

    public static MainFrame getInstance() {
        if (instance == null) {
            instance = new MainFrame();
        }
        return instance;
    }

    private MainFrame() throws HeadlessException {// 设置标题
        setTitle(Configuration.TITLE);
        // 设置尺寸
        setSize(Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGHT);
        // 在屏幕中央显示
        setLocationRelativeTo(null);
        // 窗口关闭后自动停止程序
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
