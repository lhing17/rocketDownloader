package com.ccjiuhong.mgt;

import com.ccjiuhong.download.DownloadThreadPool;
import com.ccjiuhong.download.EnumDownloadStatus;
import com.ccjiuhong.exception.MissionAlreadyExistsException;
import com.ccjiuhong.exception.MissionNotExistException;
import com.ccjiuhong.mission.Mission;
import com.ccjiuhong.mission.MissionFactory;
import com.ccjiuhong.mission.MissionMetaData;
import com.ccjiuhong.mission.PeerToPeerMission;
import com.ccjiuhong.util.DownloadUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 下载管理器，封装了所有下载需要用到的方法，同时也是GUI访问逻辑代码的唯一入口类。
 *
 * <p>
 * 此类的构造使用单例模式，因为全局仅需要一个下载管理器的对象，对所有下载相关的功能进行统一处理。
 * </p>
 *
 * @author G. Seinfeld
 * @since 2019/06/28
 */
@Slf4j
public class DefaultDownloadManager implements DownloadManager {

    /**
     * 下载管理器的单例对象
     */
    private static DownloadManager defaultDownloadManager;

    /**
     * 缓存所有的下载任务，这里的key是任务的ID
     */
    private static Map<Integer, Mission> missionMap = new HashMap<>();

    /**
     * 缓存下载地址的集合，保证下载地址不重复
     */
    private static Set<String> missionUniqueIdentifiers = new HashSet<>();

    private static final int MAX_WORK_NUM = 10;

    /**
     * 下载的线程池
     */
    private static DownloadThreadPool downloadThreadPool =
            new DownloadThreadPool(MAX_WORK_NUM, MAX_WORK_NUM, 200L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024));

    private static BlockingQueue<Mission> downloadMissionBlockingQueue = new LinkedBlockingQueue<>();

    /**
     * 私有构造器，防止直接实例化
     */
    private DefaultDownloadManager() {

    }

    /**
     * 获取下载管理器的单例对象，如果对象尚未初始化，则调用私有构造器创建一个对象
     *
     * @return 下载管理器的单例对象
     */
    public static DownloadManager getInstance() {
        if (defaultDownloadManager == null) {
            synchronized (DefaultDownloadManager.class) {
                if (defaultDownloadManager == null) {
                    defaultDownloadManager = new DefaultDownloadManager();
                    // 初始化BT下载的环境
                    PeerToPeerMission.initBtRuntime();
                }
            }
        }
        return defaultDownloadManager;
    }

    /**
     * 添加一个新的下载任务
     *
     * @param fileUrl         文件地址
     * @param targetDirectory 目标文件夹
     * @param targetFileName  目标文件名
     * @return 任务重复时返回-1，添加成功返回任务ID
     */
    @Override
    public int addMission(String fileUrl, String targetDirectory, String targetFileName) {
        return addMission(fileUrl, targetDirectory, targetFileName, false);
    }

    /**
     * @param fileUrl         如果为BT下载，指向的是BT种子文件的位置；否则指向的是文件地址
     * @param targetDirectory 目标文件夹
     * @param targetFileName  目标文件名
     * @param isBt            是否为BT下载，注意这里只包括种子文件下载，不包括磁力链
     * @return 任务重复时返回-1，添加成功返回任务ID
     */
    @Override
    public int addMission(String fileUrl, String targetDirectory, String targetFileName, boolean isBt) {
        assertMissionNotExist(fileUrl);
        log.info("增加了下载任务，文件地址为：{}，目标目录为{}，目标文件名为：{}", fileUrl, targetDirectory, targetFileName);
        Mission mission = new MissionFactory().createMissionIntelligently(fileUrl, targetDirectory, targetFileName, downloadThreadPool, isBt);
        return addMission(mission);
    }

    /**
     * 添加一个新的下载任务
     *
     * @param mission 下载任务
     * @return 任务ID
     */
    @Override
    public int addMission(Mission mission) {
        cacheMission(mission);
        return mission.getMissionId();
    }

    /**
     * 确保新增任务时任务不存在，如果存在，抛出异常，以实现快速失败（fail-fast）
     *
     * @param fileUrl 任务地址
     */
    private void assertMissionNotExist(String fileUrl) {
        if (missionUniqueIdentifiers.contains(fileUrl)) {
            log.warn("下载任务{}已存在", fileUrl);
            throw new MissionAlreadyExistsException();
        }
    }

    /**
     * 将下载任务添加到内存的缓存中
     *
     * @param mission 下载任务
     */
    private void cacheMission(Mission mission) {
        missionMap.put(mission.getMissionId(), mission);
        missionUniqueIdentifiers.add(mission.getMetaData().getUniqueIdentifier());
    }

    /**
     * 确保任务存在，如果不存在，抛出异常，以实现快速失败（fail-fast）
     *
     * @param missionId 任务ID
     */
    private void assertMissionExists(int missionId) {
        // 判断是否存在任务
        if (!missionMap.containsKey(missionId)) {
            log.warn("missionId: {} does not exist.", missionId);
            throw new MissionNotExistException();
        }
    }

    /**
     * 开始或继续下载任务
     * <p>如果任务已存在，从进度文件中获取进度，并继续下载</p>
     *
     * @param missionId 任务id
     * @return 执行结果
     */
    @Override
    public boolean startOrResumeMission(int missionId) {
        log.info("尝试开启任务，任务ID为{}", missionId);
        assertMissionExists(missionId);
        Mission mission = missionMap.get(missionId);
        boolean success;

        // 如果下载处于暂停状态或者存在进度文件，则继续下载，否则开始新的下载
        if (mission.getMetaData().getStatus() == EnumDownloadStatus.PAUSED || mission.getMetaData().getProgressFile().exists()) {
            success = mission.resume();
        } else {
            success = mission.start();
        }
        return success;
    }

    /**
     * 开始所有下载任务
     */
    @Override
    public void startOrResumeAll() {
        for (Integer missionId : missionMap.keySet()) {
            boolean missionSuccess = startOrResumeMission(missionId);
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
    @Override
    public boolean pauseMission(int missionId) {
        log.info("尝试暂停任务，任务ID为{}", missionId);
        assertMissionExists(missionId);
        Mission mission = missionMap.get(missionId);
        return mission.pause();
    }

    /**
     * 暂停所有下载任务
     */
    @Override
    public void pauseAll() {
        for (Integer missionId : missionMap.keySet()) {
            boolean missionSuccess = pauseMission(missionId);
            if (!missionSuccess) {
                //TODO 暂停失败的异常处理
            }
        }
    }

    /**
     * 取消某个下载任务
     *
     * @param missionId 任务ID
     */
    @Override
    public boolean cancelMission(int missionId) {
        assertMissionExists(missionId);
        Mission mission = missionMap.get(missionId);
        missionMap.remove(missionId);
        return mission.delete();
    }

    /**
     * 取消所有下载任务
     */
    @Override
    public void cancelAll() {
        for (Integer missionId : missionMap.keySet()) {
            boolean missionSuccess = cancelMission(missionId);
            if (!missionSuccess) {
                //TODO 取消失败的异常处理
            }
        }
    }

    /**
     * 获取以字节数表示的下载速度，这里下载速度指上一秒内下载的字节数
     *
     * @param missionId 任务ID
     * @return 下载速度
     */
    @Override
    public long getSpeed(int missionId) {
        assertMissionExists(missionId);
        Mission mission = missionMap.get(missionId);
        return mission.getMetaData().getSpeed();
    }

    /**
     * 获取所有任务累计的以字节数表示的下载速度
     *
     * @return 总下载速度
     */
    @Override
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
    @Override
    public long getDownloadedSize(int missionId) {
        assertMissionExists(missionId);
        Mission mission = missionMap.get(missionId);
        return mission.getMetaData().getDownloadedSize();
    }

    /**
     * 获取某一下载任务已完成的百分比，以字符串表示，保留两位有效数字
     *
     * @param missionId 任务ID
     * @return 百分比
     */
    @Override
    public String getReadableDownloadedPercent(int missionId) {
        assertMissionExists(missionId);
        Mission mission = missionMap.get(missionId);
        MissionMetaData metaData = mission.getMetaData();
        double percent = 100.0 * metaData.getDownloadedSize() / metaData.getFileSize();
        return DownloadUtil.getReadablePercent(percent);
    }


}
