package com.ccjiuhong.gui.swing.aboutus;

import javax.swing.*;
import java.awt.*;

/**
 * 关于我们的弹出框
 *
 * @author G. Seinfeld
 * @since 2020/01/02
 */
public class AboutUs extends JDialog {

    public AboutUs(Frame owner, String title, boolean modal) {
        super(owner, title, modal);

        this.setSize(800, 400);
        this.setLocationRelativeTo(owner);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel jPanel = new JPanel(new BorderLayout());

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        JPanel centerLeftPanel = new JPanel(new GridLayout(5, 2));
        centerLeftPanel.setBackground(Color.WHITE);
        JLabel engine = new JLabel("引擎版本 1.34.0");
        centerLeftPanel.add(engine);

        centerLeftPanel.add(new JLabel("Async DNS"));
        centerLeftPanel.add(new JLabel("BitTorrent"));
        centerLeftPanel.add(new JLabel("Firefox3 Cookie"));
        centerLeftPanel.add(new JLabel("GZip"));
        centerLeftPanel.add(new JLabel("HTTPS"));
        centerLeftPanel.add(new JLabel("Message Digest"));
        centerLeftPanel.add(new JLabel("Metalink"));
        centerLeftPanel.add(new JLabel("XML-RPC"));
        centerLeftPanel.add(new JLabel("SFTP"));

        JPanel centerRightPanel = new JPanel();
        JTextField engine1 = new JTextField("test");
        centerRightPanel.add(engine1);
//
//        Async DNS BitTorrent Firefox3 Cookie GZip HTTPS Message Digest Metalink XML-RPC SFTP

        centerPanel.add(centerLeftPanel);
        centerPanel.add(centerRightPanel);

        jPanel.add(centerPanel);

        JPanel bottomPanel = new AboutUsBottom();
        jPanel.add(bottomPanel, BorderLayout.SOUTH);


        this.setContentPane(jPanel);
    }
}
