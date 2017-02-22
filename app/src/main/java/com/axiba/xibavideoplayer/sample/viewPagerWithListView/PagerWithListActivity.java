package com.axiba.xibavideoplayer.sample.viewPagerWithListView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.listUtils.XibaPagerWithListUtil;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.sample.view.FullScreenContainer;
import com.axiba.xibavideoplayer.sample.viewPagerDemo.PlayerFragment;

/**
 * Created by xiba on 2017/2/20.
 */

public class PagerWithListActivity extends AppCompatActivity {

    public static final String TAG = PagerWithListActivity.class.getSimpleName();

    private String[] urls;

    private ViewPager mPager;
    private PagerWithListAdapter mPagerAdapter;

    //播放器回调事件
    private PagerWithListEventCallback mEventCallback;
    //全屏回调事件
    private PlayerFullScreenEventCallback mFScreenCallback;

    private XibaPagerWithListUtil mXibaPagerWithListUtil;

    //全屏容器
    private FullScreenContainer mFullScreenContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_demo);

        urls = this.getResources().getStringArray(R.array.urls);

        mPager = (ViewPager) findViewById(R.id.player_list_VP);
        mPagerAdapter = new PagerWithListAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mXibaPagerWithListUtil = new XibaPagerWithListUtil(this);

        mEventCallback = new PagerWithListEventCallback(mXibaPagerWithListUtil);
        mFScreenCallback = new PlayerFullScreenEventCallback();
    }

    private class PagerWithListAdapter extends FragmentStatePagerAdapter{

        public PagerWithListAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PagerWithListFragment.newInstance(urls, position);
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Fragment instantiateItem(ViewGroup container, int position) {
            PagerWithListFragment pagerWithListFragment = (PagerWithListFragment) super.instantiateItem(container, position);

            return pagerWithListFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int fragmentIndex, Object object) {
            //TODO
            mXibaPagerWithListUtil.removePlayer(fragmentIndex);
            super.destroyItem(container, fragmentIndex, object);
        }
    }

    /**
     * 全屏事件监听
     */
    public class PlayerFullScreenEventCallback implements XibaFullScreenEventCallback {

        @Override
        public ViewGroup onEnterFullScreen() {

            mFullScreenContainer = new FullScreenContainer(PagerWithListActivity.this);

            //初始化全屏控件
            mFullScreenContainer.initUI(mXibaPagerWithListUtil.getXibaVideoPlayer());

            mXibaPagerWithListUtil.setEventCallback(mFullScreenContainer.getFullScreenEventCallback());
            mXibaPagerWithListUtil.setPlayerActionEventCallback(mFullScreenContainer.getFullScreenEventCallback());

            //全屏状态下，垂直滑动左侧改变亮度，右侧改变声音
            mXibaPagerWithListUtil.setFullScreenVerticalFeature(XibaVideoPlayer.SLIDING_VERTICAL_LEFT_BRIGHTNESS);

            //全屏状态下，水平滑动改变播放位置
            mXibaPagerWithListUtil.setFullScreenHorizontalFeature(XibaVideoPlayer.SLIDING_HORIZONTAL_CHANGE_POSITION);

            //全屏状态下，水平滑动改变位置的总量为屏幕的 1/4
            mXibaPagerWithListUtil.setHorizontalSlopInfluenceValue(4);
            return mFullScreenContainer;
        }

        @Override
        public void onQuitFullScreen() {
            mFullScreenContainer.releaseFullScreenUI();

            //重新设置UI
            mXibaPagerWithListUtil.savePlayerInfo();

            //绑定List的eventCallback
            mXibaPagerWithListUtil.setEventCallback(mEventCallback);
            mXibaPagerWithListUtil.setPlayerActionEventCallback(null);
        }
    }

    public XibaPagerWithListUtil getXibaPagerWithListUtil(){
        return mXibaPagerWithListUtil;
    }

    public PagerWithListEventCallback getEventCallback(){
        return mEventCallback;
    }

    public PlayerFullScreenEventCallback getFScreenEventCallback(){
        return mFScreenCallback;
    }

    @Override
    public void onBackPressed() {
        if (mXibaPagerWithListUtil.onBackPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mXibaPagerWithListUtil.release();
        super.onDestroy();
    }
}
