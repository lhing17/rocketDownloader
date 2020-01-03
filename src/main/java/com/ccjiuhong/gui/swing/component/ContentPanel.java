package com.ccjiuhong.gui.swing.component;

import javax.swing.*;
import java.awt.*;

/**
 * 内容面板
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class ContentPanel extends JPanel {
    private static ContentPanel instance;

    public static ContentPanel getInstance() {
        if (instance == null) {
            instance = new ContentPanel();
        }
        return instance;
    }

    private ContentPanel() {
        setBackground(Color.WHITE);
    }
}
