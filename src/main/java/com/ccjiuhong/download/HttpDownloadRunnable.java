package com.ccjiuhong.download;

import com.ccjiuhong.mission.HttpMission;
import com.ccjiuhong.mission.Mission;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import static com.ccjiuhong.util.SslUtil.DO_NOT_VERIFY;
import static com.ccjiuhong.util.SslUtil.trustAllHosts;

/**
 * 处理http下载的线程
 *
 * @author G. Seinfeld
 * @since 2020/01/08
 */
@Slf4j
public class HttpDownloadRunnable extends DownloadRunnable {
    public HttpDownloadRunnable(String targetDirectory, String targetFileName, String fileUrl, Mission mission, long startPosition, long currentPosition, long endPosition) {
        super(targetDirectory, targetFileName, fileUrl, mission, startPosition, currentPosition, endPosition);
    }

    public HttpDownloadRunnable(String targetDirectory, String targetFileName, String fileUrl, Mission mission, long startPosition, long endPosition) {
        super(targetDirectory, targetFileName, fileUrl, mission, startPosition, endPosition);
    }

    /**
     * 处理实际下载的逻辑
     */
    @Override
    public void run() {
        File targetFile = createTargetFile();
        BufferedInputStream bufferedInputStream;
        RandomAccessFile randomAccessFile;
        byte[] buffer = new byte[BUFFER_SIZE];
        URLConnection urlConnection;
        try {
            URL url = new URL(fileUrl);
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Range", "bytes=" + currentPosition + "-" + endPosition);
            // TODO 将不校验SSL证书作为一个配置项
            boolean useHttps = fileUrl.startsWith("https");
            if (useHttps) {
                HttpsURLConnection https = (HttpsURLConnection) urlConnection;
                trustAllHosts(https);
                https.setHostnameVerifier(DO_NOT_VERIFY);
                bufferedInputStream = new BufferedInputStream(https.getInputStream());
            } else {
                bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
            }
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
                    ((HttpMission) mission).getMissionMonitor().down(len);
                }
            }
            bufferedInputStream.close();
            randomAccessFile.close();
        } catch (IOException e) {
            log.error("文件读取不正确", e);
        }
    }


}
