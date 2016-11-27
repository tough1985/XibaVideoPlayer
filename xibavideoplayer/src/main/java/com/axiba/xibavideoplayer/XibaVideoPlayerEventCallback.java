package com.axiba.xibavideoplayer;

/**
 * 播放器回调事件接口
 * Created by xiba on 2016/11/27.
 */
public interface XibaVideoPlayerEventCallback {

    /**
     * 从头开始播放
     */
    void onPrepare();

    /**
     * 播放进度更新
     * @param progress
     * @param secProgress
     * @param currentTime
     * @param totalTime
     */
    void onProgressUpdate(int progress, int secProgress, int currentTime, int totalTime);

    /**
     * 暂停
     */
    void onPause();

    /**
     * 继续播放
     */
    void onResume();

    /**
     * 播放结束
     */
    void onComplete();

    /**
     * 自动播放完成
     */
    void onAutoComplete();

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
    void onError(int what, int extra);

}
