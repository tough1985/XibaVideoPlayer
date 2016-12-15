package com.axiba.xibavideoplayer.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaListPlayUtil;
import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.utils.XibaUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiba on 2016/12/11.
 */

public class ListDemoActivity extends AppCompatActivity {
    public static final String TAG = ListDemoActivity.class.getSimpleName();

    private ListView playerList;
    private PlayerListAdapter adapter;
    private ListEventCallback eventCallback;

    private XibaListPlayUtil mXibaListPlayUtil;

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

    private List<Map<String, Object>> playerStates = new ArrayList<>();

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
        setContentView(R.layout.activity_list_demo);

        for (int i = 0; i < urls.length; i++) {
            Map<String, Object> stateMap = new HashMap<>();
            stateMap.put("url", urls[i]);
            stateMap.put("isSetUp", false);
            playerStates.add(stateMap);
        }

        playerList = (ListView) findViewById(R.id.player_list);
        adapter = new PlayerListAdapter(this);
        playerList.setAdapter(adapter);

        mXibaListPlayUtil = new XibaListPlayUtil(this);

        eventCallback = new ListEventCallback();
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

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.startBN.setOnClickListener(new StartListener(holder, position, getItem(position)));
            holder.fullscreenBN.setOnClickListener(new FullScreenListener(holder, position, getItem(position)));

            /**
             * 调用XibaListPlayUtil.resolveItem来判断播放器是否添加到当前的item中
             * 并根据返回的PlayerStateInfo来决定Item中其他控件的状态
             */
            XibaListPlayUtil.PlayerStateInfo playerStateInfo = mXibaListPlayUtil.resolveItem(position, holder.container, eventCallback);

            Log.e(TAG, "position" + position);
            Log.e(TAG, "playerStateInfo == null?" + (playerStateInfo == null));

            initHolderUIByPlayerInfo(playerStateInfo, holder);

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

            mXibaListPlayUtil.togglePlay(url, position, holder.container, eventCallback);
            eventCallback.setHolder(holder);
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
            mXibaListPlayUtil.startFullScreen(url, position, holder.container, eventCallback);
            eventCallback.setHolder(holder);
        }
    }

    /**
     * 主要解决暂停问题
     * @param playerStateInfo
     * @param holder
     */
    private void initHolderUIByPlayerInfo(XibaListPlayUtil.PlayerStateInfo playerStateInfo, ViewHolder holder){
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

//            Log.e(TAG, "totalTimeDuration=" + totalTimeDuration);
//            Log.e(TAG, "currentTimePosition=" + currentTimePosition);
//            Log.e(TAG, "progress=" + progress);

            holder.progressSeek.setProgress(progress);
            holder.progressSeek.setEnabled(true);
        } else {
            holder.startBN.setText("播放");
            holder.currentTimeTV.setText("00:00");
            holder.totalTimeTV.setText("00:00");
            holder.progressSeek.setProgress(0);
            holder.progressSeek.setEnabled(false);
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

        public void setHolder(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onPlayerPrepare() {
            holder.startBN.setText("暂停");

            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                fScreenPlayBN.setText("暂停");
            }
        }

        @Override
        public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {

            holder.currentTimeTV.setText(XibaUtil.stringForTime(currentTime));
            holder.totalTimeTV.setText(XibaUtil.stringForTime(totalTime));

            holder.progressSeek.setProgress(progress);
            holder.progressSeek.setSecondaryProgress(secProgress);
            if (!holder.progressSeek.isEnabled()) {
                holder.progressSeek.setEnabled(true);
            }

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
            holder.startBN.setText("播放");

            if (mXibaListPlayUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                fScreenPlayBN.setText("播放");
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

        @Override
        public void onEnterTinyScreen() {

        }

        @Override
        public void onQuitTinyScreen() {

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
