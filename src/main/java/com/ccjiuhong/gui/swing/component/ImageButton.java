package com.ccjiuhong.gui.swing.component;

import com.ccjiuhong.gui.swing.util.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 背景为图片的按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/02
 */
public class ImageButton extends JButton {
    public ImageButton(String path, int width, int height, boolean invertColor) {
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setIcon(IconUtil.newImageIcon(path, width, height, invertColor));
    }

    public ImageButton(String path, int width, int height) {
        this(path, width, height, false);
    }

}
