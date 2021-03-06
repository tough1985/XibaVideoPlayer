package com.axiba.xibavideoplayer.sample.viewPagerDemo;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.listUtils.PlayerStateInfo;
import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.listUtils.XibaListUtil;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2016/12/21.
 */

public class PlayerFragment extends Fragment {

    public static final String TAG = PlayerFragment.class.getSimpleName();

    private static final String KEY_URL = "key_url";
    private static final String KEY_POSITION = "key_position";

    private String mUrl;
    private int mPosition;

    private FrameLayout pagerPlayerContainer;
    private Button play;
    private TextView currentTimeTV;
    private TextView totalTimeTV;
    private SeekBar demoSeek;
    private Button fullScreenBN;
    private Button tinyScreenBN;
    private ProgressBar loadingPB;

    private XibaListUtil mXibaListUtil;

    private PlayerPagerEventCallback mEventCallback;
    private ViewPagerDemoActivity.PlayerFullScreenEventCallback mFScreenEventCallback;

    public static PlayerFragment newInstance(String url, int position){
        PlayerFragment instance = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        args.putInt(KEY_POSITION, position);

        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        if (data != null) {
            mUrl = data.getString(KEY_URL);
            mPosition = data.getInt(KEY_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player, container, false);
        pagerPlayerContainer = (FrameLayout) v.findViewById(R.id.pager_player_container);
        play = (Button) v.findViewById(R.id.pager_demo_play);
        currentTimeTV = (TextView) v.findViewById(R.id.pager_current_time);
        totalTimeTV = (TextView) v.findViewById(R.id.pager_total_time);
        demoSeek = (SeekBar) v.findViewById(R.id.pager_demo_seek);
        fullScreenBN = (Button) v.findViewById(R.id.pager_full_screen_BN);
        tinyScreenBN = (Button) v.findViewById(R.id.pager_tiny_screen_BN);
        loadingPB = (ProgressBar) v.findViewById(R.id.pager_loading_PB);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        play.setOnClickListener(new StartListener());
        fullScreenBN.setOnClickListener(new FullScreenListener());
        tinyScreenBN.setOnClickListener(new TinyScreenListener());
        demoSeek.setOnSeekBarChangeListener(new SeekProgressListener());

        mXibaListUtil = ((ViewPagerDemoActivity)getActivity()).getXibaListUtil();
        mEventCallback = ((ViewPagerDemoActivity)getActivity()).getEventCallback();
        mFScreenEventCallback = ((ViewPagerDemoActivity)getActivity()).getFScreenEventCallback();

        PlayerStateInfo playerStateInfo = mXibaListUtil.resolveItem(mPosition, pagerPlayerContainer, mEventCallback);
        initUIByPlayerInfo(playerStateInfo);
    }

    public void initUIByPlayerInfo(PlayerStateInfo playerStateInfo){

        Log.e(TAG, "initUIByPlayerInfo mPosition=" + mPosition);
        if (playerStateInfo != null) {
            if (playerStateInfo.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
                play.setText("暂停");
            } else {
                play.setText("播放");
            }

            long totalTimeDuration = playerStateInfo.getDuration();
            long currentTimePosition = playerStateInfo.getPosition();

            currentTimeTV.setText(XibaUtil.stringForTime(currentTimePosition));
            totalTimeTV.setText(XibaUtil.stringForTime(totalTimeDuration));

            int progress = (int) (currentTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度


            demoSeek.setProgress(progress);

            if (mXibaListUtil.getPlayingIndex() == mPosition) {
                demoSeek.setEnabled(true);
            } else {
                demoSeek.setEnabled(false);
            }

            if (playerStateInfo.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                tinyScreenBN.setText("返回");
            } else {
                tinyScreenBN.setText("小屏");
            }
        } else {
            play.setText("播放");
            currentTimeTV.setText("00:00");
            totalTimeTV.setText("00:00");
            demoSeek.setProgress(0);
            demoSeek.setEnabled(false);
            tinyScreenBN.setText("小屏");
        }

        /**
         * 如果mEventCallback还没有绑定UI 或者 当前Item就是播放Item
         */
        if (mXibaListUtil.getPlayingIndex() == mPosition) {
            mEventCallback.bindingPlayerUI(this, mPosition);
        }
    }

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --listeners start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */

    /**
     * 播放按钮监听
     */
    private class StartListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            mEventCallback.bindingPlayerUI(PlayerFragment.this, mPosition);
            mXibaListUtil.togglePlay(mUrl, mPosition, pagerPlayerContainer, mEventCallback);
        }
    }

    /**
     * 全屏按钮监听
     */
    private class FullScreenListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            mFScreenEventCallback.bindingPlayerUI(PlayerFragment.this);
            mEventCallback.bindingPlayerUI(PlayerFragment.this, mPosition);
            mXibaListUtil.startFullScreen(mUrl, mPosition, pagerPlayerContainer, mEventCallback, mFScreenEventCallback);

        }
    }

    /**
     * 小屏按钮监听
     */
    private class TinyScreenListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            mEventCallback.bindingPlayerUI(PlayerFragment.this, mPosition);
            mXibaListUtil.toggleTinyScreen(mUrl, mPosition, pagerPlayerContainer, mEventCallback, mEventCallback, new Point(500, 300), 600, 1400, true);

            if (mXibaListUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                tinyScreenBN.setText("返回");
            } else {
                tinyScreenBN.setText("小屏");
            }
        }
    }

    /**
     * 进度条监听
     */
    private class SeekProgressListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mEventCallback.setTrackingTouchSeekBar(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            mEventCallback.bindingPlayerUI(PlayerFragment.this, mPosition);

            mXibaListUtil.seekTo(mUrl, mPosition, pagerPlayerContainer,
                    mEventCallback, demoSeek.getProgress(), demoSeek.getMax());

            mEventCallback.setTrackingTouchSeekBar(false);
        }
    }
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --listeners end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑


    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --getter methods start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    public Button getPlay() {
        return play;
    }

    public TextView getCurrentTimeTV() {
        return currentTimeTV;
    }

    public TextView getTotalTimeTV() {
        return totalTimeTV;
    }

    public SeekBar getDemoSeek() {
        return demoSeek;
    }

    public Button getFullScreenBN() {
        return fullScreenBN;
    }

    public Button getTinyScreenBN() {
        return tinyScreenBN;
    }

    public ProgressBar getLoadingPB() {
        return loadingPB;
    }
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --getter methods end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    public void resetUI() {
        //设置播放按钮状态
        if (mXibaListUtil.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
            play.setText("暂停");
        } else {
            play.setText("播放");
        }

        //如果视频未加载，进度条不可用
        if (mXibaListUtil.getCurrentState() == XibaVideoPlayer.STATE_NORMAL
                || mXibaListUtil.getCurrentState() == XibaVideoPlayer.STATE_ERROR) {

            demoSeek.setEnabled(false);
        } else {

            demoSeek.setEnabled(true);

            long totalTimeDuration = mXibaListUtil.getDuration();
            long currentTimePosition = mXibaListUtil.getCurrentPosition();

            //设置视频总时长和当前播放位置
            currentTimeTV.setText(XibaUtil.stringForTime(currentTimePosition));
            totalTimeTV.setText(XibaUtil.stringForTime(totalTimeDuration));

            int progress = (int) (currentTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度

            //设置进度条位置
            demoSeek.setProgress(progress);
        }
    }
}
