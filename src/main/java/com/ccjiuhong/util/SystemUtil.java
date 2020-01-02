package com.ccjiuhong.util;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * 调用系统工具
 *
 * @author G. Seinfeld
 * @since 2020/01/02
 */
@Slf4j
public final class SystemUtil {
    private SystemUtil() {

    }

    public static void openUrlFromBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            URI uri = URI.create(url);
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

}
