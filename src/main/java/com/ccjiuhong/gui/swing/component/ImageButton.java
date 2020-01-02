package com.ccjiuhong.gui.swing.component;

import com.ccjiuhong.gui.swing.util.IconUtil;

import javax.swing.*;

/**
 * 背景为图片的按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/02
 */
public class ImageButton extends JButton {
    public ImageButton(String path, int width, int height) {
        setContentAreaFilled(false);
        setIcon(IconUtil.newImageIcon(path, width, height));
    }
}
