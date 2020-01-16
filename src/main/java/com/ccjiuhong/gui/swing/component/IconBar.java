package com.ccjiuhong.gui.swing.component;

import com.ccjiuhong.gui.swing.DownloaderSwing;
import com.ccjiuhong.mgt.DefaultDownloadManager;
import com.ccjiuhong.mgt.DownloadManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图标按钮条
 *
 * @author G. Seinfeld
 * @since 2019/12/30
 */
public class IconBar extends JPanel {
    /**
     * 图标按钮条上面的几个按钮
     */
    private List<JButton> leadingIcons;
    /**
     * 图标按钮条下面的几个按钮
     */
    private List<JButton> tailIcons;
    private static IconBar instance;

    public static IconBar getInstance() {
        if (instance == null) {
            instance = new IconBar();
        }
        return instance;
    }

    private IconBar() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        leadingIcons = new ArrayList<>();
        tailIcons = new ArrayList<>();

        JButton missionListButton = new ImageButton("icons/list.png", 30, 30, true);
        missionListButton.addActionListener(e -> {
            SideMenuBar sideMenuBar = SideMenuBar.getInstance();
            sideMenuBar.getTitle().setText("任务列表");
            for (JButton button : sideMenuBar.getButtons()) {
                sideMenuBar.remove(button);
            }
            sideMenuBar.addImageButtons(DownloadingButton.getInstance(),
                    WaitingButton.getInstance(), CompletedButton.getInstance());
        });

        JButton addButton = new ImageButton("icons/add.png", 30, 30, true);
        addButton.addActionListener(e -> {
            String fileUrl = JOptionPane.showInputDialog(DownloaderSwing.languageConfig.getString("inputAddress"));
            if (fileUrl != null) {
                DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();
                String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                int missionId = defaultDownloadManager.addMission(fileUrl, DownloaderSwing.jsonConfig.getString("downloadDirectory"), targetFileName);
                defaultDownloadManager.startOrResumeMission(missionId);
            }
        });

        addLeadingIcons(missionListButton, addButton);

        add(Box.createVerticalGlue());


        // 配置按钮
        JButton configurationButton = new ImageButton("icons/config.png", 30, 30, true);
        configurationButton.addActionListener(e -> {
            SideMenuBar sideMenuBar = SideMenuBar.getInstance();
            sideMenuBar.getTitle().setText("偏好设置");
            for (JButton button : sideMenuBar.getButtons()) {
                sideMenuBar.remove(button);
            }
            sideMenuBar.addImageButtons(GeneralConfigurationButton.getInstance(),
                    FurtherConfigurationButton.getInstance(), ExperimentalButton.getInstance());
        });

        // 关于我们按钮
        JButton aboutButton = AboutButton.getInstance();
        addTailIcons(configurationButton, aboutButton);
    }


    public void addLeadingIcons(JButton... leadingIconArray) {
        for (JButton leadingIcon : leadingIconArray) {
            leadingIcons.add(leadingIcon);
            this.add(leadingIcon);
        }
    }

    public void addTailIcons(JButton... tailIconArray) {
        for (JButton tailIcon : tailIconArray) {
            tailIcons.add(tailIcon);
            this.add(tailIcon);
        }
    }
}
