package com.axiba.xibavideoplayer.sample.viewPagerWithRecyclerView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.listUtils.XibaPagerWithListUtil;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.sample.recyclerViewDemo.RecyclerViewDemoActivity;
import com.axiba.xibavideoplayer.sample.view.FullScreenContainer;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2017/2/21.
 */

public class PagerWithRecyclerActivity extends AppCompatActivity {

    public static final String TAG = PagerWithRecyclerActivity.class.getSimpleName();

    private String[] urls;

    private ViewPager mPager;
    private PagerWithRecyclerAdapter mPagerAdapter;

    //播放器回调事件
    private PagerWithRecyclerEventCallback mEventCallback;
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
        mPagerAdapter = new PagerWithRecyclerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mXibaPagerWithListUtil = new XibaPagerWithListUtil(this);

        mEventCallback = new PagerWithRecyclerEventCallback(mXibaPagerWithListUtil);
        mFScreenCallback = new PlayerFullScreenEventCallback();
    }

    private class PagerWithRecyclerAdapter extends FragmentStatePagerAdapter {

        public PagerWithRecyclerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PagerWithRecyclerFragment.newInstance(urls, position);
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Fragment instantiateItem(ViewGroup container, int position) {
            PagerWithRecyclerFragment pagerWithListFragment = (PagerWithRecyclerFragment) super.instantiateItem(container, position);

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

        private PagerWithRecyclerFragment.PagerWithRecyclerAdapter.PlayerViewHolder holder;

        public PagerWithRecyclerFragment.PagerWithRecyclerAdapter.PlayerViewHolder getHolder() {
            return holder;
        }

        public void setHolder(PagerWithRecyclerFragment.PagerWithRecyclerAdapter.PlayerViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public ViewGroup onEnterFullScreen() {

            mFullScreenContainer = new FullScreenContainer(PagerWithRecyclerActivity.this);

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

            //重新设置item的UI状态
            //这里不能像ListView中使用保存播放状态的方式处理
            if (holder != null) {
                resetUI(holder);
            }

            //绑定List的eventCallback
            mXibaPagerWithListUtil.setEventCallback(mEventCallback);
            mXibaPagerWithListUtil.setPlayerActionEventCallback(null);
        }
    }

    private void resetUI(PagerWithRecyclerFragment.PagerWithRecyclerAdapter.PlayerViewHolder holder) {
        //设置播放按钮状态
        if (mXibaPagerWithListUtil.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
            holder.startBN.setText("暂停");
        } else {
            holder.startBN.setText("播放");
        }

        //如果视频未加载，进度条不可用
        if (mXibaPagerWithListUtil.getCurrentState() == XibaVideoPlayer.STATE_NORMAL
                || mXibaPagerWithListUtil.getCurrentState() == XibaVideoPlayer.STATE_ERROR) {

            holder.progressSeek.setEnabled(false);
        } else {

            holder.progressSeek.setEnabled(true);

            long totalTimeDuration = mXibaPagerWithListUtil.getDuration();
            long currentTimePosition = mXibaPagerWithListUtil.getCurrentPosition();

            //设置视频总时长和当前播放位置
            holder.currentTimeTV.setText(XibaUtil.stringForTime(currentTimePosition));
            holder.totalTimeTV.setText(XibaUtil.stringForTime(totalTimeDuration));

            int progress = (int) (currentTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度

            //设置进度条位置
            holder.progressSeek.setProgress(progress);
        }
    }

    public XibaPagerWithListUtil getXibaPagerWithListUtil(){
        return mXibaPagerWithListUtil;
    }

    public PagerWithRecyclerEventCallback getEventCallback(){
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
