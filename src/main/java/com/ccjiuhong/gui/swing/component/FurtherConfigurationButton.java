package com.ccjiuhong.gui.swing.component;

/**
 * 进阶配置按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class FurtherConfigurationButton extends ImageTextButton{
    private static FurtherConfigurationButton instance;

    public static FurtherConfigurationButton getInstance(){
        if (instance == null){
            instance = new FurtherConfigurationButton();
        }
        return instance;
    }

    private FurtherConfigurationButton(){
        super("icons/about.png", "进阶设置");
    }
}
