package com.ccjiuhong.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.util.Optional;

/**
 * 处理FTP的工具类
 *
 * @author G. Seinfeld
 * @since 2020/01/08
 */
@Slf4j
public final class FtpUtil {
    /**
     * 缓存区
     */
    private static final int BUFFER_SIZE = 1 << 22; // 4M

    /**
     * 本地字符编码
     **/
    private static String localCharset = "GBK";

    /**
     * FTP协议里面，规定文件名编码为iso-8859-1
     **/
    private static String serverCharset = "ISO-8859-1";

    /**
     * UTF-8字符编码
     **/
    private static final String CHARSET_UTF8 = "UTF-8";

    /**
     * OPTS UTF8字符串常量
     **/
    private static final String OPTS_UTF8 = "OPTS UTF8";

    private FtpUtil() {

    }

//    /**
//     * 下载该目录下所有文件到本地
//     *
//     * @param ftpPath  FTP服务器上的相对路径，例如：test/123
//     * @param savePath 保存文件到本地的路径，例如：D:/test
//     * @return 成功返回true，否则返回false
//     */
//    public boolean downloadFiles(String ftpPath, String savePath) {
//        // 登录
//        login(ftpAddress, ftpPort, ftpUsername, ftpPassword);
//        if (ftpClient != null) {
//            try {
//                String path = changeEncoding(basePath + ftpPath);
//                // 判断是否存在该目录
//                if (!ftpClient.changeWorkingDirectory(path)) {
//                    log.error(basePath + ftpPath + DIR_NOT_EXIST);
//                    return Boolean.FALSE;
//                }
//                ftpClient.enterLocalPassiveMode();  // 设置被动模式，开通一个端口来传输数据
//                String[] fs = ftpClient.listNames();
//                // 判断该目录下是否有文件
//                if (fs == null || fs.length == 0) {
//                    log.error(basePath + ftpPath + DIR_CONTAINS_NO_FILE);
//                    return Boolean.FALSE;
//                }
//                for (String ff : fs) {
//                    String ftpName = new String(ff.getBytes(serverCharset), localCharset);
//                    File file = new File(savePath + '/' + ftpName);
//                    try (OutputStream os = new FileOutputStream(file)) {
//                        ftpClient.retrieveFile(ff, os);
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                    }
//                }
//            } catch (IOException e) {
//                log.error("下载文件失败", e);
//            } finally {
//                closeConnect();
//            }
//        }
//        return Boolean.TRUE;
//    }

    /**
     * 连接FTP服务器
     *
     * @param address  地址，如：127.0.0.1
     * @param port     端口，如：21
     * @param username 用户名，如：root
     * @param password 密码，如：root
     */
    public static Optional<FTPClient> login(String address, int port, String username, String password) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(address, port);
            ftpClient.login(username, password);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            //限制缓冲区大小
            ftpClient.setBufferSize(BUFFER_SIZE);
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                closeConnect(ftpClient);
                log.warn("FTP服务器连接失败");
                return Optional.empty();
            }
            return Optional.of(ftpClient);
        } catch (Exception e) {
            log.error("FTP登录失败", e);
            return Optional.empty();
        }
    }


    /**
     * FTP服务器路径编码转换
     *
     * @param ftpPath FTP服务器路径
     * @return String
     */
    public static String changeEncoding(FTPClient ftpClient, String ftpPath) {
        if (ftpPath == null) {
            return null;
        }
        String pathname = null;
        try {
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(OPTS_UTF8, "ON"))) {
                localCharset = CHARSET_UTF8;
            }
            pathname = new String(ftpPath.getBytes(localCharset), serverCharset);
        } catch (Exception e) {
            log.error("路径编码转换失败", e);
        }
        return pathname;
    }

    /**
     * 关闭FTP连接
     */
    public static void closeConnect(FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                log.error("关闭FTP连接失败", e);
            }
        }
    }

    /**
     * 检查指定目录下是否含有指定文件
     *
     * @param ftpPath  FTP服务器文件相对路径，例如：test/123
     * @param fileName 要下载的文件名，例如：test.txt
     * @return 成功返回true，否则返回false
     */
    public static boolean checkFileInFtp(FTPClient ftpClient, String ftpPath, String fileName) {
        if (ftpClient != null) {
            try {
                String path = changeEncoding(ftpClient, ftpPath);
                // 判断是否存在该目录
                if (!ftpClient.changeWorkingDirectory(path)) {
                    log.error(ftpPath + "不存在");
                    return Boolean.FALSE;
                }
                ftpClient.enterLocalPassiveMode();  // 设置被动模式，开通一个端口来传输数据
                String[] fs = ftpClient.listNames();
                // 判断该目录下是否有文件
                if (fs == null || fs.length == 0) {
                    log.error(ftpPath + "目录中没有文件");
                    return Boolean.FALSE;
                }
                for (String ff : fs) {
                    String ftpName = new String(ff.getBytes(serverCharset), localCharset);
                    if (ftpName.equals(fileName)) {
                        return Boolean.TRUE;
                    }
                }
            } catch (IOException e) {
                log.error("请求出错", e);
            } finally {
                closeConnect(ftpClient);
            }
        }
        return Boolean.TRUE;
    }

//    /**
//     * 下载该目录下所有文件到本地 根据实际需要修改执行逻辑
//     *
//     * @param ftpPath  FTP服务器上的相对路径，例如：test/123
//     * @param savePath 保存文件到本地的路径，例如：D:/test
//     * @return 成功返回true，否则返回false
//     */
//    public Map<String, Object> downLoadTableFile(String ftpPath, String savePath) {
//        // 登录
//        login(ftpAddress, ftpPort, ftpUsername, ftpPassword);
//        Map<String, Object> resultMap = new HashMap<>();
//        if (ftpClient != null) {
//            try {
//                String path = changeEncoding(basePath + "/" + ftpPath);
//                // 判断是否存在该目录
//                if (!ftpClient.changeWorkingDirectory(path)) {
//                    log.error(basePath + "/" + ftpPath + DIR_NOT_EXIST);
//                    resultMap.put("result", false);
//                    return resultMap;
//                }
//                ftpClient.enterLocalPassiveMode();  // 设置被动模式，开通一个端口来传输数据
//                String[] fs = ftpClient.listNames();
//                // 判断该目录下是否有文件
//                if (fs == null || fs.length == 0) {
//                    log.error(basePath + "/" + ftpPath + DIR_CONTAINS_NO_FILE);
//                    resultMap.put("result", false);
//                    return resultMap;
//                }
//                List<String> tableFileNameList = new ArrayList<>();
//                //根据表名创建文件夹
//                String tableDirName = savePath + "/" + ftpPath;
//                File tableDirs = new File(tableDirName);
//                if (!tableDirs.exists()) {
//                    tableDirs.mkdirs();
//                }
//                for (String ff : fs) {
//                    String ftpName = new String(ff.getBytes(serverCharset), localCharset);
//                    File file = new File(tableDirName + "/" + ftpName);
//                    //存储文件名导入时使用
//                    tableFileNameList.add(tableDirName + "/" + ftpName);
//                    try (OutputStream os = new FileOutputStream(file)) {
//                        ftpClient.retrieveFile(ff, os);
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                    }
//                }
//                resultMap.put("fileNameList", tableFileNameList);
//                resultMap.put("result", true);
//                return resultMap;
//            } catch (IOException e) {
//                log.error("下载文件失败", e);
//            } finally {
//                closeConnect();
//            }
//        }
//        resultMap.put("result", false);
//        return resultMap;
//    }

}
