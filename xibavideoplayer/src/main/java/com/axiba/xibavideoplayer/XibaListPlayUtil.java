package com.axiba.xibavideoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by xiba on 2016/12/13.
 */

public class XibaListPlayUtil {

    public static final String TAG = XibaListPlayUtil.class.getSimpleName();

    private XibaVideoPlayer mXibaVideoPlayer;

    private int mPlayingPosition = -1;  //当前正在播放的item索引

    private SparseArray<PlayerStateInfo> stateInfoList;

    private Context context;


    public XibaListPlayUtil(Context context) {
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        mXibaVideoPlayer = new XibaVideoPlayer(context);
        stateInfoList = new SparseArray<>();
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

            int lastState = mXibaVideoPlayer.getCurrentState();

            if (mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PLAYING
                    || mXibaVideoPlayer.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {
                mXibaVideoPlayer.pausePlayer();
            }
            
            /**
             * 先删除，保存，然后setUp播放器，最后再添加到itemContainer
             */
            removeFromList(eventCallback, lastState);


            Log.e(TAG, "togglePlay: mPlayingPosition=" + mPlayingPosition);
            Log.e(TAG, "togglePlay: position=" + position);
            //设置播放索引为当前索引
            mPlayingPosition = position;

            //如果有保存播放信息，恢复上次播放位置
            PlayerStateInfo playerStateInfo = stateInfoList.get(position);
            if (playerStateInfo != null) {
                mXibaVideoPlayer.setUp(url, XibaVideoPlayer.SCREEN_LIST, playerStateInfo.getPosition(), playerStateInfo.getCacheBitmap());
            } else {
                mXibaVideoPlayer.setUp(url, XibaVideoPlayer.SCREEN_LIST, new Object() {});
            }

            removeCacheImageView(itemContainer);    //清除缓存图片

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            mXibaVideoPlayer.setEventCallback(eventCallback);
            itemContainer.addView(mXibaVideoPlayer, 0, layoutParams);   //添加播放器到目标Item


        } else {
            removeCacheImageView(itemContainer);    //清除缓存图片
        }

        mXibaVideoPlayer.togglePlayPause();

    }

    public PlayerStateInfo resolveItem(int position, ViewGroup itemContainer, XibaVideoPlayerEventCallback eventCallback) {

        if (itemContainer != null) {

            PlayerStateInfo stateInfo = stateInfoList.get(position);

            if (stateInfo != null && stateInfo.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {

                addCacheImageView(itemContainer, stateInfo.getCacheBitmap());

//                if (stateInfo.getCurrentState() == XibaVideoPlayer.STATE_PAUSE) {
//                    //显示缓存图片
//                    addCacheImageView(itemContainer, stateInfo.getCacheBitmap());
//                } else {
//                    //如果有缓存图片，删除缓存图片
//                    removeCacheImageView(itemContainer);
//                }

            } else {
                //如果有缓存图片就删除
                removeCacheImageView(itemContainer);
            }



            if (mPlayingPosition == position) { //当前播放的item
                if (itemContainer.indexOfChild(mXibaVideoPlayer) == -1) {
                    addToListItem(itemContainer, eventCallback);
                }
            } else {
                //如果播放器被复用，但又不是当前播放的索引，将播放器从容器中移出
                if (itemContainer.indexOfChild(mXibaVideoPlayer) != -1) {
                    removeFromList(eventCallback, -1);
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

        ViewGroup parent = (ViewGroup) mXibaVideoPlayer.getParent();

        //如果播放器已经在目标容器中，直接返回
        if (parent != null) {
            if (parent == itemContainer) {
                return;
            } else {
//                parent.removeView(mXibaVideoPlayer);
                removeFromList(eventCallback, -1);
            }
        }

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        itemContainer.addView(mXibaVideoPlayer, 0, layoutParams);

        mXibaVideoPlayer.setEventCallback(eventCallback);
    }

    /**
     * 将播放器从Item中移出
     */
    public void removeFromList(XibaVideoPlayerEventCallback eventCallback, int lastState) {

        //保存移出时候的状态
        PlayerStateInfo playerStateInfo = stateInfoList.get(mPlayingPosition);

        if (playerStateInfo == null) {
            playerStateInfo = new PlayerStateInfo();
        }

        Log.e(TAG, "mXibaVideoPlayer.getCurrentState()=" + mXibaVideoPlayer.getCurrentState());
        Log.e(TAG, "mXibaVideoPlayer.getCacheBitmap()=null?" + (mXibaVideoPlayer.getCacheBitmap() == null));

        playerStateInfo.setCurrentState(mXibaVideoPlayer.getCurrentState());
        playerStateInfo.setCacheBitmap(mXibaVideoPlayer.getCacheBitmap());
        playerStateInfo.setDuration(mXibaVideoPlayer.getDuration());
        playerStateInfo.setPosition(mXibaVideoPlayer.getCurrentPositionWhenPlaying());

        stateInfoList.put(mPlayingPosition, playerStateInfo);

//        eventCallback.onCacheBitmap(playerStateInfo.getCacheBitmap());

        mXibaVideoPlayer.setEventCallback(null);

        ViewGroup parent = (ViewGroup) mXibaVideoPlayer.getParent();


        if (parent != null) {

            if (lastState == XibaVideoPlayer.STATE_PLAYING) {
                addCacheImageView(parent, playerStateInfo.getCacheBitmap());
            }

            parent.removeView(mXibaVideoPlayer);
        }
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

//        if (itemContainer.getChildAt(0) instanceof ImageView) {
//            return;
//        }

        ImageView cacheIV = null;
        for (int i = 0; i < itemContainer.getChildCount(); i++) {
            if (itemContainer.getChildAt(i) instanceof ImageView) {
                cacheIV = (ImageView) itemContainer.getChildAt(i);
                break;
            }
        }

        if (cacheIV == null) {
            cacheIV = new ImageView(context);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            itemContainer.addView(cacheIV, 0, layoutParams);
        }

        cacheIV.setImageBitmap(cacheBitmap);

//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT);
//
//        itemContainer.addView(cacheIV, 0, layoutParams);
    }

    /**
     * 移出暂停时的缓存图片
     * @param itemContainer
     */
    private void removeCacheImageView(ViewGroup itemContainer){
        if (itemContainer == null) {
            return;
        }

        for (int i = 0; i < itemContainer.getChildCount(); i++) {
            if (itemContainer.getChildAt(i) instanceof ImageView) {
                itemContainer.removeViewAt(i);
//                return;
            }
        }
//        if (itemContainer.getChildAt(0) instanceof ImageView) {
//            itemContainer.removeViewAt(0);
//        }

    }

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
    }
}
