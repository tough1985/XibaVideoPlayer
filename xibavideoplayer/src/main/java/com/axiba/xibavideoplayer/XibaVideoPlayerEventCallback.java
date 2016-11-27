package com.axiba.xibavideoplayer;

/**
 * 播放器回调事件接口
 * Created by xiba on 2016/11/27.
 */
public interface XibaVideoPlayerEventCallback {

    /**
     * 从头开始播放
     */
    void onPlayerPrepare();

    /**
     * 播放进度更新
     * @param progress      播放百分比
     * @param secProgress   缓冲百分比
     * @param currentTime   当前播放位置
     * @param totalTime     总时长
     */
    void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime);

    /**
     * 暂停
     */
    void onPlayerPause();

    /**
     * 继续播放
     */
    void onPlayerResume();

    /**
     * 播放结束
     */
    void onPlayerComplete();

    /**
     * 自动播放完成
     */
    void onPlayerAutoComplete();

    /**
     * 进度变化回调
     * @param seekTime
     * @param seekTimePosition
     * @param totalTime
     * @param totalTimeDuration
     */
    void onSeekPositionChange(String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration);

    /**
     * 调整音量回调
     * @param percent 更改后的音量百分比
     */
    void onSeekVolumeChange(float percent);

    /**
     * 调整亮度回调
     * @param percent 更改后的亮度百分比
     */
    void onSeekBrightnessSlide(float percent);

    /**
     * 出错回调
     */
    void onPlayerError(int what, int extra);

}
