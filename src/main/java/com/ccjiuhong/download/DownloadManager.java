package com.ccjiuhong.download;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 下载管理器，封装了所有下载需要用到的方法，是GUI访问逻辑代码的唯一入口类。
 *
 * <p>
 * 此类的构造使用单例模式，因为全局仅需要一个下载管理器的对象，对所有下载相关的功能进行统一处理。
 * </p>
 *
 * @author G. Seinfeld
 * @date 2019/06/28
 */
@Slf4j
public class DownloadManager {
    /**
     * 下载任务的ID，从0开始，每次加1
     */
    private static int serialMissionId = 0;
    /**
     * 下载管理器的单例对象
     */
    private static DownloadManager downloadManager;

    /**
     * 缓存所有的下载任务，这里的key是任务的ID
     */
    private static Map<Integer, DownloadMission> missionMap = new HashMap<>();

    private static DownloadThreadPool downloadThreadPool =
            new DownloadThreadPool(5, 10, 200L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024));

    private static BlockingQueue<DownloadMission> downloadMissionBlockingQueue = new LinkedBlockingQueue<>();
    /**
     * 私有构造器，防止直接实例化
     */
    private DownloadManager() {

    }

    /**
     * 获取下载管理器的单例对象，如果对象尚未初始化，则调用私有构造器创建一个对象
     *
     * @return 下载管理器的单例对象
     */
    public static DownloadManager getInstance() {
        if (downloadManager == null) {
            synchronized (DownloadManager.class) {
                if (downloadManager != null) {
                    downloadManager = new DownloadManager();
                }
            }
        }
        return downloadManager;
    }

    /**
     * FIXME 在本类中统一处理ID的问题，不要在DownloadMission类中再建一套ID的体系
     * 添加一个新的下载任务
     *
     * @param downloadMission 下载任务
     */
    public void addMission(DownloadMission downloadMission) {
        missionMap.put(serialMissionId++, downloadMission);
    }

    /**
     * TODO
     * 开始某个下载任务
     *
     * @param missionId 任务ID
     */
    public boolean startMission(int missionId) {
        // 1.判断是否存在任务
        if(!missionMap.containsKey(missionId)){
            log.error("missionId: {} is not exist.",missionId);
            return false;
        }
        DownloadMission downloadMission = missionMap.get(missionId);
        // 2.创建download runable
        if(downloadThreadPool.getActiveCount() == 0){
            return false;
        }
        if(!downloadThreadPool.isTerminated()){
            // 3.开启线程任务执行
            //downloadThreadPool.execute(new DownloadRunnable(downloadMission));
            return true;
        }
        return false;
    }

    /**
     * 开始所有下载任务
     */
    public void startAll() {
        for (Integer missionId : missionMap.keySet()) {
            boolean missionSuccess = startMission(missionId);
            if(!missionSuccess){
                downloadMissionBlockingQueue.offer(missionMap.get(missionId));
            }
        }
    }

    /**
     * TODO
     * 暂停某个下载任务
     *
     * @param missionId 任务ID
     */
    public void pauseMission(int missionId) {
    }

    /**
     * 暂停所有下载任务
     */
    public void pauseAll() {
        for (Integer missionId : missionMap.keySet()) {
            pauseMission(missionId);
        }
    }

    /**
     * TODO
     * 取消某个下载任务
     *
     * @param missionId 任务ID
     */
    public void cancelMission(int missionId) {
    }

    /**
     * 取消所有下载任务
     */
    public void cancelAll() {
        for (Integer missionId : missionMap.keySet()) {
            cancelMission(missionId);
        }
    }

    /**
     * TODO
     * 获取以字节数表示的下载速度，这里下载速度指上一秒内下载的字节数
     *
     * @param missionId 任务ID
     * @return 下载速度
     */
    public long getSpeed(int missionId) {
        return 0;
    }

    /**
     * TODO
     * 获取所有任务累计的以字节数表示的下载速度
     *
     * @return 总下载速度
     */
    public long getTotalSpeed() {
        int speed = 0;
        for (Integer missionId : missionMap.keySet()) {
            speed += getSpeed(missionId);
        }
        return speed;
    }

    /**
     * TODO
     * 获取某一下载任务已下载的文件大小，以字节表示
     *
     * @param missionId 任务ID
     * @return 已下载的文件大小
     */
    public long getDownloadedSize(int missionId) {
        return 0;
    }

    /**
     * TODO
     * 获取某一下载任务已完成的百分比，以字符串表示，保留两位有效数字
     *
     * @param missionId 任务ID
     * @return 百分比
     */
    public String getReadableDownloadedPercent(int missionId) {
        return null;
    }
}
