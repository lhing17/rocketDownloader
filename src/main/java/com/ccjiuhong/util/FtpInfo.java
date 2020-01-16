package com.ccjiuhong.util;

import lombok.Data;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 储存FTP URL中解析出的信息
 *
 * @author G. Seinfeld
 * @since 2020/01/08
 */
@Data
public class FtpInfo {

    static final int NONE = 0;
    static final int ASCII = 1;
    static final int BIN = 2;
    static final int DIR = 3;
    /**
     * FTP用户名
     */
    private String username;
    /**
     * FTP密码
     */
    private String password;
    /**
     * FTP主机
     */
    private String host;
    /**
     * FTP端口
     */
    private int port;
    /**
     * FTP路径
     */
    private String fullpath;
    /**
     * FTP传输类型
     */
    private int type;
    /**
     * 路径名
     */
    private String pathname;
    /**
     * 文件名
     */
    private String filename;

    public static FtpInfo getInstance(String ftpUrl) throws MalformedURLException {
        FtpInfo ftpInfo = new FtpInfo();
        URL url = new URL(ftpUrl);
        String userInfo = url.getUserInfo();
        ftpInfo.setHost(url.getHost());
        ftpInfo.setPort(url.getPort());
        decodePath(ftpInfo, url.getPath());
        if (userInfo != null) { // get the user and password
            int delimiter = userInfo.indexOf(':');
            if (delimiter == -1) {
                ftpInfo.setUsername(URLDecoder.decode(userInfo, UTF_8));
            } else {
                ftpInfo.setUsername(URLDecoder.decode(userInfo.substring(0, delimiter++), UTF_8));
                ftpInfo.setPassword(URLDecoder.decode(userInfo.substring(delimiter), UTF_8));
            }
        }
        return ftpInfo;
    }

    /*
     * Decodes the path as per the RFC-1738 specifications.
     */
    private static void decodePath(FtpInfo ftpInfo, String path) {
        int i = path.indexOf(";type=");
        if (i >= 0) {
            String s1 = path.substring(i + 6);
            if ("i".equalsIgnoreCase(s1)) {
                ftpInfo.setType(BIN);
            }
            if ("a".equalsIgnoreCase(s1)) {
                ftpInfo.setType(ASCII);
            }
            if ("d".equalsIgnoreCase(s1)) {
                ftpInfo.setType(DIR);
            }
            path = path.substring(0, i);
        }
        if (path.length() > 1 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.length() == 0) {
            path = "./";
        }
        String filename, pathname;
        if (!path.endsWith("/")) {
            i = path.lastIndexOf('/');
            if (i > 0) {
                filename = path.substring(i + 1);
                filename = URLDecoder.decode(filename, UTF_8);
                pathname = path.substring(0, i);
            } else {
                filename = URLDecoder.decode(path, UTF_8);
                pathname = null;
            }
        } else {
            pathname = path.substring(0, path.length() - 1);
            filename = null;
        }
        ftpInfo.setPathname(pathname);
        ftpInfo.setFilename(filename);
        if (pathname != null) {
            ftpInfo.setFullpath(pathname + "/" + (filename != null ? filename : ""));
        } else {
            ftpInfo.setFullpath(filename);
        }
    }
}
