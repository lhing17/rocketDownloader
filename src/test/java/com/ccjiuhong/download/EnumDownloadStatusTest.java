package com.ccjiuhong.download;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author G. Seinfeld
 * @date 2019/06/28
 */
public class EnumDownloadStatusTest {

    @Test
    public void getStatusByCode() {
        Assertions.assertThat(EnumDownloadStatus.getStatusByCode(0)).isEqualByComparingTo(EnumDownloadStatus.READY);
        Assertions.assertThat(EnumDownloadStatus.getStatusByCode(1)).isEqualByComparingTo(EnumDownloadStatus.PAUSED);
        Assertions.assertThat(EnumDownloadStatus.getStatusByCode(2)).isEqualByComparingTo(EnumDownloadStatus.DOWNLOADING);
        Assertions.assertThat(EnumDownloadStatus.getStatusByCode(3)).isEqualByComparingTo(EnumDownloadStatus.FINISHED);
    }
}