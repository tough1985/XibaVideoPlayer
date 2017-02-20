package com.axiba.xibavideoplayer.sample.viewPagerDemo;

import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaListPlayUtil;
import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2016/12/22.
 */

public class PlayerPagerEventCallback implements XibaVideoPlayerEventCallback, XibaTinyScreenEventCallback {

    private Button play;
    private TextView currentTimeTV;
    private TextView totalTimeTV;
    private SeekBar demoSeek;
    private Button fullScreenBN;
    private Button tinyScreenBN;
    private ProgressBar loadingPB;

    private boolean isTrackingTouchSeekBar = false;


    private PlayerFragment playerUI;

    private PlayerFragment nextPlayerUI;

    private XibaListPlayUtil mXibaListPlayUtil;

    private Message mUtilMsg;

    public PlayerPagerEventCallback(XibaListPlayUtil mXibaListPlayUtil) {
        this.mXibaListPlayUtil = mXibaListPlayUtil;

        mXibaListPlayUtil.setPlayingItemPositionChangeImpl(new XibaListPlayUtil.PlayingItemPositionChange() {

            @Override
            public void prePlayingItemPositionChange(Message utilMsg) {
                mUtilMsg = utilMsg;
            }

            @Override
            public void prePlayingItemChangeOnPause() {
                if (playerUI != null) {
                    demoSeek.setEnabled(false);

                    //如果loading正在显示，在这里隐藏
                    if (loadingPB.getVisibility() == View.VISIBLE) {
                        loadingPB.setVisibility(View.GONE);
                    }

                    changePlayerUI();
                }
            }
        });
    }

    public void bindingPlayerUI(PlayerFragment playerFragment, int position) {

        if (this.playerUI == null || mXibaListPlayUtil.getPlayingIndex() == position) {
            this.playerUI = playerFragment;
            bindingUIItem();
        } else {
            this.nextPlayerUI = playerFragment;
        }


//        this.play = playerFragment.getPlay();
//        this.currentTimeTV = playerFragment.getCurrentTimeTV();
//        this.totalTimeTV = playerFragment.getTotalTimeTV();
//        this.demoSeek = playerFragment.getDemoSeek();
//        this.fullScreenBN = playerFragment.getFullScreenBN();
//        this.tinyScreenBN = playerFragment.getTinyScreenBN();
//        this.loadingPB = playerFragment.getLoadingPB();

//        isBinding = true;
    }

    public void changePlayerUI(){

        if (nextPlayerUI != null) {
            playerUI = nextPlayerUI;
            nextPlayerUI = null;
            bindingUIItem();
        }
    }

    private void bindingUIItem(){
        this.play = playerUI.getPlay();
        this.currentTimeTV = playerUI.getCurrentTimeTV();
        this.totalTimeTV = playerUI.getTotalTimeTV();
        this.demoSeek = playerUI.getDemoSeek();
        this.fullScreenBN = playerUI.getFullScreenBN();
        this.tinyScreenBN = playerUI.getTinyScreenBN();
        this.loadingPB = playerUI.getLoadingPB();
    }


//    public void unbindPlayerUI(){
//        this.play = null;
//        this.currentTimeTV = null;
//        this.totalTimeTV = null;
//        this.demoSeek = null;
//        this.fullScreenBN = null;
//        this.tinyScreenBN = null;
//        this.loadingPB = null;
//
////        isBinding = false;
//    }

//    public boolean isBinding() {
//        return isBinding;
//    }

    public boolean isTrackingTouchSeekBar() {
        return isTrackingTouchSeekBar;
    }

    public void setTrackingTouchSeekBar(boolean trackingTouchSeekBar) {
        isTrackingTouchSeekBar = trackingTouchSeekBar;
    }

    @Override
    public void onPlayerPrepare() {

        play.setText("暂停");
    }

    @Override
    public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {

//        if (!isBinding) {
//            return;
//        }

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
//        if (!isBinding) {
//            return;
//        }

        play.setText("播放");

        if (mUtilMsg != null) {
            demoSeek.setEnabled(false);

            //如果loading正在显示，在这里隐藏
            if (loadingPB.getVisibility() == View.VISIBLE) {
                loadingPB.setVisibility(View.GONE);
            }

            //在这里解除对Holder的绑定，否则loading会出现在上一个Item中
            changePlayerUI();

            mUtilMsg.sendToTarget();

            mUtilMsg = null;
        }
    }

    @Override
    public void onPlayerResume() {
//        if (!isBinding) {
//            return;
//        }
        play.setText("暂停");
    }

    @Override
    public void onPlayerComplete() {
//        if (!isBinding) {
//            return;
//        }
        play.setText("播放");
    }

    @Override
    public void onPlayerAutoComplete() {
//        if (!isBinding) {
//            return;
//        }
        play.setText("播放");
    }

    @Override
    public void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration) {
//        if (!isBinding) {
//            return;
//        }

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
        tinyScreenBN.setText("返回");
    }

    @Override
    public void onQuitTinyScreen() {
        tinyScreenBN.setText("小屏");
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
//        if (!isBinding) {
//            return;
//        }

        if (loadingPB.getVisibility() != View.VISIBLE) {
            loadingPB.setVisibility(View.VISIBLE);
        }
    }
}
