package com.ccjiuhong.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;

/**
 * @author G. Seinfeld
 * @since 2019/07/02
 */
@Slf4j
public final class FileUtil {
    private FileUtil() {
    }

    /**
     * 获取文件MD5值，用于校验下载的文件是否正确，也可以做文件字典
     *
     * @param filePath 文件路径
     * @return 文件MD5值
     * @throws IOException 文件未找到等原因引起IO异常
     */
    public static String getFileMd5(String filePath) throws IOException {
        return DigestUtils.md5Hex(new FileInputStream(filePath));
    }

    /**
     * 将全部文件内容读成一个字符串
     * @param file 要读取的文件
     * @return 文件内容的字符串
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String readText(File file) {
        String encoding = "UTF-8";
        long filelength = file.length();
        byte[] filecontent = new byte[(int) filelength];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(filecontent);
            return new String(filecontent, encoding);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将字符串一次性写入文件中
     * @param file 要写入内容的目标文件
     * @param text 要写入的字符串
     */
    public static void writeText(File file, String text) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(text);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
