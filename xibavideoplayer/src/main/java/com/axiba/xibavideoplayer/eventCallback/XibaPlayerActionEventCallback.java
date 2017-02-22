package com.axiba.xibavideoplayer.eventCallback;

/**
 * Created by xiba on 2017/2/22.
 */

public interface XibaPlayerActionEventCallback {
    /**
     * 滑动更改进度回调
     * @param originPosition    更改之前的播放位置
     * @param seekTimePosition  更改之后的播放位置
     * @param totalTimeDuration 总时长
     */
    void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration);

    /**
     * 滑动更改进度结束
     */
    void onChangingPositionEnd();

    /**
     * 调整音量回调
     * @param percent 更改后的音量百分比
     */
    void onChangingVolume(int percent);

    /**
     * 音量变化结束
     */
    void onChangingVolumeEnd();

    /**
     * 调整亮度回调
     * @param percent 更改后的亮度百分比
     */
    void onChangingBrightness(int percent);

    /**
     * 亮度变化结束
     */
    void onChangingBrightnessEnd();

    /**
     * 单击屏幕
     */
    void onSingleTap();

    /**
     * 双击屏幕
     */
    void onDoubleTap();

    /**
     * 点击被锁住的屏幕
     */
    void onTouchLockedScreen();
}
