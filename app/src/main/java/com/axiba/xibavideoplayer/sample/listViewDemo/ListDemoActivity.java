package com.axiba.xibavideoplayer.sample.listViewDemo;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.listUtils.PlayerStateInfo;
import com.axiba.xibavideoplayer.listUtils.XibaBaseListUtil;
import com.axiba.xibavideoplayer.listUtils.XibaListPlayUtil;
import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.listUtils.XibaListUtil;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.sample.view.FullScreenContainer;
import com.axiba.xibavideoplayer.utils.XibaUtil;


/**
 * Created by xiba on 2016/12/11.
 */

public class ListDemoActivity extends AppCompatActivity {
    public static final String TAG = ListDemoActivity.class.getSimpleName();

    private ListView mPlayerList;
    private PlayerListAdapter mAdapter;

    private ListEventCallback mEventCallback;
    private ListFullScreenEventCallback mFScreenEventCallback;

    private XibaListUtil mXibaListUtil;

    private boolean isTrackingTouchSeekBar = false;     //是否正在控制SeekBar

    private String[] urls;

    //全屏容器
    private FullScreenContainer mFullScreenContainer;

    private Message mUtilMsg;

    //是否正在Loading
    private boolean isLoadingProgressShow = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_demo);

        urls = this.getResources().getStringArray(R.array.urls);

        mPlayerList = (ListView) findViewById(R.id.player_list);
        mAdapter = new PlayerListAdapter(this);
        mPlayerList.setAdapter(mAdapter);

        mXibaListUtil = new XibaListUtil(this);
        mXibaListUtil.setPlayingItemPositionChangeImpl(new XibaBaseListUtil.PlayingItemPositionChange() {

            @Override
            public void prePlayingItemPositionChange(Message utilMsg) {
                mUtilMsg = utilMsg;

            }

            @Override
            public void prePlayingItemChangeOnPause() {
                if (mEventCallback != null && mEventCallback.getHolder() != null) {
                    mEventCallback.getHolder().progressSeek.setEnabled(false);

                    //如果loading正在显示，在这里隐藏
                    if (mEventCallback.getHolder().loadingPB.getVisibility() == View.VISIBLE) {
                        mEventCallback.getHolder().loadingPB.setVisibility(View.GONE);
                        isLoadingProgressShow = false;
                    }

                    mEventCallback.changeHolder();
                }
            }
        });

        mEventCallback = new ListEventCallback();
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
            PlayerStateInfo playerStateInfo = mXibaListUtil.resolveItem(position, holder.container, mEventCallback);

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

            mEventCallback.bindHolder(holder, position);
            mXibaListUtil.togglePlay(url, position, holder.container, mEventCallback);
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
            mEventCallback.bindHolder(holder, position);
            mXibaListUtil.startFullScreen(url, position, holder.container, mEventCallback, mFScreenEventCallback);

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

            mEventCallback.bindHolder(holder, position);
            mXibaListUtil.toggleTinyScreen(url, position, holder.container, mEventCallback, mEventCallback, new Point(500, 300), 600, 1400, true);

            if (mXibaListUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                holder.tinyscreenBN.setText("返回");
            } else {
                holder.tinyscreenBN.setText("小屏");
            }
        }
    }

    /**
     * 进度条监听
     */
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

            mEventCallback.bindHolder(holder, position);

            mXibaListUtil.seekTo(url, position, holder.container,
                    mEventCallback, holder.progressSeek.getProgress(), holder.progressSeek.getMax());

            isTrackingTouchSeekBar = false;
        }
    }

    /**
     * 主要解决暂停问题
     * @param playerStateInfo
     * @param holder
     */
    private void initHolderUIByPlayerInfo(PlayerStateInfo playerStateInfo, ViewHolder holder, int position){
        Log.e(TAG, "initHolderUIByPlayerInfo");
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

            //设置进度条进度
            holder.progressSeek.setProgress(progress);

            if (mXibaListUtil.getPlayingIndex() == position) {  //如果当前索引为播放索引
                //设置进度条可用
                holder.progressSeek.setEnabled(true);

                //如果正在加载，显示LoadingProgress，
                if (isLoadingProgressShow && holder.loadingPB.getVisibility() != View.VISIBLE) {
                    holder.loadingPB.setVisibility(View.VISIBLE);
                }
            } else {    //如果当前索引不是播放索引

                //设置进度条不可用
                holder.progressSeek.setEnabled(false);

                //如果显示了LoadingProgress，隐藏LoadingProgress
                if (holder.loadingPB.getVisibility() == View.VISIBLE) {
                    holder.loadingPB.setVisibility(View.GONE);
                }
            }

            //如果当前屏幕类型为小屏播放
            if (playerStateInfo.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                //设置小屏按钮内容
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
            if (holder.loadingPB.getVisibility() == View.VISIBLE) {
                holder.loadingPB.setVisibility(View.GONE);
            }
        }

        /**
         * 如果mEventCallback还没有绑定UI 或者 当前Item就是播放Item
         */
        if (mXibaListUtil.getPlayingIndex() == position) {
            mEventCallback.bindHolder(holder, position);
        }
    }

    /**
     * 播放器回调事件
     */
    private class ListEventCallback implements XibaVideoPlayerEventCallback, XibaTinyScreenEventCallback{

        private ViewHolder holder;

        private ViewHolder nextHolder;

        public void bindHolder(ViewHolder holder, int position) {

            if (this.holder == null || mXibaListUtil.getPlayingIndex() == position) {
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
            Log.e(TAG, "onPlayerPrepare");

            holder.startBN.setText("暂停");

        }

        @Override
        public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {
            Log.e(TAG, "onPlayerProgressUpdate");
            //处理列表屏幕和小屏幕相关逻辑

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
                isLoadingProgressShow = false;
            }

        }

        @Override
        public void onPlayerPause() {
            Log.e(TAG, "onPlayerPause");
            holder.startBN.setText("播放");

            if (mUtilMsg != null) {
                holder.progressSeek.setEnabled(false);

                //如果loading正在显示，在这里隐藏
                if (holder.loadingPB.getVisibility() == View.VISIBLE) {
                    holder.loadingPB.setVisibility(View.GONE);
                    isLoadingProgressShow = false;
                }

                //在这里解除对Holder的绑定，否则loading会出现在上一个Item中
                mEventCallback.changeHolder();

                mUtilMsg.sendToTarget();

                mUtilMsg = null;
            }

        }

        @Override
        public void onPlayerResume() {
            holder.startBN.setText("暂停");
        }

        @Override
        public void onPlayerComplete() {
            holder.startBN.setText("播放");
        }

        @Override
        public void onPlayerAutoComplete() {
            holder.startBN.setText("播放");
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
        public void onEnterTinyScreen() {
            if (holder != null) {
                holder.tinyscreenBN.setText("返回");
            }
        }

        @Override
        public void onQuitTinyScreen() {

            if (holder != null) {
                holder.tinyscreenBN.setText("小屏");
            }
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
            if (holder != null && holder.loadingPB.getVisibility() != View.VISIBLE) {
                holder.loadingPB.setVisibility(View.VISIBLE);
                isLoadingProgressShow = true;
            }
        }

    }

    /**
     * 全屏回调事件
     */
    private class ListFullScreenEventCallback implements XibaFullScreenEventCallback {

        @Override
        public ViewGroup onEnterFullScreen() {

            mFullScreenContainer = new FullScreenContainer(ListDemoActivity.this);

            //初始化全屏控件
            mFullScreenContainer.initUI(mXibaListUtil.getXibaVideoPlayer());

            mXibaListUtil.setEventCallback(mFullScreenContainer.geFullScreenEventCallback());

            //全屏状态下，垂直滑动左侧改变亮度，右侧改变声音
            mXibaListUtil.setFullScreenVerticalFeature(XibaVideoPlayer.SLIDING_VERTICAL_LEFT_BRIGHTNESS);

            //全屏状态下，水平滑动改变播放位置
            mXibaListUtil.setFullScreenHorizontalFeature(XibaVideoPlayer.SLIDING_HORIZONTAL_CHANGE_POSITION);

            //全屏状态下，水平滑动改变位置的总量为屏幕的 1/4
            mXibaListUtil.setHorizontalSlopInfluenceValue(4);

            return mFullScreenContainer;
        }

        @Override
        public void onQuitFullScreen() {
            Log.e(TAG, "onQuitFullScreen");
            mFullScreenContainer.releaseFullScreenUI();

            //手动调用保存播放信息，防止在暂停之后，返回List播放，播放器UI不一致的情况
            //需要注意，RecyclerView的处理方式不一样，具体请参考RecyclerView的Demo
            mXibaListUtil.savePlayerInfo();

            //绑定List的mEventCallback
            mXibaListUtil.setEventCallback(mEventCallback);

        }
    }

    @Override
    public void onBackPressed() {
        if (mXibaListUtil.onBackPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mXibaListUtil.release();
        super.onDestroy();
    }
}
