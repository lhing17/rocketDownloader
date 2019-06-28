package com.ccjiuhong.download;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author G. Seinfeld
 * @date 2019/06/27
 */
public class MissionMonitor {
    /**
     * 已经下载的字节数，这里考虑到线程安全的问题，使用JUC包中的AtomicInteger，确保计算的原子性
     */
    private AtomicInteger downloadedSize = new AtomicInteger();

}
