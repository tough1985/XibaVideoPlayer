package com.axiba.xibavideoplayer.listener;

/**
 * Created by xiba on 2016/11/26.
 */
public interface XibaMediaListener {

    void onPrepared();

    void onAutoCompletion();

    void onCompletion();

    void onBufferingUpdate(int percent);

    void onSeekComplete();

    void onError(int what, int extra);

    void onInfo(int what, int extra);

    void onVideoSizeChanged(int width, int height);
}
