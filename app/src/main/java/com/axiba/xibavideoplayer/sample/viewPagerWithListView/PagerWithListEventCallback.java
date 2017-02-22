package com.axiba.xibavideoplayer.sample.viewPagerWithListView;

import android.os.Message;
import android.view.View;

import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.listUtils.XibaBaseListUtil;
import com.axiba.xibavideoplayer.listUtils.XibaPagerWithListUtil;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2017/2/20.
 */
public class PagerWithListEventCallback implements XibaVideoPlayerEventCallback, XibaTinyScreenEventCallback {

    private XibaPagerWithListUtil mXibaPagerWithListUtil;

    private Message mUtilMsg;

    //是否正在Loading
    private boolean isLoadingProgressShow = false;

    public boolean isLoadingProgressShow() {
        return isLoadingProgressShow;
    }

    public void setLoadingProgressShow(boolean loadingProgressShow) {
        isLoadingProgressShow = loadingProgressShow;
    }

    public PagerWithListEventCallback(XibaPagerWithListUtil xibaPagerWithListUtil) {
        this.mXibaPagerWithListUtil = xibaPagerWithListUtil;

        mXibaPagerWithListUtil.setPlayingItemPositionChangeImpl(new XibaBaseListUtil.PlayingItemPositionChange() {
            @Override
            public void prePlayingItemPositionChange(Message utilMsg) {
                mUtilMsg = utilMsg;
            }

            @Override
            public void prePlayingItemChangeOnPause() {
                if (getHolder() != null) {
                    getHolder().progressSeek.setEnabled(false);

                    //如果loading正在显示，在这里隐藏
                    if (getHolder().loadingPB.getVisibility() == View.VISIBLE) {
                        getHolder().loadingPB.setVisibility(View.GONE);
                        isLoadingProgressShow = false;
                    }

                    changeHolder();
                }
            }
        });
    }

    private PagerWithListFragment.ViewHolder holder;

    private PagerWithListFragment.ViewHolder nextHolder;

    public void bindHolder(PagerWithListFragment.ViewHolder holder,
                           int fragmentIndex, int listPosition) {

        if (this.holder == null || mXibaPagerWithListUtil.isCurrentPlayingIndex(fragmentIndex, listPosition)) {
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

    public PagerWithListFragment.ViewHolder getHolder(){
        return holder;
    }

    private boolean isTrackingTouchSeekBar = false;

    public void setTrackingTouchSeekBar(boolean trackingTouchSeekBar) {
        isTrackingTouchSeekBar = trackingTouchSeekBar;
    }

    @Override
    public void onPlayerPrepare() {
        holder.startBN.setText("暂停");
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
            isLoadingProgressShow = false;
        }
    }

    @Override
    public void onPlayerPause() {
        holder.startBN.setText("播放");

        if (mUtilMsg != null) {
            holder.progressSeek.setEnabled(false);

            //如果loading正在显示，在这里隐藏
            if (holder.loadingPB.getVisibility() == View.VISIBLE) {
                holder.loadingPB.setVisibility(View.GONE);
                isLoadingProgressShow = false;
            }

            //在这里解除对Holder的绑定，否则loading会出现在上一个Item中
            changeHolder();

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
    public void onStartLoading() {
        if (holder != null && holder.loadingPB.getVisibility() != View.VISIBLE) {
            holder.loadingPB.setVisibility(View.VISIBLE);
            isLoadingProgressShow = true;
        }
    }

    @Override
    public void onPlayerError(int what, int extra) {

    }

    @Override
    public void onEnterTinyScreen() {
        holder.tinyscreenBN.setText("返回");
    }

    @Override
    public void onQuitTinyScreen() {
        holder.tinyscreenBN.setText("小屏");
    }
}
