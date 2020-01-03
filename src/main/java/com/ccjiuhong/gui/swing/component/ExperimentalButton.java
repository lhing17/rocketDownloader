package com.ccjiuhong.gui.swing.component;

/**
 * 实验室按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class ExperimentalButton extends ImageTextButton {
    private static ExperimentalButton instance;

    public static ExperimentalButton getInstance() {
        if (instance == null) {
            instance = new ExperimentalButton();
        }
        return instance;
    }

    private ExperimentalButton() {
        super("icons/about.png", "实验室");
    }
}
