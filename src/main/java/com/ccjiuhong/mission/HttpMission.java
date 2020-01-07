package com.ccjiuhong.mission;

import com.ccjiuhong.download.DownloadThreadPool;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static com.ccjiuhong.util.SslUtil.DO_NOT_VERIFY;
import static com.ccjiuhong.util.SslUtil.trustAllHosts;

/**
 * 指一个下载任务的对象，一个下载任务可以由多个线程组成
 *
 * @author G. Seinfeld
 * @since 2019/06/28
 */
@Slf4j
public class HttpMission extends ServerToClientMission{


    public HttpMission(int missionId, String fileUrl, String targetDirectory, String targetFileName, DownloadThreadPool downloadThreadPool) {
        super(missionId, fileUrl, targetDirectory, targetFileName, downloadThreadPool);
    }

    public HttpMission(int missionId, String targetDirectory, String targetFileName) {
        super(missionId, targetDirectory, targetFileName);
    }

    protected long getFileSizeFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            URLConnection urlConnection = url.openConnection();
            boolean useHttps = fileUrl.startsWith("https");
            if (useHttps) {
                HttpsURLConnection https = (HttpsURLConnection) urlConnection;
                trustAllHosts(https);
                https.setHostnameVerifier(DO_NOT_VERIFY);
                long fileSize = https.getContentLengthLong();
                getMetaData().setFileSize(fileSize);
                return fileSize;
            }
            return urlConnection.getContentLengthLong();
        } catch (IOException e) {
            log.error("从服务器获取文件大小失败", e);
            return 0;
        }
    }

}
