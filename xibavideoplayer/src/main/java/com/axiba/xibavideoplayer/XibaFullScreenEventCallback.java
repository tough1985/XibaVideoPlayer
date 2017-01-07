package com.axiba.xibavideoplayer;

import android.view.ViewGroup;

/**
 * Created by xiba on 2016/12/23.
 */

public interface XibaFullScreenEventCallback {
    /**
     * 进入全屏回调
     */
    ViewGroup onEnterFullScreen();

    /**
     * 退出全屏回调
     */
    void onQuitFullScreen();
}
