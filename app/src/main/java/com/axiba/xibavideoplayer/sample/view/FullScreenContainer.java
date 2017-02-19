package com.axiba.xibavideoplayer.sample.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2017/2/19.
 */

public class FullScreenContainer extends FrameLayout {

    public static final String TAG = FullScreenContainer.class.getSimpleName();

    private XibaVideoPlayer mXibaVideoPlayer;

    private Button fScreenPlayBN;
    private Button fScreenBackToNormalBN;
    private Button fScreenLockScreenBN;
    private TextView fScreenCurrentTimeTV;
    private TextView fScreenTotalTimeTV;
    private SeekBar fScreenDemoSeek;
    private TextView fScreenPositionChangingInfoTV;
    private RelativeLayout fScreenBottomContainerRL;
    private LinearLayout fScreenVolumeBrightLL;
    private TextView fScreenVolumeBrightTV;
    private SeekBar fScreenVolumeBrightSeek;

    private ProgressBar fScreenLoadingPB;

    private boolean isTrackingTouchSeekBar = false;     //是否正在控制SeekBar

    private FullScreenEventCallback mFullScreenEventCallback;

    public FullScreenContainer(Context context) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.activity_simple_demo_fullscreen, this);

        fScreenPlayBN = (Button) this.findViewById(R.id.demo_play);
        fScreenBackToNormalBN = (Button) this.findViewById(R.id.back_to_normal_BN);
        fScreenLockScreenBN = (Button) this.findViewById(R.id.lock_screen_BN);
        fScreenCurrentTimeTV = (TextView) this.findViewById(R.id.current_time);
        fScreenTotalTimeTV = (TextView) this.findViewById(R.id.total_time);
        fScreenDemoSeek = (SeekBar) this.findViewById(R.id.demo_seek);
        fScreenPositionChangingInfoTV = (TextView) this.findViewById(R.id.position_changing_info_TV);
        fScreenBottomContainerRL = (RelativeLayout) this.findViewById(R.id.full_screen_bottom_container_RL);
        fScreenVolumeBrightLL = (LinearLayout) this.findViewById(R.id.full_screen_volume_bright_LL);
        fScreenVolumeBrightTV = (TextView) this.findViewById(R.id.full_screen_volume_bright_TV);
        fScreenVolumeBrightSeek = (SeekBar) this.findViewById(R.id.full_screen_volume_bright_seek);
        fScreenLoadingPB = (ProgressBar) this.findViewById(R.id.full_screen_loading_PB);
    }

    //初始化UI
    public void initUI(XibaVideoPlayer xibaVideoPlayer) {
        this.mXibaVideoPlayer = xibaVideoPlayer;

        //全屏播放按钮监听
        fScreenPlayBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mXibaVideoPlayer.togglePlayPause();
            }
        });

        //退出全屏按钮监听
        fScreenBackToNormalBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mXibaVideoPlayer.quitFullScreen();
            }
        });

        //锁屏按钮监听
        fScreenLockScreenBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mXibaVideoPlayer.isScreenLock()) {
                    mXibaVideoPlayer.setScreenLock(false);
                    fScreenLockScreenBN.setText("锁屏");
                    showFullScreenUI();     //显示全部控件
                } else {
                    mXibaVideoPlayer.setScreenLock(true);
                    fScreenLockScreenBN.setText("解锁");
                    dismissFullScreenUI();  //隐藏全部控件
                }
            }
        });

        //进度条监听
        fScreenDemoSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouchSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mXibaVideoPlayer.seekTo(seekBar.getProgress());
                isTrackingTouchSeekBar = false;
            }
        });

        //设置播放按钮状态
        if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
            fScreenPlayBN.setText("暂停");
        } else {
            fScreenPlayBN.setText("播放");
        }

        //如果视频未加载，进度条不可用
        if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_NORMAL
                || mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_ERROR) {

            fScreenDemoSeek.setEnabled(false);
        } else {

            fScreenDemoSeek.setEnabled(true);

            long totalTimeDuration = mXibaVideoPlayer.getDuration();
            long currentTimePosition = mXibaVideoPlayer.getCurrentPositionWhenPlaying();

            //设置视频总时长和当前播放位置
            fScreenCurrentTimeTV.setText(XibaUtil.stringForTime(currentTimePosition));
            fScreenTotalTimeTV.setText(XibaUtil.stringForTime(totalTimeDuration));

            int progress = (int) (currentTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度

            //设置进度条位置
            fScreenDemoSeek.setProgress(progress);
        }

        mFullScreenEventCallback = new FullScreenEventCallback();

    }

    private class FullScreenEventCallback implements XibaVideoPlayerEventCallback {

        @Override
        public void onPlayerPrepare() {
            fScreenPlayBN.setText("暂停");
        }

        @Override
        public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {

            fScreenCurrentTimeTV.setText(XibaUtil.stringForTime(currentTime));
            fScreenTotalTimeTV.setText(XibaUtil.stringForTime(totalTime));

            if (!isTrackingTouchSeekBar) {
                fScreenDemoSeek.setProgress(progress);
            }

            fScreenDemoSeek.setSecondaryProgress(secProgress);
            if (!fScreenDemoSeek.isEnabled()) {
                fScreenDemoSeek.setEnabled(true);
            }

            //如果loading正在显示，在这里隐藏
            if (fScreenLoadingPB.getVisibility() == View.VISIBLE) {
                fScreenLoadingPB.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPlayerPause() {
            fScreenPlayBN.setText("播放");
        }

        @Override
        public void onPlayerResume() {
            fScreenPlayBN.setText("暂停");
        }

        @Override
        public void onPlayerComplete() {
            fScreenPlayBN.setText("播放");
        }

        @Override
        public void onPlayerAutoComplete() {
            fScreenPlayBN.setText("播放");
        }

        @Override
        public void onStartLoading() {
            if (fScreenLoadingPB.getVisibility() != View.VISIBLE) {
                fScreenLoadingPB.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration) {
            int progress = (int) (seekTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度

            //显示前进或后退了多少秒
            if (fScreenPositionChangingInfoTV.getVisibility() != View.VISIBLE) {
                fScreenPositionChangingInfoTV.setVisibility(View.VISIBLE);
            }

            long seekPosition = seekTimePosition - originPosition;
            StringBuilder sb = new StringBuilder();
            if (seekPosition > 0) {
                sb.append("+");
            } else if(seekPosition < 0){
                sb.append("-");
            }
            sb.append(XibaUtil.stringForTime(Math.abs(seekTimePosition - originPosition)));
            fScreenPositionChangingInfoTV.setText(sb.toString());

            //设置进度条
            fScreenDemoSeek.setProgress(progress);
        }

        @Override
        public void onChangingPositionEnd() {
            if (fScreenPositionChangingInfoTV.getVisibility() != View.GONE) {
                fScreenPositionChangingInfoTV.setVisibility(View.GONE);
            }
        }

        @Override
        public void onChangingVolume(int percent) {
            if (fScreenVolumeBrightLL.getVisibility() != View.VISIBLE) {
                fScreenVolumeBrightLL.setVisibility(View.VISIBLE);
            }

            fScreenVolumeBrightTV.setText("音量");
            fScreenVolumeBrightSeek.setProgress(percent);
        }

        @Override
        public void onChangingVolumeEnd() {
            if (fScreenVolumeBrightLL.getVisibility() != View.GONE) {
                fScreenVolumeBrightLL.setVisibility(View.GONE);
            }
        }

        @Override
        public void onChangingBrightness(int percent) {
            if (fScreenVolumeBrightLL.getVisibility() != View.VISIBLE) {
                fScreenVolumeBrightLL.setVisibility(View.VISIBLE);
            }

            fScreenVolumeBrightTV.setText("亮度");
            fScreenVolumeBrightSeek.setProgress(percent);
        }

        @Override
        public void onChangingBrightnessEnd() {
            if (fScreenVolumeBrightLL.getVisibility() != View.GONE) {
                fScreenVolumeBrightLL.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPlayerError(int what, int extra) {

        }

        @Override
        public void onSingleTap() {
            toggleShowHideFullScreenUI();
        }

        @Override
        public void onDoubleTap() {
            mXibaVideoPlayer.togglePlayPause();
        }

        @Override
        public void onTouchLockedScreen() {
            toggleShowHideLockBN();
        }
    }

    /**
     * 显示全部控件
     */
    private void showFullScreenUI() {
        fScreenBottomContainerRL.setVisibility(View.VISIBLE);
        fScreenBackToNormalBN.setVisibility(View.VISIBLE);
        fScreenPlayBN.setVisibility(View.VISIBLE);
        fScreenLockScreenBN.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏全部控件
     */
    private void dismissFullScreenUI() {
        fScreenBottomContainerRL.setVisibility(View.GONE);
        fScreenBackToNormalBN.setVisibility(View.GONE);
        fScreenPlayBN.setVisibility(View.GONE);
        fScreenLockScreenBN.setVisibility(View.GONE);
    }

    /**
     * 显示或隐藏全屏UI控件
     */
    private void toggleShowHideFullScreenUI() {
        if (fScreenBottomContainerRL != null) {
            if (fScreenBottomContainerRL.getVisibility() == View.VISIBLE) {
                dismissFullScreenUI();
            } else {
                showFullScreenUI();
            }
        }
    }

    /**
     * 显示或隐藏锁屏按钮
     */
    private void toggleShowHideLockBN() {
        if (fScreenLockScreenBN != null) {
            if (fScreenLockScreenBN.getVisibility() == View.VISIBLE) {
                fScreenLockScreenBN.setVisibility(View.GONE);
            } else {
                fScreenLockScreenBN.setVisibility(View.VISIBLE);
            }
        }
    }

    public Button getfScreenPlayBN() {
        return fScreenPlayBN;
    }

    public Button getfScreenBackToNormalBN() {
        return fScreenBackToNormalBN;
    }

    public Button getfScreenLockScreenBN() {
        return fScreenLockScreenBN;
    }

    public TextView getfScreenCurrentTimeTV() {
        return fScreenCurrentTimeTV;
    }

    public TextView getfScreenTotalTimeTV() {
        return fScreenTotalTimeTV;
    }

    public SeekBar getfScreenDemoSeek() {
        return fScreenDemoSeek;
    }

    public TextView getfScreenPositionChangingInfoTV() {
        return fScreenPositionChangingInfoTV;
    }

    public RelativeLayout getfScreenBottomContainerRL() {
        return fScreenBottomContainerRL;
    }

    public LinearLayout getfScreenVolumeBrightLL() {
        return fScreenVolumeBrightLL;
    }

    public TextView getfScreenVolumeBrightTV() {
        return fScreenVolumeBrightTV;
    }

    public SeekBar getfScreenVolumeBrightSeek() {
        return fScreenVolumeBrightSeek;
    }

    public boolean isTrackingTouchSeekBar() {
        return isTrackingTouchSeekBar;
    }

    public FullScreenEventCallback geFullScreenEventCallback(){
        return mFullScreenEventCallback;
    }

    /**
     * 释放全屏控件
     */
    public void releaseFullScreenUI() {
        if (fScreenPlayBN != null) {
            fScreenPlayBN = null;
        }
        if (fScreenBackToNormalBN != null) {
            fScreenBackToNormalBN = null;
        }
        if (fScreenLockScreenBN != null) {
            fScreenLockScreenBN = null;
        }
        if (fScreenCurrentTimeTV != null) {
            fScreenCurrentTimeTV = null;
        }
        if (fScreenCurrentTimeTV != null) {
            fScreenCurrentTimeTV = null;
        }
        if (fScreenTotalTimeTV != null) {
            fScreenTotalTimeTV = null;
        }
        if (fScreenDemoSeek != null) {
            fScreenDemoSeek = null;
        }
        if (fScreenPositionChangingInfoTV != null) {
            fScreenPositionChangingInfoTV = null;
        }
        if (fScreenBottomContainerRL != null) {
            fScreenBottomContainerRL = null;
        }
        if (fScreenVolumeBrightLL != null) {
            fScreenVolumeBrightLL = null;
        }
        if (fScreenVolumeBrightTV != null) {
            fScreenVolumeBrightTV = null;
        }
        if (fScreenVolumeBrightSeek != null) {
            fScreenVolumeBrightSeek = null;
        }
        if (mFullScreenEventCallback != null) {
            mFullScreenEventCallback = null;
        }
    }


}