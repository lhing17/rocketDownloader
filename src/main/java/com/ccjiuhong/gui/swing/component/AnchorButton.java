package com.ccjiuhong.gui.swing.component;

import com.ccjiuhong.util.SystemUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author G. Seinfeld
 * @since 2020/01/02
 */
public class AnchorButton extends JButton {
    public AnchorButton(String text, String href) {
        super(text);
        addActionListener(e -> SystemUtil.openUrlFromBrowser(href));
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
