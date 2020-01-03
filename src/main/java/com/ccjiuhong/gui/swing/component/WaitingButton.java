package com.ccjiuhong.gui.swing.component;

/**
 * 等待中按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class WaitingButton extends ImageTextButton{
    private static WaitingButton instance;

    public static WaitingButton getInstance(){
        if (instance == null){
            instance = new WaitingButton();
        }
        return instance;
    }

    private WaitingButton(){
        super("icons/pause.png", "等待中");

    }
}
