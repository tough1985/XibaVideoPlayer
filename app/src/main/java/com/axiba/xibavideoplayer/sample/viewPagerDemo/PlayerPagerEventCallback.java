package com.axiba.xibavideoplayer.sample.viewPagerDemo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2016/12/22.
 */

public class PlayerPagerEventCallback implements XibaVideoPlayerEventCallback {

    private Button play;
    private TextView currentTimeTV;
    private TextView totalTimeTV;
    private SeekBar demoSeek;
    private Button fullScreenBN;
    private Button tinyScreenBN;
    private ProgressBar loadingPB;

    private boolean isBinding = false;

    private boolean isTrackingTouchSeekBar = false;

    public void bindingPlayerUI(PlayerFragment playerFragment) {
        this.play = playerFragment.getPlay();
        this.currentTimeTV = playerFragment.getCurrentTimeTV();
        this.totalTimeTV = playerFragment.getTotalTimeTV();
        this.demoSeek = playerFragment.getDemoSeek();
        this.fullScreenBN = playerFragment.getFullScreenBN();
        this.tinyScreenBN = playerFragment.getTinyScreenBN();
        this.loadingPB = playerFragment.getLoadingPB();

        isBinding = true;
    }

    public void unbindPlayerUI(){
        this.play = null;
        this.currentTimeTV = null;
        this.totalTimeTV = null;
        this.demoSeek = null;
        this.fullScreenBN = null;
        this.tinyScreenBN = null;
        this.loadingPB = null;

        isBinding = false;
    }

    public boolean isBinding() {
        return isBinding;
    }

    public boolean isTrackingTouchSeekBar() {
        return isTrackingTouchSeekBar;
    }

    public void setTrackingTouchSeekBar(boolean trackingTouchSeekBar) {
        isTrackingTouchSeekBar = trackingTouchSeekBar;
    }

    @Override
    public void onPlayerPrepare() {
        if (!isBinding) {
            return;
        }

        play.setText("暂停");

//        if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
//            play.setText("暂停");
//        }
    }

    @Override
    public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {

        if (!isBinding) {
            return;
        }

        currentTimeTV.setText(XibaUtil.stringForTime(currentTime));
        totalTimeTV.setText(XibaUtil.stringForTime(totalTime));

        if (!isTrackingTouchSeekBar) {
            demoSeek.setProgress(progress);
        }

        demoSeek.setSecondaryProgress(secProgress);
        if (!demoSeek.isEnabled()) {
            demoSeek.setEnabled(true);
        }

        if (loadingPB.getVisibility() == View.VISIBLE) {
            loadingPB.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPlayerPause() {
        if (!isBinding) {
            return;
        }

        play.setText("播放");
    }

    @Override
    public void onPlayerResume() {
        if (!isBinding) {
            return;
        }
        play.setText("暂停");
    }

    @Override
    public void onPlayerComplete() {
        if (!isBinding) {
            return;
        }
        play.setText("播放");
    }

    @Override
    public void onPlayerAutoComplete() {
        if (!isBinding) {
            return;
        }
        play.setText("播放");
    }

    @Override
    public void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration) {
        if (!isBinding) {
            return;
        }

        int progress = (int) (seekTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度
        demoSeek.setProgress(progress);
    }

    @Override
    public void onChangingPositionEnd() {

    }

    @Override
    public void onChangingVolume(int percent) {

    }

    @Override
    public void onChangingVolumeEnd() {

    }

    @Override
    public void onChangingBrightness(int percent) {

    }

    @Override
    public void onChangingBrightnessEnd() {

    }

    @Override
    public void onPlayerError(int what, int extra) {

    }

//    @Override
//    public ViewGroup onEnterFullScreen() {
//        return null;
//    }
//
//    @Override
//    public void onQuitFullScreen() {
//
//    }

    @Override
    public void onEnterTinyScreen() {

    }

    @Override
    public void onQuitTinyScreen() {

    }

    @Override
    public void onSingleTap() {

    }

    @Override
    public void onDoubleTap() {

    }

    @Override
    public void onTouchLockedScreen() {

    }

    @Override
    public void onStartLoading() {
        if (!isBinding) {
            return;
        }

        if (loadingPB.getVisibility() != View.VISIBLE) {
            loadingPB.setVisibility(View.VISIBLE);
        }
    }
}
