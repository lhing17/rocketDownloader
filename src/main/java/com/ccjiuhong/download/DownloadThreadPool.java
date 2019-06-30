package com.ccjiuhong.download;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author G. Seinfeld
 * @date 2019/06/28
 */
public class DownloadThreadPool extends ThreadPoolExecutor {
    public DownloadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (r instanceof DownloadRunnable) {
            DownloadRunnable dr = (DownloadRunnable) r;
        }
    }
}
