package com.ccjiuhong.gui.swing.aboutus;

import com.ccjiuhong.gui.swing.component.AnchorButton;

import javax.swing.*;

/**
 * @author G. Seinfeld
 * @since 2020/01/02
 */
public class AboutUsBottom extends JPanel {
    public AboutUsBottom() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JButton copyright = new AnchorButton("©2020 G_Seinfeld", "https://github.com/lhing17");
        add(copyright);

        add(Box.createHorizontalGlue());

        JButton license = new AnchorButton("开源许可", "https://github.com/lhing17");
        add(license);

        JButton aboutUs = new AnchorButton("关于我们", "https://github.com/lhing17");
        add(aboutUs);

        JButton help = new AnchorButton("帮助支持", "https://github.com/lhing17");
        add(help);

        JButton changeLog = new AnchorButton("更新日志", "https://github.com/lhing17");
        add(changeLog);
    }
}
