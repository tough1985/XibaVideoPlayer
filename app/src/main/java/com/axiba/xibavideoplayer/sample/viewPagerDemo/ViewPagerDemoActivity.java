package com.axiba.xibavideoplayer.sample.viewPagerDemo;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.XibaListPlayUtil;
import com.axiba.xibavideoplayer.sample.ListDemoActivity;
import com.axiba.xibavideoplayer.sample.R;

/**
 * Created by xiba on 2016/12/21.
 */

public class ViewPagerDemoActivity extends AppCompatActivity {
    public static final String TAG = ViewPagerDemoActivity.class.getSimpleName();

    private ViewPager mPager;

    private PlayerPagerAdapter mAdapter;

    private XibaListPlayUtil mXibaListPlayUtil;

    private PlayerPagerEventCallback  eventCallback;
    private PlayerFullScreenEventCallback mFScreenCallback;

    private String[] urls = {
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=10935&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11528&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11519&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11526&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11525&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11524&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11523&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11522&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11521&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11520&editionType=default"
    };

    private ViewGroup fullScreenContainer;

    private Button fScreenPlayBN;
    private Button fScreenBackToNormalBN;
    private Button fScreenLockScreenBN;
    private TextView fScreenCurrentTimeTV;
    private TextView fScreenTotalTimeTV;
    private SeekBar fScreenDemoSeek;
    private TextView fScreenPositionChangingInfoTV;
    private RelativeLayout fScreenBottomContainerRL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_demo);

        mAdapter = new PlayerPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.player_list_VP);

        mPager.setAdapter(mAdapter);

        eventCallback = new PlayerPagerEventCallback();

        mFScreenCallback = new PlayerFullScreenEventCallback();

        mXibaListPlayUtil = new XibaListPlayUtil(this);
//        mXibaListPlayUtil.setPlayingItemPositionChangeImpl(new XibaListPlayUtil.PlayingItemPositionChange() {
//            @Override
//            public void prePlayingItemPositionChange(int position, int targetPosition) {
//                eventCallback.unbindPlayerUI();
//            }
//
//            @Override
//            public void prePlayingItemPositionChange(Message utilMsg) {
//
//            }
//        });


    }

    private class PlayerPagerAdapter extends FragmentStatePagerAdapter {

        public PlayerPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public int getCount() {
            return urls.length;
        }

        @Override
        public Fragment instantiateItem(ViewGroup container, int position) {
            PlayerFragment playerFragment = (PlayerFragment) super.instantiateItem(container, position);

            return playerFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mXibaListPlayUtil.removePlayer(position);
            super.destroyItem(container, position, object);

        }

        @Override
        public Fragment getItem(int position) {
            return PlayerFragment.newInstance(urls[position], position);
        }

    }

    /**
     * 全屏事件监听
     */
    public class PlayerFullScreenEventCallback implements XibaFullScreenEventCallback{

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

        @Override
        public ViewGroup onEnterFullScreen() {
            ViewGroup contentView = (ViewGroup) ViewPagerDemoActivity.this.findViewById(Window.ID_ANDROID_CONTENT);

            fullScreenContainer = (ViewGroup) getLayoutInflater()
                    .inflate(R.layout.activity_simple_demo_fullscreen, contentView, false);

            String playText = play.getText().toString();
            String currentTimeText = currentTimeTV.getText().toString();
            String totalTimeText = totalTimeTV.getText().toString();
            int progress = demoSeek.getProgress();
            boolean seekEnable = demoSeek.isEnabled();
            initFullScreenUI(playText, currentTimeText, totalTimeText, progress, seekEnable);

            return fullScreenContainer;
        }

        @Override
        public void onQuitFullScreen() {
            releaseFullScreenUI();
        }
    }

    /**
     * 初始化全屏控件
     */
    private void initFullScreenUI(String playText, String currentTimeText, String totalTimeText, int progress, boolean seekEnable){

        if (fullScreenContainer != null){
            fScreenPlayBN = (Button) fullScreenContainer.findViewById(R.id.demo_play);
            fScreenBackToNormalBN = (Button) fullScreenContainer.findViewById(R.id.back_to_normal_BN);
            fScreenLockScreenBN = (Button) fullScreenContainer.findViewById(R.id.lock_screen_BN);
            fScreenCurrentTimeTV = (TextView) fullScreenContainer.findViewById(R.id.current_time);
            fScreenTotalTimeTV = (TextView) fullScreenContainer.findViewById(R.id.total_time);
            fScreenDemoSeek = (SeekBar) fullScreenContainer.findViewById(R.id.demo_seek);
            fScreenPositionChangingInfoTV = (TextView) fullScreenContainer.findViewById(R.id.position_changing_info_TV);
            fScreenBottomContainerRL = (RelativeLayout) fullScreenContainer.findViewById(R.id.full_screen_bottom_container_RL);

            fScreenPlayBN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mXibaListPlayUtil.togglePlay();
                }
            });

            fScreenBackToNormalBN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mXibaListPlayUtil.quitFullScreen();
                }
            });

            fScreenLockScreenBN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mXibaListPlayUtil.isScreenLock()) {
                        mXibaListPlayUtil.lockScreen(false);
                        fScreenLockScreenBN.setText("锁屏");
                        showFullScreenUI();     //显示全部控件
                    } else {
                        mXibaListPlayUtil.lockScreen(true);
                        fScreenLockScreenBN.setText("解锁");
                        dismissFullScreenUI();  //隐藏全部控件
                    }
                }
            });

            fScreenPlayBN.setText(playText);
            fScreenCurrentTimeTV.setText(currentTimeText);
            fScreenTotalTimeTV.setText(totalTimeText);
            fScreenDemoSeek.setProgress(progress);
            fScreenDemoSeek.setEnabled(seekEnable);

        }
    }

    /**
     * 释放全屏控件
     */
    private void releaseFullScreenUI(){
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
    }

    /**
     * 显示全部控件
     */
    private void showFullScreenUI(){
        fScreenBottomContainerRL.setVisibility(View.VISIBLE);
        fScreenBackToNormalBN.setVisibility(View.VISIBLE);
        fScreenPlayBN.setVisibility(View.VISIBLE);
        fScreenLockScreenBN.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏全部控件
     */
    private void dismissFullScreenUI(){
        fScreenBottomContainerRL.setVisibility(View.GONE);
        fScreenBackToNormalBN.setVisibility(View.GONE);
        fScreenPlayBN.setVisibility(View.GONE);
        fScreenLockScreenBN.setVisibility(View.GONE);
    }

    /**
     * 显示或隐藏全屏UI控件
     */
    private void toggleShowHideFullScreenUI(){
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
    private void toggleShowHideLockBN(){
        if (fScreenLockScreenBN != null) {
            if (fScreenLockScreenBN.getVisibility() == View.VISIBLE) {
                fScreenLockScreenBN.setVisibility(View.GONE);
            } else {
                fScreenLockScreenBN.setVisibility(View.VISIBLE);
            }
        }
    }

    public XibaListPlayUtil getXibaListPlayUtil(){
        return mXibaListPlayUtil;
    }

    public PlayerPagerEventCallback getEventCallback(){
        return eventCallback;
    }

    public PlayerFullScreenEventCallback getFScreenEventCallback(){
        return mFScreenCallback;
    }

    @Override
    public void onBackPressed() {
        if (mXibaListPlayUtil.onBackPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mXibaListPlayUtil.release();
        super.onDestroy();
    }

}
