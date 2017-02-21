package com.axiba.xibavideoplayer.sample.viewPagerWithRecyclerView;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.listUtils.PlayerStateInfo;
import com.axiba.xibavideoplayer.listUtils.XibaPagerWithListUtil;
import com.axiba.xibavideoplayer.sample.DividerItemDecoration;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2017/2/21.
 */

public class PagerWithRecyclerFragment extends Fragment {

    public static final String TAG = PagerWithRecyclerFragment.class.getSimpleName();

    private static final String KEY_URLS = "key_urls";
    private static final String KEY_FRAGMENT_INDEX = "key_fragment_index";

    private String[] mUrls;
    private int mFragmentIndex;

    private RecyclerView mPlayerListRV;
    private RecyclerView.LayoutManager mLayoutManager;
    private PagerWithRecyclerAdapter mAdapter;

    private XibaPagerWithListUtil mXibaPagerWithListUtil;
    //播放器回调事件
    private PagerWithRecyclerEventCallback mEventCallback;
    //全屏回调事件
    private PagerWithRecyclerActivity.PlayerFullScreenEventCallback mFScreenCallback;

    public static PagerWithRecyclerFragment newInstance(String[] urls, int mFragmentIndex){
        PagerWithRecyclerFragment instance = new PagerWithRecyclerFragment();
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
        View v = inflater.inflate(R.layout.activity_recyclerview_demo, container, false);
        mPlayerListRV = (RecyclerView) v.findViewById(R.id.player_list_RV);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mLayoutManager = new LinearLayoutManager(getContext());

        mAdapter = new PagerWithRecyclerAdapter();


        mPlayerListRV.setLayoutManager(mLayoutManager);
        mPlayerListRV.setHasFixedSize(true);
        mPlayerListRV.setAdapter(mAdapter);
        mPlayerListRV.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));

        mEventCallback = ((PagerWithRecyclerActivity)getActivity()).getEventCallback();
        mFScreenCallback = ((PagerWithRecyclerActivity)getActivity()).getFScreenEventCallback();
        mXibaPagerWithListUtil = ((PagerWithRecyclerActivity)getActivity()).getXibaPagerWithListUtil();

    }

    public class PagerWithRecyclerAdapter extends RecyclerView.Adapter<PagerWithRecyclerAdapter.PlayerViewHolder> {

        @Override
        public PagerWithRecyclerAdapter.PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(getContext()).inflate(R.layout.player_list_item, parent, false);

            PagerWithRecyclerAdapter.PlayerViewHolder holder = new PagerWithRecyclerAdapter.PlayerViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(PagerWithRecyclerAdapter.PlayerViewHolder holder, int position) {
            holder.startBN.setOnClickListener(new StartListener(holder, position, mUrls[position]));
            holder.fullscreenBN.setOnClickListener(new FullScreenListener(holder, position, mUrls[position]));
            holder.tinyscreenBN.setOnClickListener(new TinyScreenListener(holder, position, mUrls[position]));
            holder.progressSeek.setOnSeekBarChangeListener(new SeekProgressListener(holder, position, mUrls[position]));

            /**
             * 调用XibaListPlayUtil.resolveItem来判断播放器是否添加到当前的item中
             * 并根据返回的PlayerStateInfo来决定Item中其他控件的状态
             */
            PlayerStateInfo playerStateInfo = mXibaPagerWithListUtil.resolveItem(mFragmentIndex, position, holder.container, mEventCallback);

            initHolderUIByPlayerInfo(playerStateInfo, holder, position);
        }

        @Override
        public int getItemCount() {
            return mUrls.length;
        }


        class PlayerViewHolder extends RecyclerView.ViewHolder {

            FrameLayout container;
            Button startBN;
            Button fullscreenBN;
            TextView currentTimeTV;
            TextView totalTimeTV;
            SeekBar progressSeek;
            Button tinyscreenBN;
            ProgressBar loadingPB;

            public PlayerViewHolder(View itemView) {
                super(itemView);

                container = (FrameLayout) itemView.findViewById(R.id.player_list_item_container);
                startBN = (Button) itemView.findViewById(R.id.player_list_item_play);
                fullscreenBN = (Button) itemView.findViewById(R.id.player_list_item_fullscreen);
                currentTimeTV = (TextView) itemView.findViewById(R.id.player_list_item_current_time);
                totalTimeTV = (TextView) itemView.findViewById(R.id.player_list_item_total_time);
                progressSeek = (SeekBar) itemView.findViewById(R.id.player_list_item_demo_seek);
                tinyscreenBN = (Button) itemView.findViewById(R.id.player_list_item_tinyscreen);
                loadingPB = (ProgressBar) itemView.findViewById(R.id.player_list_item_loading_PB);
            }
        }

        /**
         * 主要解决暂停问题
         *
         * @param playerStateInfo
         * @param holder
         */
        private void initHolderUIByPlayerInfo(PlayerStateInfo playerStateInfo, PagerWithRecyclerAdapter.PlayerViewHolder holder, int position) {
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

                if (mXibaPagerWithListUtil.isCurrentPlayingIndex(mFragmentIndex, position)) {  //如果当前索引为播放索引
                    //设置进度条可用
                    holder.progressSeek.setEnabled(true);

                    //如果正在加载，显示LoadingProgress，
                    if (mEventCallback.isLoadingProgressShow() && holder.loadingPB.getVisibility() != View.VISIBLE) {
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

            if (mXibaPagerWithListUtil.isCurrentPlayingIndex(mFragmentIndex, position)) {
                mEventCallback.bindHolder(holder, mFragmentIndex, position);
            }
        }

        /**
         * 播放按钮监听
         */
        private class StartListener implements View.OnClickListener{
            private PagerWithRecyclerAdapter.PlayerViewHolder holder;
            private int position;
            private String url;

            public StartListener(PagerWithRecyclerAdapter.PlayerViewHolder holder, int position, String url) {
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
            private PagerWithRecyclerAdapter.PlayerViewHolder holder;
            private int position;
            private String url;

            public FullScreenListener(PagerWithRecyclerAdapter.PlayerViewHolder holder, int position, String url) {
                this.holder = holder;
                this.position = position;
                this.url = url;
            }

            @Override
            public void onClick(View v) {
                mEventCallback.bindHolder(holder, mFragmentIndex, position);
                mFScreenCallback.setHolder(holder);
                mXibaPagerWithListUtil.startFullScreen(url, mFragmentIndex, position, holder.container, mEventCallback, mFScreenCallback);

            }
        }

        /**
         * 小屏按钮监听
         */
        private class TinyScreenListener implements View.OnClickListener{
            private PagerWithRecyclerAdapter.PlayerViewHolder holder;
            private int position;
            private String url;

            public TinyScreenListener(PagerWithRecyclerAdapter.PlayerViewHolder holder, int position, String url) {
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

            private PagerWithRecyclerAdapter.PlayerViewHolder holder;
            private int position;
            private String url;

            public SeekProgressListener(PagerWithRecyclerAdapter.PlayerViewHolder holder, int position, String url) {
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
    }
}
