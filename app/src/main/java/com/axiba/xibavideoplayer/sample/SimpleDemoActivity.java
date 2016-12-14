package com.axiba.xibavideoplayer.sample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2016/11/26.
 */
public class SimpleDemoActivity extends Activity implements XibaVideoPlayerEventCallback{

    public static final String TAG = SimpleDemoActivity.class.getSimpleName();

    private XibaVideoPlayer xibaVP;
    private Button play;
    private TextView currentTimeTV;
    private TextView totalTimeTV;
    private SeekBar demoSeek;
    private TextView positionChangingInfoTV;
    private SeekBar volumeBrightSeek;
    private Button fullScreenBN;
    private Button tinyScreenBN;

    private Button backToNormalBN;
    private ViewGroup fullScreenContainer;

    private Button lockScreenBN;
    private RelativeLayout fullScreenBottomContainerRL;

    private StartButtonListener mStartButtonListener;
    private SeekProgressListener mSeekProgressListener;

    private StartFullScreenListener mStartFullScreenListener;   //全屏按钮监听
    private BackToNormalListener mBackToNormalListener; //退出全屏监听

    private boolean isTrackingTouchSeekBar = false;     //是否正在控制SeekBar

    String url = "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11086&editionType=default";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_demo);

        xibaVP = (XibaVideoPlayer) findViewById(R.id.demo_xibaVP);
        play = (Button) findViewById(R.id.demo_play);
        currentTimeTV = (TextView) findViewById(R.id.current_time);
        totalTimeTV = (TextView) findViewById(R.id.total_time);
        demoSeek = (SeekBar) findViewById(R.id.demo_seek);
        positionChangingInfoTV = (TextView) findViewById(R.id.position_changing_info_TV);
        volumeBrightSeek = (SeekBar) findViewById(R.id.volume_bright_seek);
        fullScreenBN = (Button) findViewById(R.id.full_screen_BN);
        tinyScreenBN = (Button) findViewById(R.id.tiny_screen_BN);

        xibaVP.setUp(url, 0, new Object[]{});
        xibaVP.setEventCallback(this);
        xibaVP.setAutoRotate(true);

        //初始化监听
        mStartButtonListener = new StartButtonListener();
        mSeekProgressListener = new SeekProgressListener();
        mStartFullScreenListener = new StartFullScreenListener();

        setListeners();
        demoSeek.setEnabled(false);

        //设置全屏按钮监听
        fullScreenBN.setOnClickListener(mStartFullScreenListener);

        //设置小屏按钮监听
        tinyScreenBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (xibaVP.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                    xibaVP.quitTinyScreen();
                } else {
                    xibaVP.startTinyScreen(new Point(500, 300), 600, 1400, true);
                }
            }
        });
    }

    /**
     * 根据屏幕状态，重新加载控件
     * @param isNormalScreen true 正常屏幕; false 全屏
     */
    private void initUIByScreenType(boolean isNormalScreen){

        //保存控件状态
        String tempPlayBNState = play.getText().toString();
        String tempCurrentTime = currentTimeTV.getText().toString();
        String tempTotalTime = totalTimeTV.getText().toString();
        int progress = demoSeek.getProgress();

        if (isNormalScreen) {
            initNormalScreenUI();
        } else {
            initFullScreenUI();
        }

        //恢复控件状态
        play.setText(tempPlayBNState);
        currentTimeTV.setText(tempCurrentTime);
        totalTimeTV.setText(tempTotalTime);
        demoSeek.setProgress(progress);

        //重新添加监听
        setListeners();
    }

    /**
     * 初始化正常屏幕控件
     */
    private void initNormalScreenUI(){
        //重新初始化控件
        play = (Button) findViewById(R.id.demo_play);
        currentTimeTV = (TextView) findViewById(R.id.current_time);
        totalTimeTV = (TextView) findViewById(R.id.total_time);
        demoSeek = (SeekBar) findViewById(R.id.demo_seek);
        positionChangingInfoTV = (TextView) findViewById(R.id.position_changing_info_TV);
    }

    /**
     * 初始化全屏控件
     */
    private void initFullScreenUI(){
        //重新初始化控件
        play = (Button) fullScreenContainer.findViewById(R.id.demo_play);
        currentTimeTV = (TextView) fullScreenContainer.findViewById(R.id.current_time);
        totalTimeTV = (TextView) fullScreenContainer.findViewById(R.id.total_time);
        demoSeek = (SeekBar) fullScreenContainer.findViewById(R.id.demo_seek);
        positionChangingInfoTV = (TextView) fullScreenContainer.findViewById(R.id.position_changing_info_TV);
    }

    /**
     * 设置监听
     */
    private void setListeners(){
        play.setOnClickListener(mStartButtonListener);
        demoSeek.setOnSeekBarChangeListener(mSeekProgressListener);
    }

    @Override
    protected void onPause() {
        xibaVP.pausePlayer();
        super.onPause();
    }

    @Override
    protected void onResume() {
        xibaVP.resumePlayer();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (xibaVP.onBackPress()) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --XibaVideoPlayerEventCallback override methods start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    @Override
    public void onPlayerPrepare() {
        demoSeek.setEnabled(true);
        play.setText("暂停");
    }

    @Override
    public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {

//        currentTimeTV.setText("currentTime=" + XibaUtil.stringForTime(currentTime) + " : secProgress=" + secProgress);
//        totalTimeTV.setText("totalTime=" +  XibaUtil.stringForTime(totalTime));

        currentTimeTV.setText(XibaUtil.stringForTime(currentTime));
        totalTimeTV.setText(XibaUtil.stringForTime(totalTime));

        if (!isTrackingTouchSeekBar) {
            demoSeek.setProgress(progress);
        }
        demoSeek.setSecondaryProgress(secProgress);

        if (!demoSeek.isEnabled()) {
            demoSeek.setEnabled(true);
        }
    }

    @Override
    public void onPlayerPause() {
        play.setText("播放");
    }

    @Override
    public void onPlayerResume() {
        play.setText("暂停");
    }

    @Override
    public void onPlayerComplete() {
        play.setText("播放");
    }

    @Override
    public void onPlayerAutoComplete() {
        play.setText("播放");
    }

    @Override
    public void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration) {
        if (positionChangingInfoTV.getVisibility() != View.VISIBLE) {
            positionChangingInfoTV.setVisibility(View.VISIBLE);
        }

        long seekPosition = seekTimePosition - originPosition;
        StringBuilder sb = new StringBuilder();
        if (seekPosition > 0) {
            sb.append("+");
        } else if(seekPosition < 0){
            sb.append("-");
        }
        sb.append(XibaUtil.stringForTime(Math.abs(seekTimePosition - originPosition)));
        positionChangingInfoTV.setText(sb.toString());

        int progress = (int) (seekTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度
        demoSeek.setProgress(progress);
    }

    @Override
    public void onChangingPositionEnd() {
        if (positionChangingInfoTV.getVisibility() != View.GONE) {
            positionChangingInfoTV.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChangingVolume(int percent) {
        if (volumeBrightSeek.getVisibility() != View.VISIBLE) {
            volumeBrightSeek.setVisibility(View.VISIBLE);
        }

        volumeBrightSeek.setProgress(percent);
    }

    @Override
    public void onChangingVolumeEnd() {
        if (volumeBrightSeek.getVisibility() != View.GONE) {
            volumeBrightSeek.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChangingBrightness(int percent) {
        if (volumeBrightSeek.getVisibility() != View.VISIBLE) {
            volumeBrightSeek.setVisibility(View.VISIBLE);
        }

        volumeBrightSeek.setProgress(percent);
    }

    @Override
    public void onChangingBrightnessEnd() {
        if (volumeBrightSeek.getVisibility() != View.GONE) {
            volumeBrightSeek.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayerError(int what, int extra) {

    }

    @Override
    public ViewGroup onEnterFullScreen() {
        ViewGroup contentView = (ViewGroup) SimpleDemoActivity.this.findViewById(Window.ID_ANDROID_CONTENT);

        fullScreenContainer = (ViewGroup) getLayoutInflater()
                .inflate(R.layout.activity_simple_demo_fullscreen, contentView, false);

        //初始化全屏控件
        initUIByScreenType(false);

        fullScreenBottomContainerRL = (RelativeLayout) fullScreenContainer.findViewById(R.id.full_screen_bottom_container_RL);

        //退出全屏按钮
        backToNormalBN = (Button) fullScreenContainer.findViewById(R.id.back_to_normal_BN);
        backToNormalBN.setOnClickListener(getBackToNormalListener());

        //锁屏
        lockScreenBN = (Button) fullScreenContainer.findViewById(R.id.lock_screen_BN);
        lockScreenBN.setOnClickListener(new LockScreenListener());

        return fullScreenContainer;
    }

    @Override
    public void onQuitFullScreen() {
        //初始化普通屏控件
        initUIByScreenType(true);
    }

    @Override
    public void onEnterTinyScreen() {
        tinyScreenBN.setText("退出小屏");
    }

    @Override
    public void onQuitTinyScreen() {
        tinyScreenBN.setText("小屏");
    }

    @Override
    public void onSingleTap() {
        if (xibaVP.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
            toggleShowHideFullScreenUI();
        }
    }

    @Override
    public void onDoubleTap() {
        xibaVP.togglePlayPause();
    }

    @Override
    public void onTouchLockedScreen() {
        //显示或隐藏锁屏按钮
        toggleShowHideLockBN();
    }

    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --XibaVideoPlayerEventCallback methods end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑


    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --StartButtonOnClickListener start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    private class StartButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            xibaVP.togglePlayPause();
        }
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --StartButtonOnClickListener end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --SeekProgressListener start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    private class SeekProgressListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTrackingTouchSeekBar = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            xibaVP.seekTo(seekBar.getProgress());
            isTrackingTouchSeekBar = false;
        }
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --SeekProgressListener end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --BackToNormalListener start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    private class BackToNormalListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
//            //初始化全屏控件
//            initUIByScreenType(true);

            xibaVP.quitFullScreen();
        }
    }

    private BackToNormalListener getBackToNormalListener(){
        if (mBackToNormalListener == null) {
            mBackToNormalListener = new BackToNormalListener();
        }
        return mBackToNormalListener;
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --BackToNormalListener end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --StartFullScreenListener start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    private class StartFullScreenListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
//            startFullScreen();

            xibaVP.startFullScreen(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            xibaVP.setAutoRotate(false);
        }
    }

//    private void startFullScreen(){
//        ViewGroup contentView = (ViewGroup) SimpleDemoActivity.this.findViewById(Window.ID_ANDROID_CONTENT);
//
//        fullScreenContainer = (ViewGroup) getLayoutInflater()
//                .inflate(R.layout.activity_simple_demo_fullscreen, contentView, false);
//
//        //初始化全屏控件
//        initUIByScreenType(false);
//
//        fullScreenBottomContainerRL = (RelativeLayout) fullScreenContainer.findViewById(R.id.full_screen_bottom_container_RL);
//
//        //退出全屏按钮
//        backToNormalBN = (Button) fullScreenContainer.findViewById(R.id.back_to_normal_BN);
//        backToNormalBN.setOnClickListener(getBackToNormalListener());
//
//        //锁屏
//        lockScreenBN = (Button) fullScreenContainer.findViewById(R.id.lock_screen_BN);
//        lockScreenBN.setOnClickListener(new LockScreenListener());
//
//        boolean hasActionBar = false;
//        if(getSupportActionBar() != null) hasActionBar = true;
//
//        xibaVP.startFullScreen(fullScreenContainer, hasActionBar, true);
//    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --StartFullScreenListener end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 锁屏按钮监听
     */
    private class LockScreenListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if (xibaVP.isScreenLock()) {
                xibaVP.setScreenLock(false);
                lockScreenBN.setText("锁屏");
                showFullScreenUI();     //显示全部控件
            } else {
                xibaVP.setScreenLock(true);
                lockScreenBN.setText("解锁");
                dismissFullScreenUI();  //隐藏全部控件
            }
        }
    }

    /**
     * 显示全部控件
     */
    private void showFullScreenUI(){
        fullScreenBottomContainerRL.setVisibility(View.VISIBLE);
        backToNormalBN.setVisibility(View.VISIBLE);
        play.setVisibility(View.VISIBLE);
        lockScreenBN.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏全部控件
     */
    private void dismissFullScreenUI(){
        fullScreenBottomContainerRL.setVisibility(View.GONE);
        backToNormalBN.setVisibility(View.GONE);
        play.setVisibility(View.GONE);
        lockScreenBN.setVisibility(View.GONE);
    }

    /**
     * 显示或隐藏全屏UI控件
     */
    private void toggleShowHideFullScreenUI(){
        if (fullScreenBottomContainerRL != null) {
            if (fullScreenBottomContainerRL.getVisibility() == View.VISIBLE) {
                dismissFullScreenUI();
            } else {
                showFullScreenUI();
            }
        }
    }

    /**
     * 显示或隐藏锁屏按钮
     */
    private void toggleShowHideLockBN(){
        if (lockScreenBN != null) {
            if (lockScreenBN.getVisibility() == View.VISIBLE) {
                lockScreenBN.setVisibility(View.GONE);
            } else {
                lockScreenBN.setVisibility(View.VISIBLE);
            }
        }
    }
}
