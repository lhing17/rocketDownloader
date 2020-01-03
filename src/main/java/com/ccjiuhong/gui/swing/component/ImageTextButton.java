package com.ccjiuhong.gui.swing.component;

/**
 * 既带有图标，又带有文字的按钮
 *
 * @author G. Seinfeld
 * @since 2020/01/03
 */
public class ImageTextButton extends ImageButton {

    private static final int DEFAULT_WIDTH = 15;
    private static final int DEFAULT_HEIGHT = 15;

    public ImageTextButton(String path, int width, int height, boolean invertColor, String text) {
        super(path, width, height, invertColor);
        setText(text);
    }

    public ImageTextButton(String path, int width, int height, String text) {
        super(path, width, height);
        setText(text);
    }

    public ImageTextButton(String path, String text) {
        this(path, DEFAULT_WIDTH, DEFAULT_HEIGHT, text);
    }
}
