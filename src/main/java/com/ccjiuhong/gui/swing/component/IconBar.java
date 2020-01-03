package com.ccjiuhong.gui.swing.component;

import com.ccjiuhong.gui.swing.aboutus.AboutUs;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author G. Seinfeld
 * @since 2019/12/30
 */
public class IconBar extends JPanel {
    private List<JButton> leadingIcons;
    private List<JButton> tailIcons;
    private JFrame owner;

    public IconBar(JFrame owner) {

        this.owner = owner;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        leadingIcons = new ArrayList<>();
        tailIcons = new ArrayList<>();

        JButton missionListButton = new ImageButton("icons/list.png", 30, 30, true);
        JButton addButton = new ImageButton("icons/add.png", 30, 30, true);

        addLeadingIcons(missionListButton, addButton);

        add(Box.createVerticalGlue());

        JButton configurationButton = new ImageButton("icons/config.png", 30, 30, true);

        // 关于我们按钮
        JButton aboutButton = buildAboutButton(owner);

        addTailIcons(configurationButton, aboutButton);
    }

    @NotNull
    private JButton buildAboutButton(JFrame owner) {
        JButton aboutButton = new ImageButton("icons/about.png", 30, 30, true);

        aboutButton.addActionListener(e -> {
            AboutUs aboutUs = new AboutUs(owner, "关于我们", true);
            aboutUs.setVisible(true);
        });
        return aboutButton;
    }


    public void addLeadingIcons(JButton... leadingIconArray) {
        for (JButton leadingIcon : leadingIconArray) {
            leadingIcons.add(leadingIcon);
            this.add(leadingIcon);
        }
    }

    public void addTailIcons(JButton... tailIconArray) {
        for (JButton tailIcon : tailIconArray) {
            leadingIcons.add(tailIcon);
            this.add(tailIcon);
        }
    }
}
