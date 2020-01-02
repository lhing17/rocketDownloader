package com.ccjiuhong.gui.swing;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * 左侧边条
 *
 * @author G. Seinfeld
 * @since 2019/12/30
 */
public class SideBar extends JPanel {

    JFrame owner;

    public SideBar(JFrame owner, GridBagLayout gridBagLayout) throws IOException {
        super(gridBagLayout);
        this.owner = owner;

        IconBar iconBar = new IconBar(owner);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.25;
        c.weighty = 1.0;
        iconBar.setBackground(Color.BLACK);
        gridBagLayout.setConstraints(iconBar, c);
        this.add(iconBar);


        SideMenuBar sideMenuBar = new SideMenuBar();
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.75;
        c.weighty = 1.0;
        gridBagLayout.setConstraints(sideMenuBar, c);
        this.add(sideMenuBar);
    }
}
