package com.axiba.xibavideoplayer.eventCallback;

import android.view.ViewGroup;

/**
 * Created by xiba on 2016/12/23.
 */
public interface XibaFullScreenEventCallback {

    /**
     * 进入全屏回调
     * @return 返回ViewGroup作为全屏播放的容器
     */
    ViewGroup onEnterFullScreen();

    /**
     * 退出全屏回调
     */
    void onQuitFullScreen();
}
