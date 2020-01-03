package com.ccjiuhong.gui.swing.component;

/**
 * 基础设置的按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class GeneralConfigurationButton extends ImageTextButton {

    private static GeneralConfigurationButton instance;

    public static GeneralConfigurationButton getInstance() {
        if (instance == null) {
            instance = new GeneralConfigurationButton();
        }
        return instance;
    }

    private GeneralConfigurationButton() {
        super("icons/about.png", "基础设置");
    }
}
