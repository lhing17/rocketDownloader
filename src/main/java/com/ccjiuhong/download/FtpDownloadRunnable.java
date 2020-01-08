package com.ccjiuhong.download;

import com.ccjiuhong.mission.FtpMission;
import com.ccjiuhong.mission.Mission;
import com.ccjiuhong.util.FtpInfo;
import com.ccjiuhong.util.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;

/**
 * 处理FTP下载的线程
 *
 * @author G. Seinfeld
 * @since 2020/01/08
 */
@Slf4j
public class FtpDownloadRunnable extends DownloadRunnable {
    public FtpDownloadRunnable(String targetDirectory, String targetFileName, String fileUrl, Mission mission, long startPosition, long currentPosition, long endPosition) {
        super(targetDirectory, targetFileName, fileUrl, mission, startPosition, currentPosition, endPosition);
    }

    public FtpDownloadRunnable(String targetDirectory, String targetFileName, String fileUrl, Mission mission, long startPosition, long endPosition) {
        super(targetDirectory, targetFileName, fileUrl, mission, startPosition, endPosition);
    }

    @Override
    public void run() {
        File targetFile = createTargetFile();
        BufferedInputStream bufferedInputStream;
        RandomAccessFile randomAccessFile;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            FtpInfo ftpInfo = FtpInfo.getInstance(fileUrl);
            Optional<FTPClient> ftpClientOptional = FtpUtil.login(ftpInfo.getHost(), ftpInfo.getPort(), ftpInfo.getUsername(), ftpInfo.getPassword());
            if (ftpClientOptional.isPresent()) {
                FTPClient client = ftpClientOptional.get();
                client.enterLocalPassiveMode();
                String pathname = FtpUtil.changeEncoding(client, ftpInfo.getPathname());
                String filename = FtpUtil.changeEncoding(client, ftpInfo.getFilename());
                client.changeWorkingDirectory(pathname);
                client.setRestartOffset(currentPosition);
                bufferedInputStream = new BufferedInputStream(client.retrieveFileStream(filename));
                randomAccessFile = new RandomAccessFile(targetFile, "rw");
                randomAccessFile.seek(currentPosition);
                while (currentPosition < endPosition) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    int len = bufferedInputStream.read(buffer, 0, BUFFER_SIZE);
                    if (len == -1) {
                        break;
                    } else {
                        randomAccessFile.write(buffer, 0, len);
                        currentPosition += len;
                        ((FtpMission) mission).getMissionMonitor().down(len);
                    }
                }
                bufferedInputStream.close();
                randomAccessFile.close();
            }

        } catch (IOException e) {
            log.error("文件读取不正确", e);
        }

    }
}
