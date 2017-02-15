package com.axiba.xibavideoplayer.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.XibaListPlayUtil;
import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.utils.XibaUtil;


/**
 * Created by xiba on 2016/12/11.
 */

public class ListDemoActivity extends AppCompatActivity {
    public static final String TAG = ListDemoActivity.class.getSimpleName();

    private ListView playerList;
    private PlayerListAdapter adapter;
    private ListEventCallback eventCallback;

    private ListFullScreenEventCallback mFScreenEventCallback;

    private XibaListPlayUtil mXibaListPlayUtil;

    private boolean isTrackingTouchSeekBar = false;     //是否正在控制SeekBar

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

    private Message mUtilMsg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_demo);

        playerList = (ListView) findViewById(R.id.player_list);
        adapter = new PlayerListAdapter(this);
        playerList.setAdapter(adapter);

        mXibaListPlayUtil = new XibaListPlayUtil(this);
        mXibaListPlayUtil.setPlayingItemPositionChangeImpl(new XibaListPlayUtil.PlayingItemPositionChange() {

            @Override
            public void prePlayingItemPositionChange(Message utilMsg) {
                mUtilMsg = utilMsg;

            }

            @Override
            public void prePlayingItemChangeOnPause() {
                if (eventCallback != null && eventCallback.getHolder() != null) {
                    eventCallback.getHolder().progressSeek.setEnabled(false);

                    //如果loading正在显示，在这里隐藏
                    if (eventCallback.getHolder().loadingPB.getVisibility() == View.VISIBLE) {
                        eventCallback.getHolder().loadingPB.setVisibility(View.GONE);
                    }

                    eventCallback.changeHolder();
                }
            }
        });

        eventCallback = new ListEventCallback();
        mFScreenEventCallback = new ListFullScreenEventCallback();
    }

    private class PlayerListAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        public PlayerListAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return urls.length;
        }

        @Override
        public String getItem(int position) {
            return urls[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.player_list_item, parent, false);
                holder = new ViewHolder();
                holder.container = (FrameLayout) convertView.findViewById(R.id.player_list_item_container);
                holder.startBN = (Button) convertView.findViewById(R.id.player_list_item_play);
                holder.fullscreenBN = (Button) convertView.findViewById(R.id.player_list_item_fullscreen);
                holder.currentTimeTV = (TextView) convertView.findViewById(R.id.player_list_item_current_time);
                holder.totalTimeTV = (TextView) convertView.findViewById(R.id.player_list_item_total_time);
                holder.progressSeek = (SeekBar) convertView.findViewById(R.id.player_list_item_demo_seek);
//                holder.cacheIV = (ImageView) convertView.findViewById(R.id.player_list_item_cache_IV);
                holder.tinyscreenBN = (Button) convertView.findViewById(R.id.player_list_item_tinyscreen);
                holder.loadingPB = (ProgressBar) convertView.findViewById(R.id.player_list_item_loading_PB);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.startBN.setOnClickListener(new StartListener(holder, position, getItem(position)));
            holder.fullscreenBN.setOnClickListener(new FullScreenListener(holder, position, getItem(position)));
            holder.tinyscreenBN.setOnClickListener(new TinyScreenListener(holder, position, getItem(position)));
            holder.progressSeek.setOnSeekBarChangeListener(new SeekProgressListener(holder, position, getItem(position)));

            /**
             * 调用XibaListPlayUtil.resolveItem来判断播放器是否添加到当前的item中
             * 并根据返回的PlayerStateInfo来决定Item中其他控件的状态
             */
            XibaListPlayUtil.PlayerStateInfo playerStateInfo = mXibaListPlayUtil.resolveItem(position, holder.container, eventCallback);

            Log.e(TAG, "position" + position);
            Log.e(TAG, "playerStateInfo == null?" + (playerStateInfo == null));

            initHolderUIByPlayerInfo(playerStateInfo, holder, position);

            return convertView;
        }
    }

    private class ViewHolder{
        FrameLayout container;
        Button startBN;
        Button fullscreenBN;
        TextView currentTimeTV;
        TextView totalTimeTV;
        SeekBar progressSeek;
        Button tinyscreenBN;
        ProgressBar loadingPB;
//        ImageView cacheIV;
    }

    /**
     * 播放按钮监听
     */
    private class StartListener implements View.OnClickListener{
        private ViewHolder holder;
        private int position;
        private String url;

        public StartListener(ViewHolder holder, int position, String url) {
            this.holder = holder;
            this.position = position;
            this.url = url;
        }

        @Override
        public void onClick(View v) {

            eventCallback.bindHolder(holder, position);
            mXibaListPlayUtil.togglePlay(url, position, holder.container, eventCallback);
        }
    }

    /**
     * 全屏按钮监听
     */
    private class FullScreenListener implements View.OnClickListener{
        private ViewHolder holder;
        private int position;
        private String url;

        public FullScreenListener(ViewHolder holder, int position, String url) {
            this.holder = holder;
            this.position = position;
            this.url = url;
        }

        @Override
        public void onClick(View v) {
            mFScreenEventCallback.setHolder(holder);
            mXibaListPlayUtil.startFullScreen(url, position, holder.container, eventCallback, mFScreenEventCallback);
            eventCallback.bindHolder(holder, position);
        }
    }

    /**
     * 小屏按钮监听
     */
    private class TinyScreenListener implements View.OnClickListener{
        private ViewHolder holder;
        private int position;
        private String url;

        public TinyScreenListener(ViewHolder holder, int position, String url) {
            this.holder = holder;
            this.position = position;
            this.url = url;
        }

        @Override
        public void onClick(View v) {

            mXibaListPlayUtil.toggleTinyScreen(url, position, holder.container, eventCallback, new Point(500, 300), 600, 1400, true);
            eventCallback.bindHolder(holder, position);

            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                holder.tinyscreenBN.setText("返回");
            } else {
                holder.tinyscreenBN.setText("小屏");
            }
        }
    }

    private class SeekProgressListener implements SeekBar.OnSeekBarChangeListener{

        private ViewHolder holder;
        private int position;
        private String url;

        public SeekProgressListener(ViewHolder holder, int position, String url) {
            this.holder = holder;
            this.position = position;
            this.url = url;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTrackingTouchSeekBar = true;

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            eventCallback.bindHolder(holder, position);

            mXibaListPlayUtil.seekTo(url, position, holder.container,
                    eventCallback, holder.progressSeek.getProgress(), holder.progressSeek.getMax());

            isTrackingTouchSeekBar = false;
        }
    }

    /**
     * 主要解决暂停问题
     * @param playerStateInfo
     * @param holder
     */
    private void initHolderUIByPlayerInfo(XibaListPlayUtil.PlayerStateInfo playerStateInfo, ViewHolder holder, int position){
        if (playerStateInfo != null) {
            if (playerStateInfo.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
                holder.startBN.setText("暂停");
            } else {
                holder.startBN.setText("播放");
            }

            long totalTimeDuration = playerStateInfo.getDuration();
            long currentTimePosition = playerStateInfo.getPosition();

            holder.currentTimeTV.setText(XibaUtil.stringForTime(currentTimePosition));
            holder.totalTimeTV.setText(XibaUtil.stringForTime(totalTimeDuration));

            int progress = (int) (currentTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度


            holder.progressSeek.setProgress(progress);

            if (mXibaListPlayUtil.getPlayingPosition() == position) {
                holder.progressSeek.setEnabled(true);
            } else {
                holder.progressSeek.setEnabled(false);
            }

            if (playerStateInfo.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                holder.tinyscreenBN.setText("返回");
            } else {
                holder.tinyscreenBN.setText("小屏");
            }
        } else {
            holder.startBN.setText("播放");
            holder.currentTimeTV.setText("00:00");
            holder.totalTimeTV.setText("00:00");
            holder.progressSeek.setProgress(0);
            holder.progressSeek.setEnabled(false);
            holder.tinyscreenBN.setText("小屏");
        }

        /**
         * 如果eventCallback还没有绑定UI 或者 当前Item就是播放Item
         */
//        if (mXibaListPlayUtil.getPlayingPosition() == position || !eventCallback.isBinding()) {
        if (mXibaListPlayUtil.getPlayingPosition() == position
//                || eventCallback.getHolder() == null
                ) {
            eventCallback.bindHolder(holder, position);
        }
    }

    /**
     * 初始化全屏控件
     */
    private void initFullScreenUI(ViewHolder holder){

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

            if (holder != null) {
                fScreenPlayBN.setText(holder.startBN.getText());
                fScreenCurrentTimeTV.setText(holder.currentTimeTV.getText());
                fScreenTotalTimeTV.setText(holder.totalTimeTV.getText());
                fScreenDemoSeek.setProgress(holder.progressSeek.getProgress());
                fScreenDemoSeek.setEnabled(holder.progressSeek.isEnabled());
            }

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
     * 播放器回调事件
     */
    private class ListEventCallback implements XibaVideoPlayerEventCallback{

        private ViewHolder holder;

        private ViewHolder nextHolder;

        public void bindHolder(ViewHolder holder, int position) {

            if (this.holder == null || mXibaListPlayUtil.getPlayingPosition() == position) {
                this.holder = holder;
            } else {
                this.nextHolder = holder;
            }

        }

        public void changeHolder(){
            if (nextHolder != null) {
                holder = nextHolder;

                nextHolder = null;
            }
        }

        public ViewHolder getHolder(){
            return holder;
        }

        @Override
        public void onPlayerPrepare() {
//            Log.e(TAG, "onPlayerPrepare");

            holder.startBN.setText("暂停");

            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                fScreenPlayBN.setText("暂停");
            }
        }

        @Override
        public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {

            holder.currentTimeTV.setText(XibaUtil.stringForTime(currentTime));
            holder.totalTimeTV.setText(XibaUtil.stringForTime(totalTime));

            if (!isTrackingTouchSeekBar) {
                holder.progressSeek.setProgress(progress);
            }

            holder.progressSeek.setSecondaryProgress(secProgress);
            if (!holder.progressSeek.isEnabled()) {
                holder.progressSeek.setEnabled(true);
            }

            //如果loading正在显示，在这里隐藏
            if (holder.loadingPB.getVisibility() == View.VISIBLE) {
                holder.loadingPB.setVisibility(View.GONE);
            }

            //处理全屏相关逻辑
            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                fScreenCurrentTimeTV.setText(XibaUtil.stringForTime(currentTime));
                fScreenTotalTimeTV.setText(XibaUtil.stringForTime(totalTime));

                fScreenDemoSeek.setProgress(progress);
                fScreenDemoSeek.setSecondaryProgress(secProgress);
                if (!fScreenDemoSeek.isEnabled()) {
                    fScreenDemoSeek.setEnabled(true);
                }
            }
        }

        @Override
        public void onPlayerPause() {
//            Log.e(TAG, "onPlayerPause");
            holder.startBN.setText("播放");

            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                fScreenPlayBN.setText("播放");
            }

            if (mUtilMsg != null) {
                holder.progressSeek.setEnabled(false);

                //如果loading正在显示，在这里隐藏
                if (holder.loadingPB.getVisibility() == View.VISIBLE) {
                    holder.loadingPB.setVisibility(View.GONE);
                }

                //在这里解除对Holder的绑定，否则loading会出现在上一个Item中
                eventCallback.changeHolder();

                mUtilMsg.sendToTarget();

                mUtilMsg = null;
            }

        }

        @Override
        public void onPlayerResume() {
            holder.startBN.setText("暂停");

            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                fScreenPlayBN.setText("暂停");
            }
        }

        @Override
        public void onPlayerComplete() {
            holder.startBN.setText("播放");

            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                fScreenPlayBN.setText("播放");
            }
        }

        @Override
        public void onPlayerAutoComplete() {
            holder.startBN.setText("播放");

            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                fScreenPlayBN.setText("播放");
            }
        }

        @Override
        public void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration) {
            int progress = (int) (seekTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度
            holder.progressSeek.setProgress(progress);
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

//        @Override
//        public ViewGroup onEnterFullScreen() {
//            ViewGroup contentView = (ViewGroup) ListDemoActivity.this.findViewById(Window.ID_ANDROID_CONTENT);
//
//            fullScreenContainer = (ViewGroup) getLayoutInflater()
//                    .inflate(R.layout.activity_simple_demo_fullscreen, contentView, false);
//
//            initFullScreenUI(holder);
//            return fullScreenContainer;
//        }
//
//        @Override
//        public void onQuitFullScreen() {
//            releaseFullScreenUI();
//        }

        @Override
        public void onEnterTinyScreen() {

        }

        @Override
        public void onQuitTinyScreen() {

            if (holder != null) {
                holder.tinyscreenBN.setText("小屏");
            }
        }

        @Override
        public void onSingleTap() {
            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                toggleShowHideFullScreenUI();
            }
        }

        @Override
        public void onDoubleTap() {
            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                mXibaListPlayUtil.togglePlay();
            }
        }

        @Override
        public void onTouchLockedScreen() {
            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                //显示或隐藏锁屏按钮
                toggleShowHideLockBN();
            }
        }

        @Override
        public void onStartLoading() {
//            if (!isBinding) {
//                return;
//            }
            Log.e(TAG, "onStartLoading");
            if (holder != null && holder.loadingPB.getVisibility() != View.VISIBLE) {
                holder.loadingPB.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * 全屏回调事件
     */
    private class ListFullScreenEventCallback implements XibaFullScreenEventCallback{
        private ViewHolder holder;

        public void setHolder(ViewHolder holder) {
            this.holder = holder;
        }

        public ViewHolder getHolder(){
            return holder;
        }

        @Override
        public ViewGroup onEnterFullScreen() {
            ViewGroup contentView = (ViewGroup) ListDemoActivity.this.findViewById(Window.ID_ANDROID_CONTENT);

            fullScreenContainer = (ViewGroup) getLayoutInflater()
                    .inflate(R.layout.activity_simple_demo_fullscreen, contentView, false);

            initFullScreenUI(holder);
            return fullScreenContainer;
        }

        @Override
        public void onQuitFullScreen() {
            releaseFullScreenUI();
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
