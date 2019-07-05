package com.ccjiuhong.download;

import lombok.Data;

/**
 * @author dagerer
 */
@Data
public class DownloadInfo {
    private int missionId;
    private long fileSize;
    private String fileUrl;
    private String targetDirectory;
    private String targetFileName;
    private long startPosition;
    private long currentPosition;
    private long endPosition;
}
