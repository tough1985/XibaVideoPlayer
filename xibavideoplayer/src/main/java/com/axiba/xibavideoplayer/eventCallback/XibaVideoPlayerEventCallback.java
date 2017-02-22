package com.axiba.xibavideoplayer.eventCallback;

import android.graphics.Bitmap;
import android.view.ViewGroup;

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
     * 出错回调
     */
    void onPlayerError(int what, int extra);

    /**
     * 播放停止，播放器正在缓冲
     */
    void onStartLoading();

//    /**
//     * 滑动更改进度回调
//     * @param originPosition    更改之前的播放位置
//     * @param seekTimePosition  更改之后的播放位置
//     * @param totalTimeDuration 总时长
//     */
//    void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration);
//
//    /**
//     * 滑动更改进度结束
//     */
//    void onChangingPositionEnd();
//
//    /**
//     * 调整音量回调
//     * @param percent 更改后的音量百分比
//     */
//    void onChangingVolume(int percent);
//
//    /**
//     * 音量变化结束
//     */
//    void onChangingVolumeEnd();
//
//    /**
//     * 调整亮度回调
//     * @param percent 更改后的亮度百分比
//     */
//    void onChangingBrightness(int percent);
//
//    /**
//     * 亮度变化结束
//     */
//    void onChangingBrightnessEnd();
//
//    /**
//     * 单击屏幕
//     */
//    void onSingleTap();
//
//    /**
//     * 双击屏幕
//     */
//    void onDoubleTap();
//
//    /**
//     * 点击被锁住的屏幕
//     */
//    void onTouchLockedScreen();



//    /**
//     * 进入全屏回调
//     */
//    ViewGroup onEnterFullScreen();
//
//    /**
//     * 退出全屏回调
//     */
//    void onQuitFullScreen();

//    /**
//     * 进入小屏回调
//     */
//    void onEnterTinyScreen();
//
//    /**
//     * 退出小屏回调
//     */
//    void onQuitTinyScreen();




}
