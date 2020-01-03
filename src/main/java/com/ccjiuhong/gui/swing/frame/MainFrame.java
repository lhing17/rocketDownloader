package com.ccjiuhong.gui.swing.frame;

import com.ccjiuhong.gui.common.Configuration;

import javax.swing.*;
import java.awt.*;

/**
 * 主窗体
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class MainFrame extends JFrame {

    public MainFrame() throws HeadlessException {// 设置标题
        setTitle(Configuration.TITLE);
        // 设置尺寸
        setSize(Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGHT);
        // 在屏幕中央显示
        setLocationRelativeTo(null);
        // 窗口关闭后自动停止程序
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        MainPanel mainPanel = new MainPanel(this);
        add(mainPanel);

    }
}
