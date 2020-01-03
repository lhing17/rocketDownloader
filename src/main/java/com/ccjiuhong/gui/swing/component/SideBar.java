package com.ccjiuhong.gui.swing.component;

import com.ccjiuhong.gui.swing.frame.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * 左侧边条
 *
 * @author G. Seinfeld
 * @since 2019/12/30
 */
public class SideBar extends JPanel {

    MainFrame owner;

    public SideBar(MainFrame owner, GridBagLayout gridBagLayout) {
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


        SideMenuBar sideMenuBar = new SideMenuBar(owner);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.75;
        c.weighty = 1.0;
        gridBagLayout.setConstraints(sideMenuBar, c);
        this.add(sideMenuBar);
    }
}
