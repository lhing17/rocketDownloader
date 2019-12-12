package com.ccjiuhong.download;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author G. Seinfeld
 * @date 2019/06/28
 */
@Slf4j
public class DownloadThreadPool extends ThreadPoolExecutor {

    /**
     * 缓存任务执行状态的Map，出于线程安全考虑，使用了ConcurrentHashMap
     */
    private static Map<Integer, Queue<Future<?>>> futureMap = new ConcurrentHashMap<>();

    /**
     * 缓存Future(线程执行结果)与Runnable之间对应关系的Map，出于线程安全考虑，使用了ConcurrentHashMap
     */
    private static Map<Future<?>, Runnable> runnableMap = new ConcurrentHashMap<>();

    public DownloadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (r instanceof DownloadRunnable) {
            DownloadRunnable dr = (DownloadRunnable) r;
        }
    }

    /**
     * 通过线程池暂停任务
     *
     * @param missionId 任务ID
     */
    public void pause(int missionId) {
        for (Future<?> future : futureMap.get(missionId)) {
            future.cancel(true);
        }
    }

    /**
     * 通过线程池取消任务
     * @param missionId 任务ID
     */
    public void cancel(int missionId) {
        for (Future<?> future : futureMap.get(missionId)) {
            runnableMap.remove(future);
            future.cancel(true);
        }
        futureMap.remove(missionId);
    }

    @NotNull
    @Override
    public Future<?> submit(Runnable task) {
        Future<?> future = super.submit(task);
        if (task instanceof DownloadRunnable) {
            int missionId = ((DownloadRunnable) task).getMission().getMissionId();
            runnableMap.put(future, task);
            futureMap.computeIfAbsent(missionId, f -> new ConcurrentLinkedQueue<>()).offer(future);
        } else {
            String warnMessage = "当前提交的线程不是DownloadRunnable实例";
            throw new IllegalStateException(warnMessage);
        }
        return future;
    }
}
