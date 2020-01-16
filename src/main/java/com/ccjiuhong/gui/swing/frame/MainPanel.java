package com.ccjiuhong.gui.swing.frame;

import com.ccjiuhong.gui.swing.component.ContentPanel;
import com.ccjiuhong.gui.swing.component.IconBar;
import com.ccjiuhong.gui.swing.component.SideMenuBar;

import javax.swing.*;
import java.awt.*;

/**
 * 主窗体中的Panel容器
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class MainPanel extends JPanel {

    private static MainPanel instance;

    public static MainPanel getInstance() {
        if (instance == null) {
            instance = new MainPanel();
        }
        return instance;
    }

    private final IconBar iconBar;
    private final SideMenuBar sideMenuBar;
    private final ContentPanel contentPanel;

    private MainPanel() {

        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.3 * 0.25;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        iconBar = IconBar.getInstance();
        iconBar.setBackground(Color.BLACK);
        gb.setConstraints(iconBar, c);
        add(iconBar);


        c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.weightx = 0.3 * 0.75;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        sideMenuBar = SideMenuBar.getInstance();
        gb.setConstraints(sideMenuBar, c);
        add(sideMenuBar);
//
//        sideBar = SideBar.getInstance();
//        gb.setConstraints(sideBar, c);
//        add(sideBar);

        contentPanel = ContentPanel.getInstance();
        contentPanel.setBackground(Color.BLUE);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.7;
        c.weighty = 1.0;
        gb.setConstraints(contentPanel, c);
        add(contentPanel);
    }


    public ContentPanel getContentPanel() {
        return contentPanel;
    }
}
