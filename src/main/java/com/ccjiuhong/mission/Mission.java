package com.ccjiuhong.mission;

/**
 * 此接口代表一个下载任务，用于完成任务的开始、暂停、继续下载和下载完成。
 *
 * @author G. Seinfeld
 * @since 2019/12/10
 */
public interface Mission {

    int getMissionId();

    boolean start();

    boolean pause();

    boolean resume();

    boolean delete();

   MissionMetaData getMetaData();
}
