package com.ccjiuhong.gui.swing.component;

/**
 * 下载中的按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class DownloadingButton extends ImageTextButton{
    private static DownloadingButton instance;

    public static DownloadingButton getInstance(){
        if (instance == null){
            instance = new DownloadingButton();
        }
        return instance;
    }

    private DownloadingButton(){
        super("icons/start.png", "下载中");
    }
}
