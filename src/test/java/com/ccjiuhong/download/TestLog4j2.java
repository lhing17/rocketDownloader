package com.ccjiuhong.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author G. Seinfeld
 * @date 2019/06/28
 */
public class TestLog4j2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestLog4j2.class);

    public static void main(String[] args) {
        LOGGER.info("this is a test message");
    }
}
