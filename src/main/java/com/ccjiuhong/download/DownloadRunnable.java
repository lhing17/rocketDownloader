package com.ccjiuhong.download;

import com.ccjiuhong.mission.Mission;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * 下载所用的线程
 *
 * @author G. Seinfeld
 * @since 2019/06/27
 */
@Data
@Slf4j
public abstract class DownloadRunnable implements Runnable {
    protected static final int BUFFER_SIZE = 1024;
    /**
     * 1048576字节，即1MB
     */
    protected static final long SPLIT_THRESHOLD = 1 << 20;
    /**
     * 目标文件夹
     */
    protected String targetDirectory;
    /**
     * 目标文件名
     */
    protected String targetFileName;
    /**
     * 文件下载地址
     */
    protected String fileUrl;
    /**
     * 当前下载任务
     */
    protected Mission mission;
    /**
     * 下载起始位置
     */
    protected long startPosition;
    /**
     * 当前指针所在位置
     */
    protected long currentPosition;
    /**
     * 文件结束位置
     */
    protected long endPosition;
    /**
     * 线程ID
     */
    protected int id;

    protected static int ID_COUNTER = 0;


    /**
     * 构造新的下载线程
     *
     * @param targetDirectory 目标目录
     * @param targetFileName  目标文件名
     * @param fileUrl         文件下载地址
     * @param startPosition   下载开始位置
     * @param currentPosition 当前位置
     * @param endPosition     结束位置
     */
    public DownloadRunnable(String targetDirectory, String targetFileName, String fileUrl,
                            Mission mission,
                            long startPosition, long currentPosition, long endPosition) {
        this.targetDirectory = targetDirectory;
        this.targetFileName = targetFileName;
        this.fileUrl = fileUrl;
        this.mission = mission;
        this.startPosition = startPosition;
        this.currentPosition = currentPosition;
        this.endPosition = endPosition;
        this.id = ID_COUNTER++;
    }

    /**
     * 不提供当前位置，则以开始位置作为当前位置
     */
    public DownloadRunnable(String targetDirectory, String targetFileName, String fileUrl,
                            Mission mission, long startPosition, long endPosition) {
        this(targetDirectory, targetFileName, fileUrl, mission, startPosition, startPosition, endPosition);
    }

    /**
     * 将当前下载线程一分为二，拆分出一个
     */
    public DownloadRunnable split() {
        long remaining = endPosition - currentPosition;
        long half = remaining >> 1;
        if (half > SPLIT_THRESHOLD) {
            long newEndPosition = endPosition;
            long newStartPosition = currentPosition + half + 1;
            endPosition = newStartPosition - 1;
            return DownloadRunnableFactory.createDownloadRunnable(targetDirectory, targetFileName, fileUrl, mission, newStartPosition,
                    newEndPosition);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "DownloadRunnable{" + targetFileName + ":" + id + "}";
    }

    /**
     * 创建任务对应的目标文件，这里考虑到并发问题，对创建文件使用了同步锁
     *
     * @return 文件对象
     */
    protected File createTargetFile() {
        File targetFile;
        synchronized (this) {
            File dir = new File(targetDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            targetFile = new File(targetDirectory + File.separator + targetFileName);
            if (!targetFile.exists()) {
                try {
                    targetFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return targetFile;
    }
}
