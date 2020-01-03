package com.ccjiuhong.gui.swing.component;

import com.ccjiuhong.gui.swing.frame.MainFrame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author G. Seinfeld
 * @since 2019/12/30
 */
public class SideMenuBar extends JPanel {

    MainFrame owner;
    private JLabel title;
    private List<JButton> buttons;

    public SideMenuBar(MainFrame owner) {
        this.owner = owner;
        buttons = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        title = new JLabel("任务列表");
        add(title);

        ImageButton downloading = new ImageTextButton("icons/start.png", "下载中");
        addImageButton(downloading);

        ImageButton waiting = new ImageTextButton("icons/pause.png", "等待中");
        addImageButton(waiting);

        ImageButton complete = new ImageTextButton("icons/stop.png", "已停止");
        addImageButton(complete);

    }

    private void addImageButton(ImageButton downloading) {
        add(downloading);
        buttons.add(downloading);
    }
}
