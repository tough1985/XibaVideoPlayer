package com.axiba.xibavideoplayer.listUtils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaPlayerActionEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiba on 2017/2/21.
 */

public abstract class XibaBaseListUtil {
    public static final String TAG = XibaBaseListUtil.class.getSimpleName();

    public static final String PLAYER_TAG_NO_CONTAINER = "";                 //没有父容器
    public static final String PLAYER_TAG_ITEM_CONTAINER = "itemContainer";  //父容器是itemContainer
    public static final String PLAYER_TAG_CONTENT_VIEW = "contentView";      //父容器是ContentView

    protected static final String KEY_ITEM_CONTAINER = "itemContainer";
    protected static final String KEY_EVENT_CALLBACK = "eventCallback";
    protected static final String KEY_POSITION = "position";
    protected static final String KEY_URL = "url";
    protected static final String KEY_LAST_STATE = "lastState";

    protected static final int MSG_START_PLAY = 0;

    protected XibaVideoPlayer mXibaVideoPlayer;

    protected Context context;

    protected int mXibaVideoPlayerWidth;
    protected int mXibaVideoPlayerHeight;

    //切换播放条目接口
    public interface PlayingItemPositionChange{
        void prePlayingItemPositionChange(Message utilMsg);     //正常播放时，切换播放条目
        void prePlayingItemChangeOnPause();     //当播放器状态为暂停时，切换播放条目
    }

    protected PlayingItemPositionChange playingItemPositionChangeImpl;

    /**
     * 设置接口
     * @param playingItemPositionChangeImpl
     */
    public void setPlayingItemPositionChangeImpl(PlayingItemPositionChange playingItemPositionChangeImpl){
        this.playingItemPositionChangeImpl = playingItemPositionChangeImpl;
    }

    protected UtilHandler mUtilHandler;

    protected boolean isStratFullScreen = false;
    protected boolean isStratTinyScreen = false;

    protected XibaFullScreenEventCallback mFullScreenEventCallback;
    protected XibaTinyScreenEventCallback mTinyScreenEventCallback;

    protected Point mSize;
    protected float mX;
    protected float mY;
    protected boolean mCanMove;

    /**
     * 此Handler用于切换播放条目的过程中，
     * 原来的播放控件完成UI逻辑处理
     * 并将焦点切换到目标播放位置的UI之后，发送消息到此handler，进行目标文件的播放
     */
    protected class UtilHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_PLAY:

                    if (msg != null) {
                        Map<String, Object> msgObj = (Map<String, Object>) msg.obj;
                        ViewGroup itemContainer = (ViewGroup) msgObj.get(KEY_ITEM_CONTAINER);
                        XibaVideoPlayerEventCallback eventCallback = (XibaVideoPlayerEventCallback) msgObj.get(KEY_EVENT_CALLBACK);
                        Object targetIndex = msgObj.get(KEY_POSITION);
                        String url = (String) msgObj.get(KEY_URL);
                        int lastState = (int) msgObj.get(KEY_LAST_STATE);

                        startPlay(url, targetIndex, itemContainer, eventCallback, lastState);

                    }

                    break;
            }
        }
    }

    public XibaBaseListUtil(Context context) {
        this.context = context;
        init(context);
    }

    protected void init(Context context) {
        mXibaVideoPlayer = new XibaVideoPlayer(context);
        mUtilHandler = new UtilHandler();
    }

    /**
     * 全屏播放
     * @param url
     * @param position
     * @param itemContainer
     * @param eventCallback
     * @param fullScreenEventCallback
     */
    protected void startFullScreen(String url, Object position, ViewGroup itemContainer,
                                XibaVideoPlayerEventCallback eventCallback,
                                XibaFullScreenEventCallback fullScreenEventCallback){

        if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY
                || mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
            return;
        }

        this.mFullScreenEventCallback = fullScreenEventCallback;

        if (!isPlayingIndex(position)) {
            isStratFullScreen = true;
            togglePlay(url, position, itemContainer, eventCallback);
        } else {
            enterFullScreen();
        }
    }

    protected void enterFullScreen(){
        mXibaVideoPlayer.setFullScreenEventCallback(mFullScreenEventCallback);
        mXibaVideoPlayer.startFullScreen(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mXibaVideoPlayer.setAutoRotate(false);
    }

    /**
     * 退出全屏
     */
    public void quitFullScreen(){

        mXibaVideoPlayer.quitFullScreen();
        savePlayerInfo();
        mXibaVideoPlayer.setFullScreenEventCallback(null);
    }


    /**
     * 进入小屏模式
     */
//    protected void startTinyScreen(XibaTinyScreenEventCallback tinyScreenEventCallback, Point size, float x, float y, boolean canMove){
    protected void startTinyScreen(){
        mXibaVideoPlayer.setTinyScreenEventCallback(mTinyScreenEventCallback);
        mXibaVideoPlayer.startTinyScreen(mSize, mX, mY, mCanMove);
        savePlayerInfo();
    }

    /**
     * 退出小屏
     */
    protected void quitTinyScreen(ViewGroup itemContainer){
        if (itemContainer !=  null) {
            mXibaVideoPlayer.quitTinyScreen(itemContainer);
        } else {
            mXibaVideoPlayer.quitTinyScreen();
        }

        mXibaVideoPlayer.setTinyScreenEventCallback(null);
        savePlayerInfo();
    }

    /**
     * 进入小屏或退出小屏
     * @param url   播放地址
     * @param targetIndex  在列表中的位置
     * @param itemContainer 播放器容器
     * @param eventCallback 回调事件接口
     * @param tinyScreenEventCallback 小屏相关事件接口
     * @param size  小屏的尺寸
     * @param x 小屏x坐标
     * @param y 小屏y坐标
     * @param canMove   小屏是否可移动
     */
    protected void toggleTinyScreen(String url, Object targetIndex, ViewGroup itemContainer,
                                 XibaVideoPlayerEventCallback eventCallback,
                                 XibaTinyScreenEventCallback tinyScreenEventCallback,
                                 Point size, float x, float y, boolean canMove){

        this.mTinyScreenEventCallback = tinyScreenEventCallback;
        this.mSize = size;
        this.mX = x;
        this.mY = y;
        this.mCanMove = canMove;

        if (!isPlayingIndex(targetIndex)) {

            isStratTinyScreen = true;

            togglePlay(url, targetIndex, itemContainer, eventCallback);

//            startTinyScreen(tinyScreenEventCallback, size, x, y, canMove);
        } else {
            if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                quitTinyScreen(itemContainer);

            } else {
                if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                    return;
                }

                startTinyScreen();
            }
        }
    }

    /**
     * 跳转快进
     * @param url   播放地址
     * @param targetIndex  在列表中的位置
     * @param itemContainer 播放器容器
     * @param eventCallback 回调事件接口
     * @param progress
     * @param maxProgress
     */
    protected void seekTo(String url, Object targetIndex, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback,
                       int progress, int maxProgress){
        //如果当前播放索引不是目标播放索引，切换播放位置
        if (!isPlayingIndex(targetIndex)) {
            PlayerStateInfo stateInfo = getPlayingStateInfoByIndex(targetIndex);
            if (stateInfo != null) {
                //根据进度百分比，得出目标位置
                long seekPosition = stateInfo.getDuration() * progress / maxProgress;
                stateInfo.setPosition(seekPosition);
            }

            togglePlay(url, targetIndex, itemContainer, eventCallback);
        } else {
            mXibaVideoPlayer.seekTo(progress);
        }
    }

    /**
     * 跳转播放位置
     * @param progress
     */
    public void seekTo(int progress){
        mXibaVideoPlayer.seekTo(progress);
    }

    public XibaVideoPlayer getXibaVideoPlayer(){
        return mXibaVideoPlayer;
    }

    /**
     * 暂停或播放
     */
    public void togglePlay(){
        mXibaVideoPlayer.togglePlayPause();
    }

    /**
     * 获取屏幕类型
     * @return
     */
    public int getCurrentScreen(){
        return mXibaVideoPlayer.getCurrentScreen();
    }

    /**
     * 获取锁屏状态
     * @return
     */
    public boolean isScreenLock(){
        return mXibaVideoPlayer.isScreenLock();
    }

//    /**
//     * 获取当前播放索引
//     * @return
//     */
//    public abstract Object getPlayingIndex();

    /**
     * 获取当前视频总时长
     * @return
     */
    public long getDuration(){
        return mXibaVideoPlayer.getDuration();
    }

    /**
     * 获取当前播放位置
     * @return
     */
    public long getCurrentPosition(){
        return mXibaVideoPlayer.getCurrentPositionWhenPlaying();
    }

    /**
     * 获得当前状态
     * @return
     */
    public int getCurrentState(){
        return mXibaVideoPlayer.getCurrentState();
    }

    /**
     * 设置锁屏状态
     * @param lock
     */
    public void lockScreen(boolean lock){
        mXibaVideoPlayer.setScreenLock(lock);
    }

    /**
     * 获取全屏状态下，垂直滑动屏幕时的功能
     */
    public int getFullScreenVerticalFeature() {
        return mXibaVideoPlayer.getFullScreenVerticalFeature();
    }

    /**
     * 设置全屏状态下，垂直滑动屏幕时的功能
     */
    public void setFullScreenVerticalFeature(int fullScreenVerticalFeature) {
        mXibaVideoPlayer.setFullScreenVerticalFeature(fullScreenVerticalFeature);
    }

    /**
     * 获取全屏状态下，水平滑动屏幕时的功能
     */
    public int getFullScreenHorizontalFeature() {
        return mXibaVideoPlayer.getFullScreenHorizontalFeature();
    }

    /**
     * 设置全屏状态下，水平滑动屏幕时的功能
     * @param fullScreenHorizontalFeature
     */
    public void setFullScreenHorizontalFeature(int fullScreenHorizontalFeature) {
        mXibaVideoPlayer.setFullScreenHorizontalFeature(fullScreenHorizontalFeature);
    }

    /**
     * 获取水平滑动影响值
     * @return
     */
    public int getHorizontalSlopInfluenceValue() {
        return mXibaVideoPlayer.getHorizontalSlopInfluenceValue();
    }

    /**
     * 设置水平滑动的影响值
     * 例如参数为2，那么滑动整个屏幕改变整个播放器时长的一半
     * 参数需要大于0的整数，传入参数如果小于等于0，自动将参数修改为1
     * 推荐在onPlayerPrepare事件中，获取视频长度之后，根据需求修改此参数
     * @param horizontalSlopInfluenceValue
     */
    public void setHorizontalSlopInfluenceValue(int horizontalSlopInfluenceValue) {
        mXibaVideoPlayer.setHorizontalSlopInfluenceValue(horizontalSlopInfluenceValue);
    }

    /**
     * 为播放器设置事件回调
     * @param eventCallback
     */
    public void setEventCallback(XibaVideoPlayerEventCallback eventCallback){
        if (eventCallback != null) {
            mXibaVideoPlayer.setEventCallback(eventCallback);
        }
    }

    /**
     * 设置动作事件相关回调接口
     * @param actionEventCallback
     */
    public void setPlayerActionEventCallback(XibaPlayerActionEventCallback actionEventCallback){
        mXibaVideoPlayer.setPlayerActionEventCallback(actionEventCallback);
    }

    public boolean onBackPress(){
        if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
            quitFullScreen();
            return true;
        }
        if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
            quitTinyScreen(null);
            return true;
        }
        return false;
    }

    /**
     * 1.开始播放
     * 2.暂停
     * 3.恢复播放 播放位置
     *
     * @param url
     * @param targetIndex
     * @param itemContainer
     * @param eventCallback
     */
    protected void togglePlay(String url, Object targetIndex, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {



        if (!isPlayingIndex(targetIndex)) {

            //如果播放器在小屏播放状态，先退出小屏状态
            if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                quitTinyScreen(null);
            }

            //如果播放器在全屏播放状态，先退出全屏状态
            if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                quitFullScreen();
            }

            int lastState = mXibaVideoPlayer.getCurrentState();

            /**
             * 先保存，删除，然后setUp播放器，最后再添加到itemContainer
             */
            if (!isPlayingIndexNull()) {

                if (playingItemPositionChangeImpl != null) {

                    //由于屏幕滑动的过程中，会将eventCallback设成null，导致无法调用暂停回调
                    //因此，这里需要设置eventCallback
                    mXibaVideoPlayer.setEventCallback(eventCallback);

                    //创建handler消息
                    Map<String, Object> msgObj = new HashMap<>();
                    msgObj.put(KEY_ITEM_CONTAINER, itemContainer);
                    msgObj.put(KEY_EVENT_CALLBACK, eventCallback);
                    msgObj.put(KEY_POSITION, targetIndex);
                    msgObj.put(KEY_URL, url);
                    msgObj.put(KEY_LAST_STATE, lastState);

                    Message utilMsg = mUtilHandler.obtainMessage(MSG_START_PLAY, msgObj);

                    //如果播放器为播放状态，暂停播放器
                    if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
                        //调用播放位置变更接口
                        playingItemPositionChangeImpl.prePlayingItemPositionChange(utilMsg);

                        mXibaVideoPlayer.pausePlayer();

                    } else if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {

                        playingItemPositionChangeImpl.prePlayingItemChangeOnPause();
                        startPlay(url, targetIndex, itemContainer, eventCallback, lastState);
                    }

                } else {
                    //如果播放器为播放状态，暂停播放器
                    if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING
                            || mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {
                        mXibaVideoPlayer.pausePlayer();
                    }
                    startPlay(url, targetIndex, itemContainer, eventCallback, lastState);
                }

            } else {
                //如果播放器为播放状态，暂停播放器
                if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING
                        || mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {
                    mXibaVideoPlayer.pausePlayer();
                }
                startPlay(url, targetIndex, itemContainer, eventCallback, lastState);
            }
        }
        else{

            mXibaVideoPlayer.togglePlayPause();
        }

    }

    protected void startPlay(String url, Object targetIndex, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback, int lastState){

        //将播放器从当前父容器中移出
        removePlayerFromParent(lastState);

        //设置播放索引为当前索引
        setPlayingIndex(targetIndex);

        //如果有保存播放信息，恢复上次播放位置
        PlayerStateInfo playerStateInfo = getPlayingStateInfoByIndex(targetIndex);
        if (playerStateInfo != null) {
            mXibaVideoPlayer.setUp(url, XibaVideoPlayer.SCREEN_LIST, playerStateInfo.getPosition(), playerStateInfo.getCacheBitmap());
        } else {
//            mXibaVideoPlayer.setUp(url, XibaVideoPlayer.SCREEN_LIST, new Object() {});
            mXibaVideoPlayer.setUp(url, XibaVideoPlayer.SCREEN_LIST);
        }

        addToListItem(itemContainer, eventCallback);    //添加到目标容器中

        mXibaVideoPlayer.togglePlayPause();     //开始播放

        //如果是在全屏播放的过程中，开始播放之后进入全屏状态
        if (isStratFullScreen) {
            enterFullScreen();
            isStratFullScreen = false;
        } else if (isStratTinyScreen) {     //如果是在进入小屏播放的过程中，开始播放之后进入小屏状态
            startTinyScreen();
            isStratTinyScreen = false;
        }
    }

    /**
     * 根据position和播放器的状态，来确定itemContainer中的内容
     * @param targetIndex
     * @param itemContainer
     * @param eventCallback
     * @return
     */
    protected PlayerStateInfo resolveItem(Object targetIndex, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {

        if (itemContainer != null) {

            PlayerStateInfo stateInfo = getPlayingStateInfoByIndex(targetIndex);

            if (stateInfo != null && stateInfo.getCurrentState() == XibaVideoPlayer.STATE_PAUSE && !isPlayingIndex(targetIndex)) {
                //如果当期item为暂停状态，添加暂停图片
                addCacheImageView(itemContainer, stateInfo.getCacheBitmap());
            } else {
                //如果有缓存图片就删除
                removeCacheImageView(itemContainer);
            }


            if (isPlayingIndex(targetIndex)) {
                //如果item为正在播放的item，将播放器添加到item中
                if (itemContainer.indexOfChild(mXibaVideoPlayer) == -1) {
                    addToListItem(itemContainer, eventCallback);
                }

                if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                    itemContainer.setTag(XibaVideoPlayer.TINY_SCREEN_CONTAINER_TAG);
                    mXibaVideoPlayer.setEventCallback(eventCallback);
                }

            } else {
                //如果播放器被复用，但又不是当前播放的索引，将播放器从容器中移出，并添加到contentView中
                if (itemContainer.indexOfChild(mXibaVideoPlayer) != -1) {
//                    removeFromList(-1);
                    removePlayerFromParent();
                    addToContentView();
                }

                //在小屏模式下，如果item被复用，将事件回调设为null
                if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY
                        && itemContainer.getTag() != null
                        && itemContainer.getTag().equals(XibaVideoPlayer.TINY_SCREEN_CONTAINER_TAG)) {
                    itemContainer.setTag("");
                    mXibaVideoPlayer.setEventCallback(null);
                }
            }

        }

        return getPlayingStateInfoByIndex(targetIndex);
    }

    /**
     * 将播放器添加到Item中
     *
     * @param itemContainer
     * @param eventCallback
     */
    public void addToListItem(ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {

        if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN
                || mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY
                ) {
            return;
        }

        removeCacheImageView(itemContainer);    //移出itemContainer中的暂停图片

        ViewGroup parent = (ViewGroup) mXibaVideoPlayer.getParent();

        //如果播放器已经在目标容器中，直接返回
        if (parent != null) {
            if (parent == itemContainer) {
                return;
            } else {
                removePlayerFromParent();
            }
        }

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        itemContainer.addView(mXibaVideoPlayer, 0, layoutParams);

        mXibaVideoPlayer.setY(0);
        mXibaVideoPlayer.setTag(PLAYER_TAG_ITEM_CONTAINER);
        mXibaVideoPlayer.setEventCallback(eventCallback);

    }

    protected void removePlayer(Object targetIndex){
        if (isPlayingIndex(targetIndex)) {
            removePlayerFromParent();
            addToContentView();
        }
    }

    /**
     * 将播放器从父容器中移出
     */
    protected void removePlayerFromParent(){

        if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN
                || mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY
                ) {
            return;
        }

        //保存移出时候的状态
        savePlayerInfo();

        mXibaVideoPlayer.setEventCallback(null);

        ViewGroup parent = (ViewGroup) mXibaVideoPlayer.getParent();

        if (parent != null) {

            mXibaVideoPlayerWidth = mXibaVideoPlayer.getWidth();    //获取播放器宽
            mXibaVideoPlayerHeight = mXibaVideoPlayer.getHeight();  //获取播放器高

            parent.removeView(mXibaVideoPlayer);
            mXibaVideoPlayer.setTag(PLAYER_TAG_NO_CONTAINER);
        }
    }

    /**
     * 将播放器从父容器中移出
     * @param lastState 根据状态判断是否需要添加暂停图片
     */
    protected void removePlayerFromParent(int lastState){

        if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN
                || mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY
                ) {
            return;
        }

        //保存移出时候的状态
        PlayerStateInfo playerStateInfo = savePlayerInfo();

        mXibaVideoPlayer.setEventCallback(null);

        ViewGroup parent = (ViewGroup) mXibaVideoPlayer.getParent();

        if (parent != null) {

            if ((lastState == XibaVideoPlayer.STATE_PLAYING || lastState == XibaVideoPlayer.STATE_PAUSE)
                    && mXibaVideoPlayer.getTag().equals(PLAYER_TAG_ITEM_CONTAINER)) {
                addCacheImageView(parent, playerStateInfo.getCacheBitmap());    //添加暂停图片
            }

            mXibaVideoPlayerWidth = mXibaVideoPlayer.getWidth();    //获取播放器宽
            mXibaVideoPlayerHeight = mXibaVideoPlayer.getHeight();  //获取播放器高

            parent.removeView(mXibaVideoPlayer);
            mXibaVideoPlayer.setTag(PLAYER_TAG_NO_CONTAINER);
        }
    }

    /**
     * 将播放器添加到ContentView中
     */
    protected void addToContentView(){
        if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN
                || mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY
                ) {
            return;
        }

        ViewGroup contentView = (ViewGroup) ((Activity)context).getWindow().findViewById(Window.ID_ANDROID_CONTENT);

        //如果ContentView中已经有播放器，直接返回
        if (contentView.indexOfChild(mXibaVideoPlayer) != -1) {
            return;
        }

        mXibaVideoPlayer.setTag(PLAYER_TAG_CONTENT_VIEW);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(mXibaVideoPlayerWidth, mXibaVideoPlayerHeight);
        contentView.addView(mXibaVideoPlayer, 0, params);

        //让播放器在屏幕上方不可见，这样暂停的时候，依然可以拿到暂停图片
        mXibaVideoPlayer.setY(-mXibaVideoPlayerHeight);
    }


    /**
     * 保存当前正在播放的播放器状态
     */
    public PlayerStateInfo savePlayerInfo(){
        PlayerStateInfo playerStateInfo = getPlayingStateInfo();

        if (playerStateInfo == null) {
            playerStateInfo = new PlayerStateInfo();
        }

        playerStateInfo.setCurrentState(mXibaVideoPlayer.getCurrentState());
        playerStateInfo.setCacheBitmap(mXibaVideoPlayer.getCacheBitmap());
        playerStateInfo.setDuration(mXibaVideoPlayer.getDuration());
        playerStateInfo.setPosition(mXibaVideoPlayer.getCurrentPositionWhenPlaying());
        playerStateInfo.setCurrentScreen(mXibaVideoPlayer.getCurrentScreen());

        setPlayingStateInfo(playerStateInfo);

        return playerStateInfo;
    }



    /**
     * 添加暂停时的缓存图片
     * @param itemContainer
     * @param cacheBitmap
     */
    protected void addCacheImageView(ViewGroup itemContainer, Bitmap cacheBitmap){

        if (itemContainer == null) {
            return;
        }

        ImageView cacheIV = null;

        //当前itemContainer是否存在cache控件
        View cache = itemContainer.findViewWithTag("cache");

        //如果itemContainer中存在cache，直接使用
        if (cache != null) {
            cacheIV = (ImageView) cache;
        }

        //如果当前itemContainer不存在cache，创建一个添加到itemContainer中
        if (cacheIV == null) {
            cacheIV = new ImageView(context);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            cacheIV.setTag("cache");

            itemContainer.addView(cacheIV, 0, layoutParams);
        }

        //为cache设置图片
        cacheIV.setImageBitmap(cacheBitmap);

    }

    /**
     * 移出暂停时的缓存图片
     * @param itemContainer
     */
    protected void removeCacheImageView(ViewGroup itemContainer){
        if (itemContainer == null) {
            return;
        }

        //如果当前itemContainer存在cache，将cache移出
        View cache = itemContainer.findViewWithTag("cache");
        if (cache != null) {
            cache.setTag("");
            itemContainer.removeView(cache);
        }
    }



    /**
     * 获取正在播放的播放器状态
     * @return
     */
    protected abstract PlayerStateInfo getPlayingStateInfo();

    /**
     * 保存正在播放的播放器状态
     * @param playerStateInfo
     */
    protected abstract void setPlayingStateInfo(PlayerStateInfo playerStateInfo);

    /**
     * 根据索引来获取播放信息
     * @param targetIndex
     * @return
     */
    protected abstract PlayerStateInfo getPlayingStateInfoByIndex(Object targetIndex);

    /**
     * 目标索引是否是正在播放的索引
     * @param targetIndex
     * @return true 是目标索引是正在播放的索引
     */
    protected abstract boolean isPlayingIndex(Object targetIndex);

    /**
     * 当前正在播放的索引是否为空，还没有播放过任何视频
     * @return true 当前正在播放的索引为空
     */
    protected abstract boolean isPlayingIndexNull();

    /**
     * 设置目标索引
     * @param targetIndex
     * @return
     */
    protected abstract void setPlayingIndex(Object targetIndex);

    /**
     * 释放资源
     */
    public abstract void release();

}

