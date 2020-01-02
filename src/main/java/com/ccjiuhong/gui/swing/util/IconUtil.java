package com.ccjiuhong.gui.swing.util;

import com.ccjiuhong.gui.swing.IconBar;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author G. Seinfeld
 * @since 2020/01/02
 */
@Slf4j
public final class IconUtil {

    private IconUtil() {

    }

    public static Icon newImageIcon(String resourcePath, int width, int height) {
        InputStream is = IconBar.class.getClassLoader().getResourceAsStream(resourcePath);
        try {
            BufferedImage srcImage = ImageIO.read(Objects.requireNonNull(is));

            // 将图片缩放
            BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = destImage.getGraphics();
            g.drawImage(srcImage, 0, 0, width, height, null);

            // 将图片反白
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel = destImage.getRGB(j, i);
                    // 异或0为保持原样，异或1为按位取反
                    destImage.setRGB(j, i, pixel ^ 0x00FFFFFF);
                }
            }
            return new ImageIcon(destImage);
        } catch (IOException | NullPointerException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
