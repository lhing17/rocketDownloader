package com.ccjiuhong.download;

import com.ccjiuhong.mission.HttpMission;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author G.Seinfeld
 * @since 2019/07/03
 */
public class HttpMissionTest {
    private String fileUrl = "http://www.qq.com/qq.exe";
    private String targetDirectory = "F:\\rocketDownloader";
    private String targetFileName = "qq.exe";
    private static final int MAX_WORK_NUM = 10;
    /**
     * 下载的线程池
     */
    private static DownloadThreadPool downloadThreadPool =
            new DownloadThreadPool(MAX_WORK_NUM, MAX_WORK_NUM, 200L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024));
    private HttpMission httpMission = new HttpMission(0, fileUrl,
            targetDirectory, targetFileName, downloadThreadPool);


    @Test
    public void readDownloadInfo() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<HttpMission> clazz = HttpMission.class;
        Method readDownloadInfo = clazz.getDeclaredMethod("readDownloadInfo");
        readDownloadInfo.setAccessible(true);
        readDownloadInfo.invoke(httpMission);
    }

    /**
     * 测试将下载信息保存为JSON文件
     */
    @Test
    public void saveOrUpdateDownloadInfo() {

        List<DownloadRunnable> runnableList = new ArrayList<>();
        DownloadRunnable downloadRunnable = DownloadRunnableFactory.createDownloadRunnable(targetDirectory, targetFileName,
                fileUrl, httpMission, 0, 999);
        DownloadRunnable downloadRunnable1 = DownloadRunnableFactory.createDownloadRunnable(targetDirectory, targetFileName,
                fileUrl, httpMission, 1000, 1999);
        runnableList.add(downloadRunnable);
        runnableList.add(downloadRunnable1);
        httpMission.saveOrUpdateDownloadInfo(runnableList);
    }
}