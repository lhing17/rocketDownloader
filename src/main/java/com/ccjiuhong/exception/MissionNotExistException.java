package com.ccjiuhong.exception;

/**
 * @author G. Seinfeld
 * @since 2019/12/12
 */
public class MissionNotExistException extends DownloadException {
    private static final String ERROR_MESSAGE = "任务不存在";

    public MissionNotExistException() {
        super(ERROR_MESSAGE);
    }

    public MissionNotExistException(Throwable cause) {
        super(ERROR_MESSAGE, cause);
    }
}
