package com.axiba.xibavideoplayer.listUtils;

import android.content.Context;
import android.graphics.Point;
import android.view.ViewGroup;

import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by xiba on 2017/2/21.
 *
 * 用于ViewPager结合ListView或RecyclerView使用
 */
public class XibaPagerWithListUtil extends XibaBaseListUtil {


    private HashMap<String, PlayerStateInfo> mStateInfoMap;

    private PagerWithListPlayerIndex mPlayingIndex;


    public XibaPagerWithListUtil(Context context) {
        super(context);
        mStateInfoMap= new HashMap<>();

        mPlayingIndex = new PagerWithListPlayerIndex(-1, -1);
    }


    /**
     * 全屏播放
     *
     * @param url
     * @param fragmentIndex
     * @param listPosition
     * @param itemContainer
     * @param eventCallback
     * @param fullScreenEventCallback
     */
    public void startFullScreen(String url, int fragmentIndex, int listPosition, ViewGroup itemContainer,
                                XibaVideoPlayerEventCallback eventCallback,
                                XibaFullScreenEventCallback fullScreenEventCallback) {

        PagerWithListPlayerIndex targetIndex = new PagerWithListPlayerIndex(fragmentIndex, listPosition);

        super.startFullScreen(url, targetIndex, itemContainer,
                eventCallback, fullScreenEventCallback);
    }

    /**
     * 进入小屏或退出小屏
     *
     * @param url                     播放地址
     * @param fragmentIndex
     * @param listPosition
     * @param itemContainer           播放器容器
     * @param eventCallback           回调事件接口
     * @param tinyScreenEventCallback 小屏相关事件接口
     * @param size                    小屏的尺寸
     * @param x                       小屏x坐标
     * @param y                       小屏y坐标
     * @param canMove                 小屏是否可移动
     */
    public void toggleTinyScreen(String url, int fragmentIndex, int listPosition, ViewGroup itemContainer,
                                 XibaVideoPlayerEventCallback eventCallback,
                                 XibaTinyScreenEventCallback tinyScreenEventCallback,
                                 Point size, float x, float y, boolean canMove) {

        PagerWithListPlayerIndex targetIndex = new PagerWithListPlayerIndex(fragmentIndex, listPosition);

        super.toggleTinyScreen(url, targetIndex, itemContainer,
                eventCallback, tinyScreenEventCallback, size,
                x, y, canMove);
    }

    /**
     * 跳转快进
     * @param url   播放地址
     * @param fragmentIndex fragment的索引
     * @param listPosition  播放器在list中的索引
     * @param itemContainer 播放器容器
     * @param eventCallback 回调事件接口
     * @param progress
     * @param maxProgress
     */
    public void seekTo(String url, int fragmentIndex, int listPosition, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback,
                       int progress, int maxProgress){
        PagerWithListPlayerIndex targetIndex = new PagerWithListPlayerIndex(fragmentIndex, listPosition);
        super.seekTo(url, targetIndex, itemContainer, eventCallback, progress, maxProgress);
    }

    /**
     * 切换播放状态
     *
     * @param url
     * @param fragmentIndex
     * @param listPosition
     * @param itemContainer
     * @param eventCallback
     */
    public void togglePlay(String url, int fragmentIndex, int listPosition, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {
        PagerWithListPlayerIndex targetIndex = new PagerWithListPlayerIndex(fragmentIndex, listPosition);
        super.togglePlay(url, targetIndex, itemContainer, eventCallback);

    }


    /**
     * 根据position和播放器的状态，来确定itemContainer中的内容
     *
     * @param fragmentIndex
     * @param listPosition
     * @param itemContainer
     * @param eventCallback
     * @return
     */
    public PlayerStateInfo resolveItem(int fragmentIndex, int listPosition, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {
        PagerWithListPlayerIndex targetIndex = new PagerWithListPlayerIndex(fragmentIndex, listPosition);

        return super.resolveItem(targetIndex, itemContainer, eventCallback);
    }

    /**
     * 只要fragmentIndex相同，就移出播放器
     * @param fragmentIndex
     */
    public void removePlayer(int fragmentIndex){

        if (mPlayingIndex != null && mPlayingIndex.getFragmentIndex() == fragmentIndex) {
            removePlayerFromParent();
            addToContentView();
        }

    }

    public boolean isCurrentPlayingIndex(int fragmentIndex, int listPosition) {
        if (mPlayingIndex == null) {
            return false;
        }
        PagerWithListPlayerIndex targetIndex = new PagerWithListPlayerIndex(fragmentIndex, listPosition);
        return mPlayingIndex.equals(targetIndex);
    }

//    @Override
//    public Object getPlayingIndex() {
//        return mPlayingIndex;
//    }

    @Override
    protected void setPlayingIndex(Object targetIndex) {
        if (!(targetIndex instanceof PagerWithListPlayerIndex)) {
            throw new IllegalArgumentException("param 'targetIndex' must be an object of XibaPagerWithListUtil.PagerWithListPlayerIndex");
        }

        mPlayingIndex = (PagerWithListPlayerIndex) targetIndex;
    }

    @Override
    protected PlayerStateInfo getPlayingStateInfo() {
        return mStateInfoMap.get(mPlayingIndex.getKey());
    }

    @Override
    protected void setPlayingStateInfo(PlayerStateInfo playerStateInfo) {
        mStateInfoMap.put(mPlayingIndex.getKey(), playerStateInfo);
    }

    @Override
    protected PlayerStateInfo getPlayingStateInfoByIndex(Object targetIndex) {
        if (!(targetIndex instanceof PagerWithListPlayerIndex)) {
            throw new IllegalArgumentException("param 'targetIndex' must be an object of XibaPagerWithListUtil.PagerWithListPlayerIndex");
        }

        if (targetIndex == null) {
            return null;
        }
        PagerWithListPlayerIndex target = (PagerWithListPlayerIndex) targetIndex;

        return mStateInfoMap.get(target.getKey());
    }

    @Override
    protected boolean isPlayingIndex(Object targetIndex) {
        if (mPlayingIndex == null) {
            return false;
        }
        return mPlayingIndex.equals(targetIndex);
    }

    @Override
    protected boolean isPlayingIndexNull() {

        if (mPlayingIndex == null || (mPlayingIndex.getFragmentIndex() == -1 && mPlayingIndex.getListPosition() == -1)) {
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        mXibaVideoPlayer.release();

        if (mStateInfoMap != null) {

            Iterator<String> i = mStateInfoMap.keySet().iterator();

            while (i.hasNext()) {
                mStateInfoMap.get(i.next()).releaseBitmap();
            }

            mStateInfoMap = null;
        }
    }


    //二维索引
    private class PagerWithListPlayerIndex {
        //fragment的索引
        private int fragmentIndex;
        //list中的索引
        private int listPosition;

        public PagerWithListPlayerIndex(int fragmentIndex, int listPosition) {
            this.fragmentIndex = fragmentIndex;
            this.listPosition = listPosition;
        }

        public int getFragmentIndex() {
            return fragmentIndex;
        }

        public void setFragmentIndex(int fragmentIndex) {
            this.fragmentIndex = fragmentIndex;
        }

        public int getListPosition() {
            return listPosition;
        }

        public void setListPosition(int listPosition) {
            this.listPosition = listPosition;
        }

        public String getKey(){
            return fragmentIndex + ":" + listPosition;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj != null) {
                if (obj == this) {
                    return true;
                } else if (obj instanceof PagerWithListPlayerIndex) {
                    PagerWithListPlayerIndex playerIndex = (PagerWithListPlayerIndex) obj;
                    if (this.fragmentIndex == playerIndex.fragmentIndex &&
                            this.listPosition == playerIndex.listPosition) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
