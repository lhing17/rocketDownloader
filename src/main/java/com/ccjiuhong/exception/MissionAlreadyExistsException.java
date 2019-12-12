package com.ccjiuhong.exception;

/**
 * @author G. Seinfeld
 * @since 2019/12/12
 */
public class MissionAlreadyExistsException extends DownloadException {

    private static final String ERROR_MESSAGE = "任务已存在";

    public MissionAlreadyExistsException() {
        super(ERROR_MESSAGE);
    }

    public MissionAlreadyExistsException(Throwable cause) {
        super(ERROR_MESSAGE, cause);
    }
}
