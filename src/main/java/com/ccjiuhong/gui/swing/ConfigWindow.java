package com.ccjiuhong.gui.swing;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;


/**
 * @author G. Seinfeld
 * @since 2019/07/01
 */
@Slf4j
class ConfigWindow extends JFrame {
    ConfigWindow() {
        setTitle("配置中心");

        setLocationRelativeTo(null);

        setSize(600, 480);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        add(jPanel);

        JLabel downloadDirLabel = new JLabel("下载地址：");
        downloadDirLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        jPanel.add(downloadDirLabel);

        JTextField downloadDirInput = new JTextField(20);
        downloadDirInput.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        jPanel.add(downloadDirInput);

        JButton downloadDirButton = new JButton("选择");
        downloadDirButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        downloadDirButton.addActionListener(e -> {
            JFrame frame = new JFrame();
            JFileChooser downloadDir = new JFileChooser();
            downloadDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            frame.getContentPane().add(downloadDir);
            frame.setSize(600, 480);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            frame.requestFocus();
        });
        jPanel.add(downloadDirButton);

        JButton submit = new JButton("保存更改");
        jPanel.add(submit);

        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            new ConfigWindow();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            log.error(e.getMessage(), e);
        }
    }
}