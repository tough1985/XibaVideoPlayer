package com.axiba.xibavideoplayer;

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

import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiba on 2016/12/13.
 */

public class XibaListPlayUtil {

    public static final String TAG = XibaListPlayUtil.class.getSimpleName();

    public static final String PLAYER_TAG_NO_CONTAINER = "";                 //没有父容器
    public static final String PLAYER_TAG_ITEM_CONTAINER = "itemContainer";  //父容器是itemContainer
    public static final String PLAYER_TAG_CONTENT_VIEW = "contentView";      //父容器是ContentView

    private static final String KEY_ITEM_CONTAINER = "itemContainer";
    private static final String KEY_EVENT_CALLBACK = "eventCallback";
    private static final String KEY_POSITION = "position";
    private static final String KEY_URL = "url";
    private static final String KEY_LAST_STATE = "lastState";

    private static final int MSG_START_PLAY = 0;

    private XibaVideoPlayer mXibaVideoPlayer;

    private int mPlayingPosition = -1;  //当前正在播放的item索引

    private SparseArray<PlayerStateInfo> stateInfoList;     //已经播放过的视频信息列表

    private Context context;

    private int mXibaVideoPlayerWidth;
    private int mXibaVideoPlayerHeight;

    //切换播放条目接口
    public interface PlayingItemPositionChange{
        void prePlayingItemPositionChange(Message utilMsg);     //正常播放时，切换播放条目
        void prePlayingItemChangeOnPause();     //当播放器状态为暂停时，切换播放条目
    }

    private PlayingItemPositionChange playingItemPositionChangeImpl;

    /**
     * 设置接口
     * @param playingItemPositionChangeImpl
     */
    public void setPlayingItemPositionChangeImpl(PlayingItemPositionChange playingItemPositionChangeImpl){
        this.playingItemPositionChangeImpl = playingItemPositionChangeImpl;
    }

    private UtilHandler mUtilHandler;

    private boolean isStratFullScreen = false;
    private boolean isStratTinyScreen = false;

    private XibaFullScreenEventCallback mFullScreenEventCallback;
    private XibaTinyScreenEventCallback mTinyScreenEventCallback;

    private Point mSize;
    private float mX;
    private float mY;
    private boolean mCanMove;

    /**
     * 此Handler用于切换播放条目的过程中，
     * 原来的播放控件完成UI逻辑处理
     * 并将焦点切换到目标播放位置的UI之后，发送消息到此handler，进行目标文件的播放
     */
    private class UtilHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_PLAY:

                    if (msg != null) {
                        Map<String, Object> msgObj = (Map<String, Object>) msg.obj;
                        ViewGroup itemContainer = (ViewGroup) msgObj.get(KEY_ITEM_CONTAINER);
                        XibaVideoPlayerEventCallback eventCallback = (XibaVideoPlayerEventCallback) msgObj.get(KEY_EVENT_CALLBACK);
                        int position = (int) msgObj.get(KEY_POSITION);
                        String url = (String) msgObj.get(KEY_URL);
                        int lastState = (int) msgObj.get(KEY_LAST_STATE);

                        startPlay(url, position, itemContainer, eventCallback, lastState);

                    }

                    break;
            }
        }
    }

    public XibaListPlayUtil(Context context) {
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        mXibaVideoPlayer = new XibaVideoPlayer(context);
        stateInfoList = new SparseArray<>();

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
    public void startFullScreen(String url, int position, ViewGroup itemContainer,
                                XibaVideoPlayerEventCallback eventCallback,
                                XibaFullScreenEventCallback fullScreenEventCallback){

        if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY
                || mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
            return;
        }

        this.mFullScreenEventCallback = fullScreenEventCallback;

        if (mPlayingPosition != position) {
            isStratFullScreen = true;
            togglePlay(url, position, itemContainer, eventCallback);
        } else {
            enterFullScreen();
        }
    }

    private void enterFullScreen(){
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
//    private void startTinyScreen(XibaTinyScreenEventCallback tinyScreenEventCallback, Point size, float x, float y, boolean canMove){
    private void startTinyScreen(){
        mXibaVideoPlayer.setTinyScreenEventCallback(mTinyScreenEventCallback);
        mXibaVideoPlayer.startTinyScreen(mSize, mX, mY, mCanMove);
        savePlayerInfo();
    }

    /**
     * 退出小屏
     */
    private void quitTinyScreen(ViewGroup itemContainer){
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
     * @param position  在列表中的位置
     * @param itemContainer 播放器容器
     * @param eventCallback 回调事件接口
     * @param tinyScreenEventCallback 小屏相关事件接口
     * @param size  小屏的尺寸
     * @param x 小屏x坐标
     * @param y 小屏y坐标
     * @param canMove   小屏是否可移动
     */
    public void toggleTinyScreen(String url, int position, ViewGroup itemContainer,
                                 XibaVideoPlayerEventCallback eventCallback,
                                 XibaTinyScreenEventCallback tinyScreenEventCallback,
                                 Point size, float x, float y, boolean canMove){

        this.mTinyScreenEventCallback = tinyScreenEventCallback;
        this.mSize = size;
        this.mX = x;
        this.mY = y;
        this.mCanMove = canMove;

        if (mPlayingPosition != position) {

            isStratTinyScreen = true;

            togglePlay(url, position, itemContainer, eventCallback);

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
     * @param position  在列表中的位置
     * @param itemContainer 播放器容器
     * @param eventCallback 回调事件接口
     * @param progress
     * @param maxProgress
     */
    public void seekTo(String url, int position, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback,
                       int progress, int maxProgress){
        if (mPlayingPosition != position) {
            PlayerStateInfo stateInfo = stateInfoList.get(position);
            if (stateInfo != null) {
                //根据进度百分比，得出目标位置
                long seekPosition = stateInfo.getDuration() * progress / maxProgress;
                stateInfo.setPosition(seekPosition);
            }

            togglePlay(url, position, itemContainer, eventCallback);
        } else {
            mXibaVideoPlayer.seekTo(progress);
        }

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

    public int getPlayingPosition(){
        return mPlayingPosition;
    }

    /**
     * 设置锁屏状态
     * @param lock
     */
    public void lockScreen(boolean lock){
        mXibaVideoPlayer.setScreenLock(lock);
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
     * @param position
     * @param itemContainer
     * @param eventCallback
     */
    public void togglePlay(String url, int position, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {



        if (mPlayingPosition != position) {

            //如果播放器在小屏播放状态，先退出小屏状态
            if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
//                quitTinyScreen(itemContainer);
                quitTinyScreen(null);
            }

            //如果播放器在全屏播放状态，先退出全屏状态
            if (mXibaVideoPlayer.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                quitFullScreen();
            }

            int lastState = mXibaVideoPlayer.getCurrentState();

//            //如果播放器为播放状态，暂停播放器
//            if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING
//                    || mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {
//                mXibaVideoPlayer.pausePlayer();
//            }
            
            /**
             * 先保存，删除，然后setUp播放器，最后再添加到itemContainer
             */
//            removeFromList(lastState);
            if (mPlayingPosition != -1) {

                if (playingItemPositionChangeImpl != null) {

                    //由于屏幕滑动的过程中，会将eventCallback设成null，导致无法调用暂停回调
                    //因此，这里需要设置eventCallback
                    mXibaVideoPlayer.setEventCallback(eventCallback);

                    //创建handler消息
                    Map<String, Object> msgObj = new HashMap<>();
                    msgObj.put(KEY_ITEM_CONTAINER, itemContainer);
                    msgObj.put(KEY_EVENT_CALLBACK, eventCallback);
                    msgObj.put(KEY_POSITION, position);
                    msgObj.put(KEY_URL, url);
                    msgObj.put(KEY_LAST_STATE, lastState);

                    Message utilMsg = mUtilHandler.obtainMessage(MSG_START_PLAY, msgObj);

//                    //调用播放位置变更接口
//                    playingItemPositionChangeImpl.prePlayingItemPositionChange(utilMsg);

                    //如果播放器为播放状态，暂停播放器
                    if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
                        //调用播放位置变更接口
                        playingItemPositionChangeImpl.prePlayingItemPositionChange(utilMsg);

                        mXibaVideoPlayer.pausePlayer();

                    } else if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {

                        playingItemPositionChangeImpl.prePlayingItemChangeOnPause();
                        startPlay(url, position, itemContainer, eventCallback, lastState);
                    }

                } else {
                    //如果播放器为播放状态，暂停播放器
                    if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING
                            || mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {
                        mXibaVideoPlayer.pausePlayer();
                    }
                    startPlay(url, position, itemContainer, eventCallback, lastState);
                }

            } else {
                //如果播放器为播放状态，暂停播放器
                if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING
                        || mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {
                    mXibaVideoPlayer.pausePlayer();
                }
                startPlay(url, position, itemContainer, eventCallback, lastState);
            }
        }
        else{

            mXibaVideoPlayer.togglePlayPause();
        }

    }

    private void startPlay(String url, int position, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback, int lastState){

        //将播放器从当前父容器中移出
        removePlayerFromParent(lastState);

        //设置播放索引为当前索引
        mPlayingPosition = position;

        //如果有保存播放信息，恢复上次播放位置
        PlayerStateInfo playerStateInfo = stateInfoList.get(position);
        if (playerStateInfo != null) {
            mXibaVideoPlayer.setUp(url, XibaVideoPlayer.SCREEN_LIST, playerStateInfo.getPosition(), playerStateInfo.getCacheBitmap());
        } else {
            mXibaVideoPlayer.setUp(url, XibaVideoPlayer.SCREEN_LIST, new Object() {});
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
     * @param position
     * @param itemContainer
     * @param eventCallback
     * @return
     */
    public PlayerStateInfo resolveItem(int position, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {

        if (itemContainer != null) {

            PlayerStateInfo stateInfo = stateInfoList.get(position);

            if (stateInfo != null && stateInfo.getCurrentState() == XibaVideoPlayer.STATE_PAUSE && mPlayingPosition != position) {
                //如果当期item为暂停状态，添加暂停图片
                addCacheImageView(itemContainer, stateInfo.getCacheBitmap());
            } else {
                //如果有缓存图片就删除
                removeCacheImageView(itemContainer);
            }

            if (mPlayingPosition == position) {
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

        return stateInfoList.get(position);
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

    public void removePlayer(int position){
        if (position == mPlayingPosition) {
            removePlayerFromParent();
            addToContentView();
        }
    }

    /**
     * 将播放器从父容器中移出
     */
    private void removePlayerFromParent(){

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
    private void removePlayerFromParent(int lastState){

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
    private void addToContentView(){
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
    private PlayerStateInfo savePlayerInfo(){
        PlayerStateInfo playerStateInfo = stateInfoList.get(mPlayingPosition);

        if (playerStateInfo == null) {
            playerStateInfo = new PlayerStateInfo();
        }

        playerStateInfo.setCurrentState(mXibaVideoPlayer.getCurrentState());
        playerStateInfo.setCacheBitmap(mXibaVideoPlayer.getCacheBitmap());
        playerStateInfo.setDuration(mXibaVideoPlayer.getDuration());
        playerStateInfo.setPosition(mXibaVideoPlayer.getCurrentPositionWhenPlaying());
        playerStateInfo.setCurrentScreen(mXibaVideoPlayer.getCurrentScreen());

        stateInfoList.put(mPlayingPosition, playerStateInfo);
        return playerStateInfo;
    }

    /**
     * 添加暂停时的缓存图片
     * @param itemContainer
     * @param cacheBitmap
     */
    private void addCacheImageView(ViewGroup itemContainer, Bitmap cacheBitmap){

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
    private void removeCacheImageView(ViewGroup itemContainer){
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
     * 释放资源
     */
    public void release(){
        mXibaVideoPlayer.release();
        if (stateInfoList != null) {
            for (int i = 0; i < stateInfoList.size(); i++) {
                stateInfoList.get(stateInfoList.keyAt(i)).releaseBitmap();
            }
            stateInfoList = null;
        }
    }

    public class PlayerStateInfo {
        private int currentState;   //播放器当前状态
        private long position;      //当前位置
        private long duration;     //总时长
        private Bitmap cacheBitmap; //暂停时的缓存图片
        private int currentScreen;

        public int getCurrentState() {
            return currentState;
        }

        public void setCurrentState(int currentState) {
            this.currentState = currentState;
        }

        public long getPosition() {
            return position;
        }

        public void setPosition(long position) {
            this.position = position;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public Bitmap getCacheBitmap() {
            return cacheBitmap;
        }

        public void setCacheBitmap(Bitmap cacheBitmap) {
            if (cacheBitmap != null) {
                this.cacheBitmap = cacheBitmap.copy(Bitmap.Config.ARGB_8888, false);
            } else {
                this.cacheBitmap = null;
            }
        }

        public void releaseBitmap(){
            if (cacheBitmap != null) {
                cacheBitmap.recycle();
            }
        }

        public int getCurrentScreen() {
            return currentScreen;
        }

        public void setCurrentScreen(int currentScreen) {
            this.currentScreen = currentScreen;
        }

    }
}
