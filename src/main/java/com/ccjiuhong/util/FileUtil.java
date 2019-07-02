package com.ccjiuhong.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author G. Seinfeld
 * @date 2019/07/02
 */
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
}
