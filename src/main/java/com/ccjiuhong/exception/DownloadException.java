package com.ccjiuhong.exception;

/**
 * @author G. Seinfeld
 * @since 2019/12/12
 */
public class DownloadException extends RuntimeException {
    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
