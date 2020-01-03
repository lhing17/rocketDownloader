package com.ccjiuhong.gui.swing.component;

import javax.swing.*;
import java.awt.*;

/**
 * 左侧边条
 *
 * @author G. Seinfeld
 * @since 2019/12/30
 */
public class SideBar extends JPanel {

    private final IconBar iconBar;
    private final SideMenuBar sideMenuBar;

    public SideBar(GridBagLayout gridBagLayout) {
        super(gridBagLayout);

        iconBar = new IconBar( this);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.25;
        c.weighty = 1.0;
        iconBar.setBackground(Color.BLACK);
        gridBagLayout.setConstraints(iconBar, c);
        this.add(iconBar);


        sideMenuBar = new SideMenuBar();
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.75;
        c.weighty = 1.0;
        gridBagLayout.setConstraints(sideMenuBar, c);
        this.add(sideMenuBar);
    }

    public IconBar getIconBar() {
        return iconBar;
    }

    public SideMenuBar getSideMenuBar() {
        return sideMenuBar;
    }
}
