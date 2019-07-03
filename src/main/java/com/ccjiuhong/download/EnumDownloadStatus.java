package com.ccjiuhong.download;

import java.util.HashMap;
import java.util.Map;

/**
 * 下载状态枚举类
 *
 * @author G. Seinfeld
 * @date 2019/06/28
 */
public enum EnumDownloadStatus {
    READY(0),
    PAUSED(1),
    DOWNLOADING(2),
    FINISHED(3);
    /**
     * 枚举值对应的码，枚举值构造后，不允许修改对应的码，因此设置为final
     */
    private final int code;
    private static Map<Integer, EnumDownloadStatus> cache = new HashMap<>();

    EnumDownloadStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 根据状态码获取相应的枚举值
     *
     * @param code 状态码
     * @return 枚举值
     */
    public static EnumDownloadStatus getStatusByCode(int code) {
        if (cache.containsKey(code)) return cache.get(code);
        for (EnumDownloadStatus enumDownloadStatus : values()) {
            if (enumDownloadStatus.getCode() == code) {
                cache.put(code, enumDownloadStatus);
                return enumDownloadStatus;
            }
        }
        return null;
    }

    /**
     * 比较下载状态，如果新状态与原状态不同，返回新状态
     *
     * @param old  原先的下载状态
     * @param _new 新的下载状态
     */
    public static EnumDownloadStatus compareAndSetDownloadStatus(EnumDownloadStatus old, EnumDownloadStatus _new) {
        if (old == null || _new == null) {
            throw new IllegalStateException("下载状态设置错误");
        }
        if (old.getCode() != _new.getCode()) {
            return _new;
        }
        return old;
    }
}
