package com.axiba.xibavideoplayer.sample.viewPagerWithListView;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.axiba.xibavideoplayer.listUtils.PlayerStateInfo;
import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.listUtils.XibaPagerWithListUtil;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.sample.viewPagerDemo.PlayerFragment;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2017/2/20.
 */

public class PagerWithListFragment extends Fragment{
    public static final String TAG = PlayerFragment.class.getSimpleName();

    private static final String KEY_URLS = "key_urls";
    private static final String KEY_FRAGMENT_INDEX = "key_fragment_index";

    private String[] mUrls;
    private int mFragmentIndex;

    private ListView mPlayerList;
    private PagerWithListAdapter mAdapter;

    private XibaPagerWithListUtil mXibaPagerWithListUtil;
    //播放器回调事件
    private PagerWithListEventCallback mEventCallback;
    //全屏回调事件
    private PagerWithListActivity.PlayerFullScreenEventCallback mFScreenCallback;

    public static PagerWithListFragment newInstance(String[] urls, int mFragmentIndex){
        PagerWithListFragment instance = new PagerWithListFragment();
        Bundle args = new Bundle();
        args.putStringArray(KEY_URLS, urls);
        args.putInt(KEY_FRAGMENT_INDEX, mFragmentIndex);

        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        if (data != null) {
            mUrls = data.getStringArray(KEY_URLS);
            mFragmentIndex = data.getInt(KEY_FRAGMENT_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_list_demo, container, false);
        mPlayerList = (ListView) v.findViewById(R.id.player_list);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new PagerWithListAdapter(getContext());
        mPlayerList.setAdapter(mAdapter);

        mEventCallback = ((PagerWithListActivity)getActivity()).getEventCallback();
        mFScreenCallback = ((PagerWithListActivity)getActivity()).getFScreenEventCallback();
        mXibaPagerWithListUtil = ((PagerWithListActivity)getActivity()).getXibaPagerWithListUtil();

    }

    private class PagerWithListAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        public PagerWithListAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mUrls.length;
        }

        @Override
        public String getItem(int position) {
            return mUrls[position];
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
            PlayerStateInfo playerStateInfo = mXibaPagerWithListUtil.resolveItem(mFragmentIndex, position, holder.container, mEventCallback);

            initHolderUIByPlayerInfo(playerStateInfo, holder, position);

            return convertView;
        }
    }

    public class ViewHolder{
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

            mEventCallback.bindHolder(holder, mFragmentIndex, position);
            mXibaPagerWithListUtil.togglePlay(url, mFragmentIndex, position, holder.container, mEventCallback);
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
            mEventCallback.bindHolder(holder, mFragmentIndex, position);
            mXibaPagerWithListUtil.startFullScreen(url, mFragmentIndex, position, holder.container, mEventCallback, mFScreenCallback);

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

            mEventCallback.bindHolder(holder, mFragmentIndex, position);
            mXibaPagerWithListUtil.toggleTinyScreen(url, mFragmentIndex, position, holder.container, mEventCallback, mEventCallback, new Point(500, 300), 600, 1400, true);

            if (mXibaPagerWithListUtil.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
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
            mEventCallback.setTrackingTouchSeekBar(true);

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            mEventCallback.bindHolder(holder, mFragmentIndex, position);

            mXibaPagerWithListUtil.seekTo(url, mFragmentIndex, position, holder.container,
                    mEventCallback, holder.progressSeek.getProgress(), holder.progressSeek.getMax());

            mEventCallback.setTrackingTouchSeekBar(false);
        }
    }

    /**
     * 主要解决暂停问题
     * @param playerStateInfo
     * @param holder
     */
    private void initHolderUIByPlayerInfo(PlayerStateInfo playerStateInfo, ViewHolder holder, int position){
        Log.e(TAG, "fragmentIndex=" + mFragmentIndex + " : listPosition=" + position);



        if (playerStateInfo != null) {

            Log.e(TAG, playerStateInfo.toString());

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


            if (mXibaPagerWithListUtil.isCurrentPlayingIndex(mFragmentIndex, position)) {
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
        if (mXibaPagerWithListUtil.isCurrentPlayingIndex(mFragmentIndex, position)) {
            mEventCallback.bindHolder(holder, mFragmentIndex, position);
        }
    }
}
