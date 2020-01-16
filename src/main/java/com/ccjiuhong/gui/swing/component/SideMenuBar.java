package com.ccjiuhong.gui.swing.component;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author G. Seinfeld
 * @since 2019/12/30
 */
public class SideMenuBar extends JPanel {

    private final JLabel title;
    private final List<JButton> buttons;
    private static SideMenuBar instance;

    public static SideMenuBar getInstance() {
        if (instance == null) {
            instance = new SideMenuBar();
        }
        return instance;
    }

    private SideMenuBar() {

        buttons = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        title = new JLabel("任务列表");
        add(title);

        ImageButton downloading = DownloadingButton.getInstance();
        ImageButton waiting = WaitingButton.getInstance();
        ImageButton complete = CompletedButton.getInstance();
        addImageButtons(downloading, waiting, complete);

    }

    public void addImageButtons(ImageButton... imageButtons) {
        for (ImageButton imageButton : imageButtons) {
            add(imageButton);
            buttons.add(imageButton);
        }
    }

    public JLabel getTitle() {
        return title;
    }

    public List<JButton> getButtons() {
        return buttons;
    }
}
