package com.ccjiuhong.mission;

import com.ccjiuhong.download.DownloadThreadPool;
import com.ccjiuhong.util.FtpInfo;
import com.ccjiuhong.util.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.Optional;

/**
 * @author G. Seinfeld
 * @since 2020/01/08
 */
@Slf4j
public class FtpMission extends ServerToClientMission {
    public FtpMission(int missionId, String fileUrl, String targetDirectory, String targetFileName, DownloadThreadPool downloadThreadPool) {
        super(missionId, fileUrl, targetDirectory, targetFileName, downloadThreadPool);
    }

    public FtpMission(int missionId, String targetDirectory, String targetFileName) {
        super(missionId, targetDirectory, targetFileName);
    }

    @Override
    protected long getFileSizeFromUrl(String fileUrl) {
        try {
            FtpInfo ftpInfo = FtpInfo.getInstance(fileUrl);
            Optional<FTPClient> ftp = FtpUtil.login(ftpInfo.getHost(), ftpInfo.getPort(), ftpInfo.getUsername(), ftpInfo.getPassword());
            long length = ftp
                    .map(ftpClient -> {
                        try {
                            ftpClient.enterLocalPassiveMode();
                            return ftpClient.listFiles(ftpInfo.getFullpath());
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                            return null;
                        }
                    })
                    .map(ftpFiles1 -> ftpFiles1[0])
                    .map(FTPFile::getSize)
                    .orElse(0L);
            getMetaData().setFileSize(length);
            return length;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return 0;
        }

    }
}
