package com.axiba.xibavideoplayer.listUtils;

import android.content.Context;
import android.graphics.Point;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;

/**
 * Created by xiba on 2017/2/21.
 */

public class XibaListUtil extends XibaBaseListUtil {

    protected int mPlayingIndex = -1;  //当前正在播放的item索引

    private SparseArray<PlayerStateInfo> stateInfoList;     //已经播放过的视频信息列表

    public XibaListUtil(Context context) {
        super(context);

        stateInfoList = new SparseArray<>();
    }
    /**
     * 全屏播放
     *
     * @param url
     * @param listPosition
     * @param itemContainer
     * @param eventCallback
     * @param fullScreenEventCallback
     */
    public void startFullScreen(String url, int listPosition, ViewGroup itemContainer,
                                XibaVideoPlayerEventCallback eventCallback,
                                XibaFullScreenEventCallback fullScreenEventCallback) {

        super.startFullScreen(url, listPosition, itemContainer,
                eventCallback, fullScreenEventCallback);
    }

    /**
     * 进入小屏或退出小屏
     *
     * @param url                     播放地址
     * @param listPosition
     * @param itemContainer           播放器容器
     * @param eventCallback           回调事件接口
     * @param tinyScreenEventCallback 小屏相关事件接口
     * @param size                    小屏的尺寸
     * @param x                       小屏x坐标
     * @param y                       小屏y坐标
     * @param canMove                 小屏是否可移动
     */
    public void toggleTinyScreen(String url, int listPosition, ViewGroup itemContainer,
                                 XibaVideoPlayerEventCallback eventCallback,
                                 XibaTinyScreenEventCallback tinyScreenEventCallback,
                                 Point size, float x, float y, boolean canMove) {

        super.toggleTinyScreen(url, listPosition, itemContainer,
                eventCallback, tinyScreenEventCallback, size,
                x, y, canMove);
    }

    /**
     * 跳转快进
     * @param url   播放地址
     * @param listPosition  播放器在list中的索引
     * @param itemContainer 播放器容器
     * @param eventCallback 回调事件接口
     * @param progress
     * @param maxProgress
     */
    public void seekTo(String url, int listPosition, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback,
                       int progress, int maxProgress){
        super.seekTo(url, listPosition, itemContainer, eventCallback, progress, maxProgress);
    }

    /**
     * 切换播放状态
     *
     * @param url
     * @param listPosition
     * @param itemContainer
     * @param eventCallback
     */
    public void togglePlay(String url, int listPosition, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {
        super.togglePlay(url, listPosition, itemContainer, eventCallback);

    }


    /**
     * 根据position和播放器的状态，来确定itemContainer中的内容
     *
     * @param listPosition
     * @param itemContainer
     * @param eventCallback
     * @return
     */
    public PlayerStateInfo resolveItem( int listPosition, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {

        return super.resolveItem(listPosition, itemContainer, eventCallback);
    }

    /**
     * 移出播放器
     * @param listPosition
     */
    public void removePlayer(int listPosition){

        if (listPosition == mPlayingIndex) {
            removePlayerFromParent();
            addToContentView();
        }

    }

    public boolean isCurrentPlayingIndex(int listPosition) {
        return mPlayingIndex == listPosition;
    }

    public int getPlayingIndex() {
        return mPlayingIndex;
    }

    @Override
    protected PlayerStateInfo getPlayingStateInfo() {
        return stateInfoList.get(mPlayingIndex);
    }

    @Override
    protected void setPlayingStateInfo(PlayerStateInfo playerStateInfo) {
        stateInfoList.put(mPlayingIndex, playerStateInfo);
    }

    @Override
    protected PlayerStateInfo getPlayingStateInfoByIndex(Object targetIndex) {
        if (!(targetIndex instanceof Integer)) {
            throw new IllegalArgumentException("param 'targetIndex' must be int");
        }

        return stateInfoList.get((int)targetIndex);
    }

    @Override
    protected boolean isPlayingIndex(Object targetIndex) {
        if (!(targetIndex instanceof Integer)) {
            throw new IllegalArgumentException("param 'targetIndex' must be int");
        }

        return mPlayingIndex == (int)targetIndex;
    }

    @Override
    protected boolean isPlayingIndexNull() {

        if (mPlayingIndex == -1) {
            return true;
        }
        return false;
    }

    @Override
    protected void setPlayingIndex(Object targetIndex) {
        if (!(targetIndex instanceof Integer)) {
            throw new IllegalArgumentException("param 'targetIndex' must be int");
        }
        mPlayingIndex = (int) targetIndex;
    }

    @Override
    public void release() {
        mXibaVideoPlayer.release();

        if (stateInfoList != null) {
            for (int i = 0; i < stateInfoList.size(); i++) {
                stateInfoList.get(stateInfoList.keyAt(i)).releaseBitmap();
            }
            stateInfoList = null;
        }
    }
}
