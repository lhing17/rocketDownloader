package com.ccjiuhong.gui.swing.component;

/**
 * 已完成按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class CompletedButton extends ImageTextButton {
    private static CompletedButton instance;

    public static CompletedButton getInstance() {
        if (instance == null) {
            instance = new CompletedButton();
        }
        return instance;
    }

    private CompletedButton() {
        super("icons/stop.png", "已停止");
    }
}
