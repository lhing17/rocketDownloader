package com.ccjiuhong.mission;

import com.ccjiuhong.download.EnumDownloadStatus;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author G. Seinfeld
 * @since 2019/12/12
 */
@Data
public class MissionMetaData {
    /**
     * 文件大小，如果是多个文件，则为文件总大小，单位为字节
     */
    private long fileSize;
    /**
     * 目标文件夹
     */
    private String targetDirectory;
    /**
     * 目标文件名
     */
    private String targetFileName;
    /**
     * 任务下载状态
     */
    private EnumDownloadStatus status;
    /**
     * 任务进度文件
     */
    private File progressFile;
    /**
     * 通过任务url获得的任务唯一标识
     */
    private String uniqueIdentifier;
    /**
     * 任务当前下载速度，单位为B/s
     */
    private long speed;
    /**
     * 任务已下载字节数
     */
    private long downloadedSize;
    /**
     * 下载该任务的线程数
     */
    private int threadNum = 5;

    /**
     * 种子文件的地址，针对PeerToPeerMission
     */
    @Nullable
    private String dotTorrentFilePath;

}
