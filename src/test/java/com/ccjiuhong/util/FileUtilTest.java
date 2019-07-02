package com.ccjiuhong.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author G. Seinfeld
 * @date 2019/07/02
 */
public class FileUtilTest {

    @Test
    public void getFileMd5() throws IOException {
        String md5 = FileUtil.getFileMd5("F:\\rocketDownloader\\QQ9.1.5.25530.exe");
        System.out.println(md5);
    }
}