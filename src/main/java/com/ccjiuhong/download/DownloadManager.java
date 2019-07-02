package com.ccjiuhong.download;

import com.ccjiuhong.util.DownloadUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;

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

    private static final int MAX_WORK_NUM = 10;

    /**
     * 下载的线程池
     */
    private static DownloadThreadPool downloadThreadPool =
            new DownloadThreadPool(MAX_WORK_NUM, MAX_WORK_NUM, 200L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024));

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
                if (downloadManager == null) {
                    downloadManager = new DownloadManager();
                }
            }
        }
        return downloadManager;
    }

    /**
     * 添加一个新的下载任务
     *
     * @param fileUrl         文件地址
     * @param targetDirectory 目标文件夹
     * @param targetFileName  目标文件名
     * @return 任务ID
     */
    public int addMission(String fileUrl, String targetDirectory, String targetFileName) {
        log.info("增加了下载任务，文件地址为：{}，目标目录为{}，目标文件名为：{}", fileUrl, targetDirectory, targetFileName);
        DownloadMission downloadMission = new DownloadMission(serialMissionId++, fileUrl, targetDirectory,
                targetFileName);
        addMission(downloadMission);
        return downloadMission.getMissionId();
    }

    /**
     * 添加一个新的下载任务
     *
     * @param downloadMission 下载任务
     */
    private void addMission(DownloadMission downloadMission) {
        missionMap.put(downloadMission.getMissionId(), downloadMission);
    }

    /**
     * 开始某个下载任务
     * <p>如果任务已存在，从进度文件中获取进度，并继续下载</p>
     * TODO 从进度文件读取的逻辑还没有实现
     *
     * @param missionId 任务ID
     */
    public boolean startMission(int missionId) {
        log.info("尝试开启任务，任务ID为{}", missionId);
        assertMissionExists(missionId);
        return startOrResumeMission(missionId);
    }

    /**
     * 确保任务存在，如果不存在，抛出异常，以实现快速失败
     *
     * @param missionId 任务ID
     */
    private void assertMissionExists(int missionId) {
        // 判断是否存在任务
        if (!missionMap.containsKey(missionId)) {
            log.warn("missionId: {} does not exist.", missionId);
            throw new IllegalStateException("任务不存在");
        }
    }

    /**
     * 开始所有下载任务
     */
    public void startAll() {
        for (Integer missionId : missionMap.keySet()) {
            boolean missionSuccess = startMission(missionId);
            // FIXME 异常处理，暂时将任务加入到一个缓冲队列中，这有啥用？@dagerer
            if (!missionSuccess) {
                downloadMissionBlockingQueue.offer(missionMap.get(missionId));
            }
        }
    }

    /**
     * 暂停某个下载任务
     *
     * @param missionId 任务ID
     */
    public boolean pauseMission(int missionId) {
        log.info("尝试暂停任务，任务ID为{}", missionId);
        assertMissionExists(missionId);
        DownloadMission downloadMission = missionMap.get(missionId);
        return downloadMission.pause(downloadThreadPool);
    }

    /**
     * 暂停所有下载任务
     */
    public void pauseAll() {
        for (Integer missionId : missionMap.keySet()) {
            boolean missionSuccess = pauseMission(missionId);
            if (!missionSuccess) {
                //TODO 暂停失败的异常处理
            }
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
     * 获取以字节数表示的下载速度，这里下载速度指上一秒内下载的字节数
     *
     * @param missionId 任务ID
     * @return 下载速度
     */
    public long getSpeed(int missionId) {
        assertMissionExists(missionId);
        DownloadMission downloadMission = missionMap.get(missionId);
        return downloadMission.getSpeedMonitor().getSpeed();
    }

    /**
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
     * 获取某一下载任务已下载的文件大小，以字节表示
     *
     * @param missionId 任务ID
     * @return 已下载的文件大小
     */
    public long getDownloadedSize(int missionId) {
        assertMissionExists(missionId);
        DownloadMission downloadMission = missionMap.get(missionId);
        return downloadMission.getMissionMonitor().getDownloadedSize().get();
    }

    /**
     * 获取某一下载任务已完成的百分比，以字符串表示，保留两位有效数字
     *
     * @param missionId 任务ID
     * @return 百分比
     */
    public String getReadableDownloadedPercent(int missionId) {
        assertMissionExists(missionId);
        DownloadMission downloadMission = missionMap.get(missionId);
        double percent = 100.0 * downloadMission.getSpeedMonitor().getCurrentSize() / downloadMission.getFileSize();
        return DownloadUtil.getReadablePercent(percent);
    }

    public boolean startOrResumeMission(int missionId) {
        DownloadMission downloadMission = missionMap.get(missionId);
        boolean start;
        if (downloadMission.getDownloadStatus().getCode() == EnumDownloadStatus.PAUSED.getCode()) {
            start = downloadMission.resume(downloadThreadPool);
        } else {
            start = downloadMission.start(downloadThreadPool);
        }
        return start;
    }
}
