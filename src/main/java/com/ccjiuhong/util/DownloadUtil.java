package com.ccjiuhong.util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Base64;

/**
 * @author G.Seinfeld
 * @date 2019/06/29
 */
public class DownloadUtil {
    private static final String FILE_KEY = "rocket";

    public static String getName(String targetDirectory, String prefix, String suffix) {
        return getPath(targetDirectory) + prefix + FILE_KEY + suffix;
    }

    public static String getPath(String targetDirectory) {
        return targetDirectory + File.separator;
    }

    public static String getReadableSize(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
                + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getReadableSize(long bytes) {
        if (bytes <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(bytes
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    public static String getReadablePercent(double percent) {
        return new DecimalFormat("#0.##").format(percent) + "%";
    }

    public static String getReadableSpeed(long speed) {
        return getReadableSize(speed) + "/s";
    }

    public static String decodeIfNecessary(String url) {
        if (url.startsWith("thunder")) {
            String base64Code = url.substring(10);
            byte[] bytes = Base64.getDecoder().decode(base64Code);
            String decodedUrl = new String(bytes);
            return decodedUrl.substring(2, decodedUrl.length() - 2);
        }
        return url;
    }
}
