package com.ccjiuhong.gui.swing.frame;

import com.ccjiuhong.gui.swing.component.ContentPanel;
import com.ccjiuhong.gui.swing.component.SideBar;

import javax.swing.*;
import java.awt.*;

/**
 * 主窗体中的Panel容器
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class MainPanel extends JPanel {
    private final SideBar sideBar;
    private final ContentPanel contentPanel;
    private final MainFrame owner;

    public MainPanel(MainFrame owner) {
        this.owner = owner;

        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.3;
        c.weighty = 1.0;
        sideBar = new SideBar(owner, new GridBagLayout());
        gb.setConstraints(sideBar, c);
        add(sideBar);

        contentPanel = new ContentPanel();
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.7;
        c.weighty = 1.0;
        gb.setConstraints(contentPanel, c);
        add(contentPanel);
    }
}
