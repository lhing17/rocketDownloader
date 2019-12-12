package com.ccjiuhong.mission;

import com.ccjiuhong.download.EnumDownloadStatus;
import com.ccjiuhong.util.DownloadUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * @author G. Seinfeld
 * @since 2019/12/11
 */
@Data
@Slf4j
public abstract class GenericMission implements Mission {
    /**
     * 任务的唯一标识
     */
    private int missionId;

    /**
     * 元数据
     */
    private MissionMetaData metaData;

    protected static final String DOWNLOAD_INFO_SUFFIX = "dl.json";

    public GenericMission(int missionId, String targetDirectory, String targetFileName) {
        this.missionId = missionId;
        metaData = new MissionMetaData();
        metaData.setStatus(EnumDownloadStatus.READY);
        metaData.setTargetDirectory(targetDirectory);
        metaData.setProgressFile(new File(DownloadUtil.getName(targetDirectory, String.valueOf(missionId),
                DOWNLOAD_INFO_SUFFIX)));
        metaData.setTargetFileName(targetFileName);
    }


}
