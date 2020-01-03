package com.ccjiuhong.gui.swing.component;

import com.ccjiuhong.gui.swing.aboutus.AboutUs;

/**
 * 图标按钮条上关于我们的按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public final class AboutButton extends ImageButton {
    private static AboutButton instance;

    public static AboutButton getInstance() {
        if (instance == null) {
            instance = new AboutButton();
        }
        return instance;
    }

    private AboutButton() {
        super("icons/about.png", 30, 30, true);

        addActionListener(e -> {
            AboutUs aboutUs = new AboutUs("关于我们", true);
            aboutUs.setVisible(true);
        });
    }
}
