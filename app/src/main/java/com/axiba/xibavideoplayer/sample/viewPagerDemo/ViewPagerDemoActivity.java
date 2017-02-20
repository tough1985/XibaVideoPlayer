package com.axiba.xibavideoplayer.sample.viewPagerDemo;

import android.os.Bundle;
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

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.XibaListPlayUtil;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.sample.recyclerViewDemo.RecyclerViewDemoActivity;
import com.axiba.xibavideoplayer.sample.view.FullScreenContainer;

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

    private String[] urls;

    //全屏容器
    private FullScreenContainer mFullScreenContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_demo);

        urls = this.getResources().getStringArray(R.array.urls);

        mAdapter = new PlayerPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.player_list_VP);

        mPager.setAdapter(mAdapter);


        mXibaListPlayUtil = new XibaListPlayUtil(this);

        eventCallback = new PlayerPagerEventCallback(mXibaListPlayUtil);

        mFScreenCallback = new PlayerFullScreenEventCallback();



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


        private PlayerFragment playerFragment;

        public void bindingPlayerUI(PlayerFragment playerFragment) {
            this.playerFragment = playerFragment;
        }

        @Override
        public ViewGroup onEnterFullScreen() {

            mFullScreenContainer = new FullScreenContainer(ViewPagerDemoActivity.this);

            //初始化全屏控件
            mFullScreenContainer.initUI(mXibaListPlayUtil.getXibaVideoPlayer());

            mXibaListPlayUtil.setEventCallback(mFullScreenContainer.geFullScreenEventCallback());

            //全屏状态下，垂直滑动左侧改变亮度，右侧改变声音
            mXibaListPlayUtil.setFullScreenVerticalFeature(XibaVideoPlayer.SLIDING_VERTICAL_LEFT_BRIGHTNESS);

            //全屏状态下，水平滑动改变播放位置
            mXibaListPlayUtil.setFullScreenHorizontalFeature(XibaVideoPlayer.SLIDING_HORIZONTAL_CHANGE_POSITION);

            //全屏状态下，水平滑动改变位置的总量为屏幕的 1/4
            mXibaListPlayUtil.setHorizontalSlopInfluenceValue(4);
            return mFullScreenContainer;
        }

        @Override
        public void onQuitFullScreen() {
            mFullScreenContainer.releaseFullScreenUI();

            //重新设置UI
            playerFragment.resetUI();

            //绑定List的eventCallback
            mXibaListPlayUtil.setEventCallback(eventCallback);

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
