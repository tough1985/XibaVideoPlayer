package com.axiba.xibavideoplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaPlayerActionEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.listener.OnAutoOrientationChangedListener;
import com.axiba.xibavideoplayer.listener.TinyWindowOnTouchListener;
import com.axiba.xibavideoplayer.listener.XibaMediaListener;
import com.axiba.xibavideoplayer.utils.OrientationUtils;
import com.axiba.xibavideoplayer.utils.XibaUtil;
import com.axiba.xibavideoplayer.view.XibaResizeImageView;
import com.axiba.xibavideoplayer.view.XibaResizeTextureView;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by xiba on 2016/11/26.
 */
public class XibaVideoPlayer extends FrameLayout implements TextureView.SurfaceTextureListener, XibaMediaListener, OnAutoOrientationChangedListener {

    public static final String TAG = XibaVideoPlayer.class.getSimpleName();

    public static final String NAMESPACE = "http://schemas.android.com/apk/res/android";

//    public static final int FULLSCREEN_ID = R.id.fullscreen_id;
//    public static final int TINYSCREEN_ID = R.id.tinyscreen_id;

    private int fullScreenContainerID = NO_ID;

    public static final String TINY_SCREEN_CONTAINER_TAG = "playerContainer";
    /**
     * 播放状态
     */
    public static final int STATE_NORMAL = 0;                   //正常
    public static final int STATE_PREPARING = 1;               //准备中
    public static final int STATE_PLAYING = 2;                  //播放中
    public static final int STATE_PLAYING_BUFFERING_START = 3;  //开始缓冲
    public static final int STATE_PAUSE = 4;                    //暂停
    public static final int STATE_COMPLETE = 5;                    //
    public static final int STATE_AUTO_COMPLETE = 6;            //自动播放结束
    public static final int STATE_ERROR = 7;                    //错误状态

    private int mCurrentState = -1;                       //当前的播放状态
    private int mLastState = -1;                       //退出前的播放状态

    /**
     * 屏幕状态
     */
    public static final int SCREEN_NORMAL = 0;              //正常
    public static final int SCREEN_WINDOW_FULLSCREEN = 1;   //全屏
    public static final int SCREEN_WINDOW_TINY = 2;         //小屏
    public static final int SCREEN_LIST = 3;                //列表

    private int mCurrentScreen = -1;                         //当前屏幕状态
    private int mSetUpScreen = -1;                          //初始时的屏幕状态

    /**
     * 垂直滑动时，什么都不做
     */
    public static final int SLIDING_VERTICAL_NONE = 0;
    /**
     *垂直滑动只改变声音
     */
    public static final int SLIDING_VERTICAL_ONLY_VOLUME = 1;
    /**
     * 垂直滑动只改变亮度
     */
    public static final int SLIDING_VERTICAL_ONLY_BRIGHTNESS = 2;
    /**
     * 垂直滑动左侧改变声音，右侧改变亮度
     */
    public static final int SLIDING_VERTICAL_LEFT_VOLUME = 3;
    /**
     * 垂直滑动左侧改变亮度，右侧改变声音
     */
    public static final int SLIDING_VERTICAL_LEFT_BRIGHTNESS = 4;

    private int mNormalScreenVerticalFeature = SLIDING_VERTICAL_NONE;     //普通屏幕下垂直滑动的功能
    private int mFullScreenVerticalFeature = SLIDING_VERTICAL_NONE;       //全屏下垂直滑动的功能

    /**
     * 水平滑动时，什么都不做
     */
    public static final int SLIDING_HORIZONTAL_NONE = 0;

    /**
     * 水平滑动时，改变播放位置
     */
    public static final int SLIDING_HORIZONTAL_CHANGE_POSITION = 1;

    private int mNormalScreenHorizontalFeature = SLIDING_HORIZONTAL_NONE;    //普通屏幕下水平滑动的功能
    private int mFullScreenHorizontalFeature = SLIDING_HORIZONTAL_NONE;      //全屏下水平滑动的功能

    /**
     * 水平滑动的影响值，用来影响滑动屏幕时，改变的进度
     */
    private int mHorizontalSlopInfluenceValue = 1;

    protected String url;                                   //播放地址

    protected Map<String, String> mapHeadData = new HashMap<>();
//    protected Object[] objects = null;
    protected boolean mLooping = false;

    protected AudioManager mAudioManager;                   //音频焦点的监听

    protected XibaVideoPlayerEventCallback eventCallback;   //播放器事件回调接口
    protected XibaFullScreenEventCallback mFScreenEventCallback;    //全屏相关事件回调接口
    protected XibaTinyScreenEventCallback mTScreenEventCallback;    //小屏相关事件回调接口
    protected XibaPlayerActionEventCallback mActionEventCallback;    //动作相关事件回调接口

    private XibaResizeTextureView textureView;              //播放器显示Texture
    private XibaResizeImageView cacheImageView;

    private static Timer UPDATE_PROGRESS_TIMER;             //刷新播放进度的timer

    private ProgressTimerTask progressTimerTask;            //TimerTask

    private int mCurrentBufferPercentage;                   //当前缓冲百分比

    private Handler mHandler;                               //主线程handler

    private int mScreenWidth;   //屏幕宽度
    private int mScreenHeight;  //屏幕高度

    private float mDownX;       //触摸的X坐标
    private float mDownY;       //触摸的坐标

    private long mDownPosition;  //手指放下时的播放位置
    private long mSeekTimePosition; //滑动改变播放位置

    private int mDownVolumn; //手指放下时音量的大小
    private float mDownBrightness;  //手指放下时的亮度

    private static final int TOUCH_SLOP = 80;   //判定滑动的最小距离

    private int mTouchCurrentFeature = 0;      //touch事件当前的功能

    private static final int CHANGING_POSITION = 1;       //正在改变播放进度

    private static final int CHANGING_VOLUME = 2;         //正在改变音量

    private static final int CHANGING_BRIGHTNESS = 3;       //是否正在改变亮度

    //全屏模式下
//    private boolean mHasActionBar;  //显示或隐藏ActionBar
//    private boolean mHasStatusBar;  //显示或隐藏StatusBar


    private GestureDetector mGestureDetector;
    private XibaOnGestureListener mXibaOnGestureListener;

    private static boolean mIsScreenLocked = false;   //是否锁屏

    private ViewGroup mParent;      //播放器父容器
    private int mIndexInParent = 0;  //在父容器中的索引
    private ViewGroup.LayoutParams mLayoutParams;   //播放器布局参数
    private int mBackgroundColor = Color.TRANSPARENT; //播放器背景色

    private float mOriginX;      //播放器x坐标
    private float mOriginY;      //播放器y坐标

    private Bitmap mCacheBitmap;    //用于暂停时，切换屏幕状态用
    private boolean mHasTextureUpdated;

    private long mCurrentPosition;

    private OrientationUtils mOrientationUtils;

    private boolean mIsBuffering = false;   //是否正在加载
    private long mLastPosition = 0;
    private boolean mIsLoading = false;

    /**
     * 监听是否有外部其他多媒体开始播放
     */
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:

                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (XibaMediaManager.getInstance().getMediaPlayer().isPlaying()) {
                        XibaMediaManager.getInstance().getMediaPlayer().pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    public XibaVideoPlayer(Context context) {
        super(context);
        init();
    }

    public XibaVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        String background = attrs.getAttributeValue(NAMESPACE, "background");
        Log.e(TAG, "background = " + background);

        if (background != null) {
            mBackgroundColor = Color.parseColor(background);
        }

        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mHandler= new Handler();

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;

        mXibaOnGestureListener = new XibaOnGestureListener();

        mGestureDetector = new GestureDetector(getContext(), mXibaOnGestureListener);
        mOrientationUtils = new OrientationUtils((Activity) getContext(), this);
    }

    /**
     * 设置视频源
     *
     * @param url       视频地址
     * @param screen    屏幕类型
     * @return  true为设置成功，false为设置失败
     */
//    public boolean setUp(String url, int screen, Object... objects) {
    public boolean setUp(String url, int screen) {
        if (TextUtils.isEmpty(url) && TextUtils.equals(this.url, url)) {
            return false;
        }

        mCurrentState = STATE_NORMAL;

        this.url = url;
        this.mCurrentScreen = screen;
//        this.objects = objects;
        this.mCurrentPosition = 0;

        this.mSetUpScreen = screen;

        this.mCacheBitmap = null;
        if (cacheImageView != null) {
            cacheImageView.setImageBitmap(mCacheBitmap);
            cacheImageView.setVisibility(INVISIBLE);
        }

        this.mIsLoading = false;

        return true;
    }

    /**
     * 设置视频源
     *
     * @param url   播放地址
     * @param screen    屏幕类型
     * @param position  播放位置
     * @param cacheBitmap  起始播放时显示的图片，主要用于List中，从暂停状态重新播放时使用
     * @return    true为设置成功，false为设置失败
     */
    public boolean setUp(String url, int screen, long position, Bitmap cacheBitmap) {
        if (TextUtils.isEmpty(url) && TextUtils.equals(this.url, url)) {
            return false;
        }

        mCurrentState = STATE_NORMAL;

        this.url = url;
        this.mCurrentScreen = screen;
        this.mCurrentPosition = position;

        this.mSetUpScreen = screen;

        if (cacheBitmap != null) {
            this.mCacheBitmap = cacheBitmap;
//            cacheImageView.setImageBitmap(mCacheBitmap);
//            cacheImageView.setVisibility(VISIBLE);
        } else {
            this.mCacheBitmap = null;
//            cacheImageView.setImageBitmap(mCacheBitmap);
//            cacheImageView.setVisibility(INVISIBLE);
        }
        if (cacheImageView != null) {
            cacheImageView.setImageBitmap(mCacheBitmap);
            cacheImageView.setVisibility(INVISIBLE);
        }


        this.mIsLoading = false;

        return true;
    }

    /**
     * 设置播放器回调
     *
     * @param eventCallback 播放器事件回调接口实现类
     */
    public void setEventCallback(XibaVideoPlayerEventCallback eventCallback) {
        this.eventCallback = eventCallback;
    }

    /**
     * 设置全屏事件相关回调接口
     * @param fullScreenEventCallback 全屏事件回调接口实现类
     */
    public void setFullScreenEventCallback(XibaFullScreenEventCallback fullScreenEventCallback){
        this.mFScreenEventCallback = fullScreenEventCallback;
    }

    /**
     * 设置小屏事件相关回调接口
     * @param tinyScreenEventCallback 小屏事件回调接口实现类
     */
    public void setTinyScreenEventCallback(XibaTinyScreenEventCallback tinyScreenEventCallback){
        this.mTScreenEventCallback = tinyScreenEventCallback;
    }

    /**
     * 设置动作事件相关回调接口
     * @param playerActionEventCallback 动作事件回调接口实现类
     */
    public void setPlayerActionEventCallback(XibaPlayerActionEventCallback playerActionEventCallback){
        this.mActionEventCallback = playerActionEventCallback;
    }

    /**
     * 添加texture
     */
    private void addTexture() {
        removeTexture();

        textureView = new XibaResizeTextureView(getContext());

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        textureView.setSurfaceTextureListener(this);
        this.addView(textureView, lp);
        textureView.setVideoSize(XibaMediaManager.getInstance().getVideoSize());


        cacheImageView = new XibaResizeImageView(getContext());
        this.addView(cacheImageView, lp);
        cacheImageView.setVideoSize(XibaMediaManager.getInstance().getVideoSize());

        if (this.mCacheBitmap != null) {
            cacheImageView.setImageBitmap(mCacheBitmap);
            cacheImageView.setVisibility(VISIBLE);
        }
//        cacheImageView.setVisibility(INVISIBLE);
    }


    /**
     * 移出texture
     */
    private void removeTexture() {
        if (this.getChildCount() > 0) {
            this.removeAllViews();
        }
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDetachedFromWindow() {
        if (mCurrentScreen == SCREEN_NORMAL) {
            release();
        }
        super.onDetachedFromWindow();
    }


    //**********↓↓↓↓↓↓↓↓↓↓ --播放相关的方法 start-- ↓↓↓↓↓↓↓↓↓↓**********
    /**
     * 准备播放
     */
    public void prepareVideo() {

        if (XibaMediaManager.getInstance().getListener() != null) {
            XibaMediaManager.getInstance().getListener().onCompletion();
        }
        //设置播放器监听
        XibaMediaManager.getInstance().setListener(this);

        addTexture();

        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//        //屏幕常亮
//        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        //准备播放视频
        XibaMediaManager.getInstance().prepare(url, mapHeadData, mLooping);

        //启动刷新播放进度的timer
        startProgressTimer();

        //showLoading
        if (eventCallback != null && !mIsLoading) {
            mIsLoading = true;
            eventCallback.onStartLoading();
        }
    }

    /**
     * 播放按钮逻辑
     * 切换播放器的播放暂停状态
     * 发送事件给监听器
     *
     * @return true 正常播放；false 播放需要流量
     */
    public boolean togglePlayPause() {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        //如果当前是普通状态 或者 错误状态 -> 初始化播放视频
        if (mCurrentState == STATE_NORMAL || mCurrentState == STATE_ERROR) {

            //如果不是本地播放，同时网络状态又不是WIFI
            if (!url.startsWith("file") && !XibaUtil.isWifiConnected(getContext())) {
                return false;
            }
            //准备初始化播放
            prepareVideo();

        } else if (mCurrentState == STATE_PLAYING) {                    //如果当前是播放状态 -> 暂停播放
//            if (eventCallback != null) {
//                eventCallback.onPlayerPause();    //回调暂停方法
//            }
//            XibaMediaManager.getInstance().getMediaPlayer().pause();
//            setUiWithStateAndScreen(STATE_PAUSE);
            XibaMediaManager.getInstance().pausePlayer();
        } else if (mCurrentState == STATE_PAUSE) {                      //如果当前是暂停状态 -> 继续播放
//            if (eventCallback != null) {
//                eventCallback.onPlayerResume();    //回调继续播放方法
//            }
//            XibaMediaManager.getInstance().getMediaPlayer().start();
//            setUiWithStateAndScreen(STATE_PLAYING);
            XibaMediaManager.getInstance().startPlayer();
        } else if (mCurrentState == STATE_AUTO_COMPLETE
                || mCurrentState == STATE_COMPLETE) {              //如果当前是自动播放完成状态 -> 从头开始播放
            //准备初始化播放
            prepareVideo();
        }
        return true;
    }

    /**
     * 指定位置开始播放
     * @param progress  目标进度
     */
    public void seekTo(int progress){
        if (XibaMediaManager.getInstance().getMediaPlayer() != null) {
            long seekTime = progress * getDuration() / 100;

            if (XibaMediaManager.getInstance().getMediaPlayer().isPlaying()) {
                XibaMediaManager.getInstance().getMediaPlayer().seekTo(seekTime);
            } else if(mCurrentState == STATE_PAUSE || mCurrentState == STATE_COMPLETE) {
                if (eventCallback != null) {
                    eventCallback.onPlayerResume();    //回调继续播放方法
                }
                XibaMediaManager.getInstance().getMediaPlayer().seekTo(seekTime);
                XibaMediaManager.getInstance().getMediaPlayer().start();
                setUiWithStateAndScreen(STATE_PLAYING);
            }
        }
    }

    public void seekTo(long position){
        if (XibaMediaManager.getInstance().getMediaPlayer() != null) {
            if (XibaMediaManager.getInstance().getMediaPlayer().isPlaying()) {
                XibaMediaManager.getInstance().getMediaPlayer().seekTo(position);
            } else if(mCurrentState == STATE_PAUSE || mCurrentState == STATE_COMPLETE) {
                if (eventCallback != null) {
                    eventCallback.onPlayerResume();    //回调继续播放方法
                }
                XibaMediaManager.getInstance().getMediaPlayer().seekTo(position);
                XibaMediaManager.getInstance().getMediaPlayer().start();
                setUiWithStateAndScreen(STATE_PLAYING);
            }
        }
    }

    //根据播放器状态和屏幕状态设置UI
    public void setUiWithStateAndScreen(int state) {
        mCurrentState = state;
        switch (mCurrentState) {

//            case STATE_COMPLETE:
//                cancelProgressTimer();
//                //屏幕常亮
//                ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                break;
            case STATE_NORMAL:
            case STATE_ERROR:
                release();
                break;
            case STATE_PREPARING:
//                resetProgressAndTime();
                break;
            case STATE_PLAYING:
//            case STATE_PAUSE:
            case STATE_PLAYING_BUFFERING_START:
                startProgressTimer();
                //屏幕常亮
                ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
            case STATE_PAUSE:
            case STATE_COMPLETE:
            case STATE_AUTO_COMPLETE:
                cancelProgressTimer();
                //取消屏幕常亮
                ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        XibaMediaManager.getInstance().releaseMediaPlayer();
        cancelProgressTimer();
        removeTexture();
        //取消屏幕常亮
        ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mCacheBitmap != null) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
    }

    /**
     * 暂停
     */
    public void pausePlayer(){
        mLastState = mCurrentState;
        if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_PLAYING_BUFFERING_START || mCurrentState == STATE_PREPARING) {
            mCurrentState = STATE_PAUSE;
        }
        if (mCurrentScreen == SCREEN_LIST) {
            getCacheImageBitmap();
        }
        XibaMediaManager.getInstance().pausePlayer();
        mCurrentPosition = XibaMediaManager.getInstance().getMediaPlayer().getCurrentPosition();
    }

    /**
     * 恢复
     */
    public void resumePlayer(){

        if (mCurrentState == STATE_PAUSE && mLastState != STATE_PAUSE) {
            if (cacheImageView.getVisibility() != VISIBLE) {
                cacheImageView.setVisibility(VISIBLE);
            }

            if (XibaMediaManager.getInstance().getMediaPlayer().getDataSource() != null) {
                XibaMediaManager.getInstance().startPlayer();
            } else {
                prepareVideo();
            }
        }
        mLastState = -1;
    }


    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --处理触摸事件 start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果是小屏模式，不处理触摸事件
        if (mCurrentScreen == SCREEN_WINDOW_TINY) {
            return false;
        }

        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;

                mTouchCurrentFeature = 0;

                break;

            case MotionEvent.ACTION_MOVE:
                //如果是锁屏状态，直接返回
                if (mIsScreenLocked) {
                    return true;
                }

                //移动X，Y方向的距离
                float deltaX = x - mDownX;      //右移为正，左移为负
                float deltaY = y - mDownY;      //下移为正，上移为负
                //移动X，Y方向的绝对值
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);

                switch (mTouchCurrentFeature) {

                    //改变播放位置
                    case CHANGING_POSITION:
                        long totalTimeDuration = getDuration();

//                        mSeekTimePosition = (long) (mDownPosition + totalTimeDuration * deltaX / mScreenWidth);

                        mSeekTimePosition = (long) (mDownPosition + totalTimeDuration * deltaX / (mScreenWidth * mHorizontalSlopInfluenceValue));

                        if (mSeekTimePosition > totalTimeDuration) {
                            mSeekTimePosition = totalTimeDuration;
                        } else if (mSeekTimePosition < 0) {
                            mSeekTimePosition = 0;
                        }
//                        if (eventCallback != null) {
//                            eventCallback.onChangingPosition(mDownPosition, mSeekTimePosition, totalTimeDuration); //回调播放位置变化
//                        }

                        if (mActionEventCallback != null) {
                            mActionEventCallback.onChangingPosition(mDownPosition, mSeekTimePosition, totalTimeDuration); //回调播放位置变化
                        }

                        break;

                    //改变音量
                    case CHANGING_VOLUME:

                        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //系统音量最大值
                        int deltaV = (int) (max * 3 * (-deltaY) / mScreenHeight);   //变化量，增加的量与滑动方向相反

                        int changedVolume = mDownVolumn + deltaV;   //滑动后的音量

                        if (changedVolume > max) {
                            changedVolume = max;
                        } else if (changedVolume < 0) {
                            changedVolume = 0;
                        }

                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, changedVolume, 0); //设置音量

                        int volumePercent = changedVolume * 100 / max;  //当前音量的百分比
//                        if (eventCallback != null) {
//                            eventCallback.onChangingVolume(volumePercent);  //回调音量改变方法
//                        }
                        if (mActionEventCallback != null) {
                            mActionEventCallback.onChangingVolume(volumePercent);  //回调音量改变方法
                        }
                        break;

                    //改变亮度
                    case CHANGING_BRIGHTNESS:
                        float deltaB = -deltaY / mScreenHeight;
                        float changedBrightness = mDownBrightness + deltaB;
                        if (changedBrightness > 1.0f) {
                            changedBrightness = 1.0f;
                        } else if (changedBrightness < 0.01f) {
                            changedBrightness = 0.01f;
                        }

                        WindowManager.LayoutParams wLayoutParams = ((Activity)getContext()).getWindow().getAttributes();
                        wLayoutParams.screenBrightness = changedBrightness;

                        ((Activity)getContext()).getWindow().setAttributes(wLayoutParams);  //设置亮度，退出后亮度会自动恢复到系统亮度

                        int brightneesPercent = (int) (changedBrightness * 100);    //当前亮度百分比

//                        if (eventCallback != null) {
//                            eventCallback.onChangingBrightness(brightneesPercent);      //回调亮度改变方法
//                        }
                        if (mActionEventCallback != null) {
                            mActionEventCallback.onChangingBrightness(brightneesPercent);      //回调亮度改变方法
                        }
                        break;

                    //如果当前没有给mTouchCurrentFeature添加任何功能，将在这里进行判断
                    default:
                        if (absDeltaX >= TOUCH_SLOP) {      //进入水平滑动

                            firstHorizontalSlide();     //第一次进入水平滑动

                        } else if (absDeltaY > TOUCH_SLOP) {    //进入垂直滑动

                            firstVerticalSlide();   //第一次进入垂直滑动

                        }
                        break;
                }

                break;

            case MotionEvent.ACTION_UP:
                //如果是在锁屏状态，调用锁屏触摸回调
                if (mIsScreenLocked) {
//                    if (eventCallback != null) {
//                        eventCallback.onTouchLockedScreen();
//                    }
                    if (mActionEventCallback != null) {
                        mActionEventCallback.onTouchLockedScreen();
                    }
                    return true;
                }

                switch (mTouchCurrentFeature) {
                    case CHANGING_POSITION:
                        XibaMediaManager.getInstance().getMediaPlayer().seekTo(mSeekTimePosition);
//                        if (eventCallback != null) {
//                            eventCallback.onChangingPositionEnd();  //播放位置改变结束回调
//                        }
                        if (mActionEventCallback != null) {
                            mActionEventCallback.onChangingPositionEnd();  //播放位置改变结束回调
                        }
                        startProgressTimer();
                        break;
                    case CHANGING_VOLUME:
//                        if (eventCallback != null) {
//                            eventCallback.onChangingVolumeEnd();    //音量变化结束回调
//                        }
                        if (mActionEventCallback != null) {
                            mActionEventCallback.onChangingVolumeEnd();    //音量变化结束回调
                        }
                        break;
                    case CHANGING_BRIGHTNESS:
//                        if (eventCallback != null) {
//                            eventCallback.onChangingBrightnessEnd();    //亮度变化结束回调
//                        }
                        if (mActionEventCallback != null) {
                            mActionEventCallback.onChangingBrightnessEnd();    //亮度变化结束回调
                        }
                        break;
                }
                break;

        }

        mGestureDetector.onTouchEvent(event);

        return true;
    }

    /**
     * 第一次进入水平滑动，分配垂直滑动功能
     */
    private void firstHorizontalSlide(){

        int screenHorizontalFeature = 0;

        if (mCurrentScreen == SCREEN_NORMAL) {  //普通屏幕
            screenHorizontalFeature = mNormalScreenHorizontalFeature;
        } else if (mCurrentScreen == SCREEN_WINDOW_FULLSCREEN) {    //全屏模式
            screenHorizontalFeature = mFullScreenHorizontalFeature;
        }

        confirmHorizontalFeature(screenHorizontalFeature);
    }

    /**
     * 确认水平滑动的功能
     * @param screenHorizontalFeature
     */
    private void confirmHorizontalFeature(int screenHorizontalFeature){

        if (screenHorizontalFeature == SLIDING_HORIZONTAL_CHANGE_POSITION) {
            //改变播放进度
            mTouchCurrentFeature = CHANGING_POSITION;
            mDownPosition = getCurrentPositionWhenPlaying();//获取当前的播放位置

            cancelProgressTimer();
        }
    }

    /**
     * 第一次进入垂直滑动，分配垂直滑动功能
     */
    private void firstVerticalSlide(){
        boolean isLeft;
        int screenVerticalFeature = 0;

        if (mDownX < mScreenWidth * 0.5f) {     //进入左侧滑动
            isLeft = true;
        } else {         //进入右侧滑动
            isLeft = false;
        }

        if (mCurrentScreen == SCREEN_NORMAL) {  //普通屏幕
            screenVerticalFeature = mNormalScreenVerticalFeature;
        } else if (mCurrentScreen == SCREEN_WINDOW_FULLSCREEN) {    //全屏模式
            screenVerticalFeature = mFullScreenVerticalFeature;
        }

        confirmVerticalFeature(screenVerticalFeature, isLeft);

    }

    /**
     * 确认垂直滑动的功能
     * @param screenVerticalFeature 全屏 或 普通屏幕
     * @param isLeft 是否点击屏幕左侧
     */
    private void confirmVerticalFeature(int screenVerticalFeature, boolean isLeft){

        if (screenVerticalFeature == SLIDING_VERTICAL_ONLY_BRIGHTNESS                            //1.只改变亮度
                || (isLeft && (screenVerticalFeature == SLIDING_VERTICAL_LEFT_BRIGHTNESS))       //2.左侧改变亮度，同时点击左侧
                || (!isLeft && (screenVerticalFeature == SLIDING_VERTICAL_LEFT_VOLUME))) {       //3.左侧改变声音，同时点击右侧
            preChangeBrightness();
        } else if (screenVerticalFeature == SLIDING_VERTICAL_ONLY_VOLUME                         //1.只改变声音
                || (isLeft && (screenVerticalFeature == SLIDING_VERTICAL_LEFT_VOLUME))           //2.左侧改变声音，同时点击左侧
                || (!isLeft && (screenVerticalFeature == SLIDING_VERTICAL_LEFT_BRIGHTNESS))) {   //3.左侧改变亮度，同时点击右侧
            preChangeVolume();
        }
    }

    /**
     * 准备更改亮度 {@link #firstVerticalSlide}
     */
    private void preChangeBrightness(){
        mTouchCurrentFeature = CHANGING_BRIGHTNESS; //改变亮度

        //如果windowManager没有设置过屏幕亮度，默认得到的亮度是-1f
        mDownBrightness = ((Activity)getContext()).getWindow().getAttributes().screenBrightness;

        //如果是默认值 -1f 需要调用Setting获取屏幕亮度
        Log.e(TAG, "mDownBrightness=" + mDownBrightness);
        if (mDownBrightness <= 0.0f) {
            try {
                ContentResolver cr = getContext().getContentResolver();
                int brightnsee = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
                mDownBrightness = Float.valueOf(brightnsee) * (1f/255f);
            } catch (Settings.SettingNotFoundException e) {
                mDownBrightness = 0.1f;
            }
        }
    }

    /**
     * 准备更改声音 {@link #firstVerticalSlide}
     */
    private void preChangeVolume(){
        mTouchCurrentFeature = CHANGING_VOLUME;     //改变音量
        mDownVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 处理单击 双击事件
     */
    private class XibaOnGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            //如果是锁屏状态直接返回，
            if (mIsScreenLocked) {
                return false;
            }
//            if (eventCallback != null) {
//                eventCallback.onSingleTap();    //单击事件回调
//            }

            if (mActionEventCallback != null) {
                mActionEventCallback.onSingleTap();    //单击事件回调
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (mIsScreenLocked) {
                return false;
            }

            if (e.getAction() == MotionEvent.ACTION_UP) {
//                if (eventCallback != null) {
//                    eventCallback.onDoubleTap();    //双击事件回调
//                }
                if (mActionEventCallback != null) {
                    mActionEventCallback.onDoubleTap();    //双击事件回调
                }
                return true;
            }

            return false;
        }
    }

    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --处理触摸事件 end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑


    /**
     * 全屏播放
     * 使用全屏播放必须在XibaVideoPlayerEventCallback中
     * @param orientation 仅支持横屏ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
     *                    或镜像横屏ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE两个值
     */
    public void startFullScreen(int orientation){
//    public void startFullScreen(final boolean hasActionBar, final boolean hasStatusBar){
        //如果当前在全屏状态或小屏状态，直接返回
        if (mCurrentScreen == SCREEN_WINDOW_FULLSCREEN || mCurrentScreen == SCREEN_WINDOW_TINY) {
            return;
        }

        //设置屏幕状态
        mCurrentScreen = SCREEN_WINDOW_FULLSCREEN;

        getCacheImageBitmap();  //获取视频截图

        //隐藏ActionBar和StatusBar
        XibaUtil.hideSupportActionBar(getContext());

//        mHasActionBar = hasActionBar;
//        mHasStatusBar = hasStatusBar;

        ViewGroup fullScreenContainer = null;
        //进入全屏事件回调
//        if (eventCallback != null) {
//            fullScreenContainer = eventCallback.onEnterFullScreen();
//        }
        if (mFScreenEventCallback != null) {
            fullScreenContainer = mFScreenEventCallback.onEnterFullScreen();
        }

        if (fullScreenContainer == null) {
            throw new RuntimeException("eventCallback.onEnterFullScreen must return a ViewGroup for player");
        }

        mParent = ((ViewGroup)this.getParent());    //获取当前父容器
        mIndexInParent = mParent.indexOfChild(this);    //获取在父容器中的索引
        mLayoutParams = this.getLayoutParams();     //获取当前的布局参数
        ((ViewGroup)this.getParent()).removeView(this);     //将播放器从当前容器中移出

        this.setBackgroundColor(Color.BLACK);   //设置背景色

        //将全屏播放器放到全屏容器之中
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        fullScreenContainer.addView(XibaVideoPlayer.this, 0, layoutParams);

        //如果容器没有id，设置一个ID给容器
        fullScreenContainerID = fullScreenContainer.getId();
        if (fullScreenContainerID == NO_ID) {
            fullScreenContainerID = R.id.fullscreen_container_id;
            fullScreenContainer.setId(fullScreenContainerID);
        }

        //将全屏容器添加到contentView中
        ViewGroup contentView = getContentView();
        FrameLayout.LayoutParams contentViewLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        );
        contentView.addView(fullScreenContainer, contentViewLp);

        //旋转屏幕
//        OrientationUtils mOrientationUtils = new OrientationUtils((Activity) getContext());
//        mOrientationUtils.setOrientationLand();
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        mOrientationUtils.setOrientation(orientation);

        //改变texture尺寸
        if (textureView != null) {
            this.textureView.setVideoSize(XibaMediaManager.getInstance().getVideoSize());
        } else {
            this.addTexture();
        }
//        showCacheImageView();   //显示视频截图

    }

    /**
     * 恢复默认
     */
    public void quitFullScreen(){
        if (mCurrentScreen == SCREEN_NORMAL) {
            return;
        }

        //显示ActionBar和StatusBar
        XibaUtil.showSupportActionBar(getContext());

        getCacheImageBitmap();  //获取视频截图

        //获取contentView
        ViewGroup contentView = getContentView();

        //获取全屏容器
        ViewGroup fullScreenContainer = (ViewGroup) contentView.findViewById(fullScreenContainerID);

        if (fullScreenContainer != null) {
            fullScreenContainer.removeView(this);           //将播放器从全屏父容器中移出
            contentView.removeView(fullScreenContainer);    //将父容器从ContentView中移出
        }

        if (mParent != null) {
            mParent.addView(this, mIndexInParent, mLayoutParams);   //将播放器添加到原来的容器中
        }

        this.setBackgroundColor(mBackgroundColor);  //还原背景

        //旋转屏幕
//        OrientationUtils mOrientationUtils = new OrientationUtils((Activity) getContext());
//        mOrientationUtils.setOrientationPort();

        mOrientationUtils.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        if (eventCallback != null) {
//            eventCallback.onQuitFullScreen();   //调用退出全屏回调事件
//        }
        if (mFScreenEventCallback != null) {
            mFScreenEventCallback.onQuitFullScreen();   //调用退出全屏回调事件
        }

        //改变texture尺寸
        if (textureView != null) {
            this.textureView.setVideoSize(XibaMediaManager.getInstance().getVideoSize());
        } else {
            this.addTexture();
        }

//        showCacheImageView();   //显示视频截图

        //设置屏幕状态
//        mCurrentScreen = SCREEN_NORMAL;
        mCurrentScreen = mSetUpScreen;
    }



    /**
     * 小屏播放
     * @param size 小屏的尺寸
     * @param x 小屏左上角的x坐标
     * @param y 小屏左上角的y坐标
     * @param canMove 小屏幕是否可以移动 true为可移动
     */
    public void startTinyScreen(Point size, float x, float y, boolean canMove){
        if (mCurrentScreen == SCREEN_WINDOW_TINY || mCurrentScreen == SCREEN_WINDOW_FULLSCREEN) {
            return;
        }

        //设置屏幕状态
        mCurrentScreen = SCREEN_WINDOW_TINY;

        getCacheImageBitmap();  //获取视频截图

        mOriginX = this.getX();
        mOriginY = this.getY();

        mParent = ((ViewGroup)this.getParent());    //获取当前父容器
        mIndexInParent = mParent.indexOfChild(this);    //获取在父容器中的索引
        mLayoutParams = this.getLayoutParams();     //获取当前的布局参数
        mParent.removeView(this);     //将播放器从当前容器中移出
        mParent.setTag(TINY_SCREEN_CONTAINER_TAG);

        this.setBackgroundColor(Color.BLACK);   //设置背景色

        //创建一个空View在原来的位置，不然布局会发生变化
        mParent.addView(new View(getContext()), mIndexInParent, mLayoutParams);

        //将小屏添加到contentView中
        ViewGroup contentView = getContentView();
        FrameLayout.LayoutParams contentViewLp = new FrameLayout.LayoutParams(size.x, size.y);

        //获取contentView的宽高
        int contentWidth = contentView.getMeasuredWidth();
        int contentHeight = contentView.getMeasuredHeight();

        //如果小屏左上角的x坐标超出了屏幕，修正x
        if (x + size.x > contentWidth) {
            x = contentWidth - size.x;
        }

        //如果小屏左上角的y坐标超出了屏幕，修正y
        if (y + size.y > contentHeight) {
            y = contentHeight - size.y;
        }

        //设置小屏幕初始化的位置
        this.setX(x);
        this.setY(y);

        //将小屏幕添加到ContentView中
        contentView.addView(this, contentViewLp);

        //改变texture尺寸
        if (textureView != null) {
            this.textureView.setVideoSize(XibaMediaManager.getInstance().getVideoSize());
        } else {
            this.addTexture();
        }

        //如果设置小屏幕可以移动，添加触摸监听
        if (canMove) {
            this.setOnTouchListener(new TinyWindowOnTouchListener());
        }

//        if (eventCallback != null) {
//            eventCallback.onEnterTinyScreen();  //调用进入小屏回调事件
//        }

        if (mTScreenEventCallback != null) {
            mTScreenEventCallback.onEnterTinyScreen();  //调用进入小屏回调事件
        }

//        showCacheImageView();   //显示视频截图
    }


    public void quitTinyScreen(ViewGroup container){
        if (container != null) {
            mParent = container;
        }
        quitTinyScreen();
    }
    /**
     * 退出小屏
     */
    public void quitTinyScreen(){
        if (mCurrentScreen == SCREEN_NORMAL) {
            return;
        }

        getCacheImageBitmap();  //获取视频截图

        //获取contentView
        ViewGroup contentView = getContentView();

        //移除小屏播放器
        contentView.removeView(this);

        if (mParent != null) {
            if (mParent.getChildCount() > mIndexInParent) {
                mParent.removeViewAt(mIndexInParent);   //移出占位用的View
            }

            //只有在父容器设置了tag的时候在返回，目的是避免在list中的错位
            if (mParent.getTag() != null && mParent.getTag().equals(TINY_SCREEN_CONTAINER_TAG)) {
                mParent.addView(this, mIndexInParent, mLayoutParams);   //将播放器添加回原来的容器
            }

        }

        //还原位置
        this.setX(mOriginX);
        this.setY(mOriginY);
        //还原背景
        this.setBackgroundColor(mBackgroundColor);

        //改变texture尺寸
        if (textureView != null) {
            this.textureView.setVideoSize(XibaMediaManager.getInstance().getVideoSize());
        } else {
            this.addTexture();
        }

//        mCurrentScreen = SCREEN_NORMAL;     //设置屏幕类型
        mCurrentScreen = mSetUpScreen;

//        if (eventCallback != null) {
//            eventCallback.onQuitTinyScreen();   //调用退出小屏回调事件
//        }
        if (mTScreenEventCallback != null) {
            mTScreenEventCallback.onQuitTinyScreen();   //调用退出小屏回调事件
        }

//        showCacheImageView();   //显示视频截图
    }


    private ViewGroup getContentView() {
        return (ViewGroup) (XibaUtil.scanForActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
    }

    /**
     * 获取切换屏幕时的视频截图
     */
    private Bitmap getCacheImageBitmap(){
        if (textureView != null && mHasTextureUpdated) {
            mCacheBitmap = textureView.getBitmap();
            mHasTextureUpdated = false;
            return mCacheBitmap;
        }
        return null;
    }

    /**
     * 当暂停的时候，切换屏幕状态时，显示视频截图，来避免切换之后的黑屏
     */
    private void showCacheImageView(){
        if(mCurrentState == STATE_PAUSE){

            if (mCacheBitmap != null) {
                cacheImageView.setImageBitmap(mCacheBitmap);
            }

            cacheImageView.setVideoSize(XibaMediaManager.getInstance().getVideoSize());
            cacheImageView.setVisibility(VISIBLE);
        }
    }

    public Bitmap getCacheBitmap(){
        return mCacheBitmap;
    }

    /**
     * 获得当前状态
     * @return 返回当前播放器的状态
     */
    public int getCurrentState(){
        return mCurrentState;
    }

    /**
     * 获得当前屏幕状态
     * @return 返回当前屏幕的状态
     */
    public int getCurrentScreen(){
        return mCurrentScreen;
    }


    /**
     * 获取锁屏状态
     * @return  当前播放器是否锁屏，true为锁屏状态
     */
    public boolean isScreenLock() {
        return mIsScreenLocked;
    }

    /**
     * 设置锁屏状态
     * @param screenLock true 锁屏; false 解锁
     */
    public void setScreenLock(boolean screenLock) {
        mIsScreenLocked = screenLock;
    }

    public boolean onBackPress(){
        if (mCurrentScreen == SCREEN_WINDOW_FULLSCREEN) {
            quitFullScreen();
            return true;
        }
        if (mCurrentScreen == SCREEN_WINDOW_TINY) {
            quitTinyScreen();
            return true;
        }
        return false;
    }

    /**
     * 设置是否自动旋转屏幕
     * @param autoRotate true为自动旋转屏幕
     */
    public void setAutoRotate(boolean autoRotate){
        mOrientationUtils.setAutoRotate(autoRotate);
    }

    /**
     * 获取普通屏幕下，垂直滑动屏幕时的功能
     * @return 垂直屏幕相关功能，默认{@link #SLIDING_VERTICAL_NONE}
     */
    public int getNormalScreenVerticalFeature() {
        return mNormalScreenVerticalFeature;
    }

    /**
     * 设置普通屏幕下，垂直滑动屏幕时的功能
     * @param normalScreenVerticalFeature {@link #SLIDING_VERTICAL_NONE}
     */
    public void setNormalScreenVerticalFeature(int normalScreenVerticalFeature) {
        this.mNormalScreenVerticalFeature = normalScreenVerticalFeature;
    }

    /**
     * 获取全屏状态下，垂直滑动屏幕时的功能
     * @return 垂直屏幕相关功能，{@link #SLIDING_VERTICAL_NONE}
     */
    public int getFullScreenVerticalFeature() {
        return mFullScreenVerticalFeature;
    }

    /**
     * 设置全屏状态下，垂直滑动屏幕时的功能
     * @param fullScreenVerticalFeature {@link #SLIDING_VERTICAL_NONE}
     */
    public void setFullScreenVerticalFeature(int fullScreenVerticalFeature) {
        this.mFullScreenVerticalFeature = fullScreenVerticalFeature;
    }

    /**
     * 获取普通屏幕下，水平滑动屏幕时的功能
     * @return 水平滑动屏幕相关功能，{@link #SLIDING_HORIZONTAL_NONE}
     */
    public int getNormalScreenHorizontalFeature() {
        return mNormalScreenHorizontalFeature;
    }

    /**
     * 设置普通屏幕下，水平滑动屏幕时的功能
     * @param normalScreenHorizontalFeature {@link #SLIDING_HORIZONTAL_NONE}
     */
    public void setNormalScreenHorizontalFeature(int normalScreenHorizontalFeature) {
        this.mNormalScreenHorizontalFeature = normalScreenHorizontalFeature;
    }

    /**
     * 获取全屏状态下，水平滑动屏幕时的功能
     * @return 水平滑动屏幕相关功能，{@link #SLIDING_HORIZONTAL_NONE}
     */
    public int getFullScreenHorizontalFeature() {
        return mFullScreenHorizontalFeature;
    }

    /**
     * 设置全屏状态下，水平滑动屏幕时的功能
     * @param fullScreenHorizontalFeature {@link #SLIDING_HORIZONTAL_NONE}
     */
    public void setFullScreenHorizontalFeature(int fullScreenHorizontalFeature) {
        this.mFullScreenHorizontalFeature = fullScreenHorizontalFeature;
    }

    /**
     * 获取水平滑动影响值
     * @return 返回当前水平滑动影响值
     */
    public int getHorizontalSlopInfluenceValue() {
        return mHorizontalSlopInfluenceValue;
    }

    /**
     * 设置水平滑动的影响值
     * 例如参数为2，那么滑动整个屏幕改变整个播放器时长的一半
     * 参数需要大于0的整数，传入参数如果小于等于0，自动将参数修改为1
     * 推荐在onPlayerPrepare事件中，获取视频长度之后，根据需求修改此参数
     * @param horizontalSlopInfluenceValue 目标水平滑动的影响值
     */
    public void setHorizontalSlopInfluenceValue(int horizontalSlopInfluenceValue) {
        this.mHorizontalSlopInfluenceValue = horizontalSlopInfluenceValue <= 0 ? 1 : horizontalSlopInfluenceValue;
    }

    //**********↑↑↑↑↑↑↑↑↑↑ --播放相关的方法 end-- ↑↑↑↑↑↑↑↑↑↑**********


    //**********↓↓↓↓↓↓↓↓↓↓ --SurfaceTextureListener override methods start-- ↓↓↓↓↓↓↓↓↓↓**********
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        XibaMediaManager.getInstance().setDisplay(new Surface(surface));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        XibaMediaManager.getInstance().setDisplay(null);
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        Log.e(TAG, "onSurfaceTextureUpdated");
        if (cacheImageView.getVisibility() == VISIBLE) {
            cacheImageView.setVisibility(INVISIBLE);
        }

        if (!mHasTextureUpdated) {
            mHasTextureUpdated = true;
        }

    }
    //**********↑↑↑↑↑↑↑↑↑↑ --SurfaceTextureListener override methods end-- ↑↑↑↑↑↑↑↑↑↑**********


    //**********↓↓↓↓↓↓↓↓↓↓ --IMediaPlayer Listeners override methods start-- ↓↓↓↓↓↓↓↓↓↓**********
    @Override
    public void onVideoSizeChanged(int width, int height) {
        Log.d(TAG, "onVideoSizeChanged");
        //根据视频宽高比重置播放器大小
        textureView.setVideoSize(new Point(width, height));
        cacheImageView.setVideoSize(new Point(width, height));
    }

    @Override
    public void onPrepared() {

        Log.e(TAG, "onPrepared");

        if (eventCallback != null) {
            eventCallback.onPlayerPrepare();  //回调准备播放
        }
        setUiWithStateAndScreen(STATE_PLAYING);  //修改状态为正在播放

        if (mCurrentPosition > 0) {
            seekTo(mCurrentPosition);
        }

    }

    @Override
    public void onStart() {

        Log.e(TAG, "onStart");

        if (eventCallback != null) {
            eventCallback.onPlayerResume();    //回调继续播放方法
        }
        setUiWithStateAndScreen(STATE_PLAYING);

//        if (cacheImageView.getVisibility() == VISIBLE) {
//            cacheImageView.setVisibility(INVISIBLE);
//        }

    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");

        setUiWithStateAndScreen(STATE_PAUSE);

        getCacheImageBitmap();  //获取视频截图
        showCacheImageView();

        if (eventCallback != null) {
            eventCallback.onPlayerPause();    //回调暂停方法
        }
    }

    @Override
    public void onAutoCompletion() {

        Log.e(TAG, "onAutoCompletion");

        if (eventCallback != null) {
            eventCallback.onPlayerAutoComplete();
        }
        setUiWithStateAndScreen(STATE_AUTO_COMPLETE);
    }

    @Override
    public void onCompletion() {

        Log.e(TAG, "onCompletion");

        if (eventCallback != null) {
            eventCallback.onPlayerComplete();
        }
        setUiWithStateAndScreen(STATE_COMPLETE);
    }

    @Override
    public void onBufferingUpdate(int percent) {
        Log.d(TAG, "onBufferingUpdate : precent=" + percent);
        mCurrentBufferPercentage = percent;

        if (percent > 0 && mIsBuffering) {
            mIsBuffering = false;
        }
    }

    @Override
    public void onSeekComplete() {
        Log.d(TAG, "onSeekComplete");
    }

    @Override
    public void onError(int framework_err, int impl_err) {
        Log.d(TAG, "onError");
        if (eventCallback != null) {
            eventCallback.onPlayerError(framework_err, impl_err);
        }
        setUiWithStateAndScreen(STATE_ERROR);

        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            //不支持逐步播放

        } else {
            //未知错误
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        Log.d(TAG, "onInfo: what=" + what + " : extra=" + extra);
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.e(TAG, "MEDIA_INFO_BUFFERING_START:");
                mIsBuffering = true;
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
//                mCurrentBufferPercentage = 100;         //缓冲完成，设置缓冲百分比为100
                mIsBuffering = false;
                Log.e(TAG, "MEDIA_INFO_BUFFERING_END:");
                break;
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
                break;
            case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                break;
            case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                break;
            case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                break;
            case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                break;
            case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
//                mVideoRotationDegree = extra;
                Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + extra);
//                if (mRenderView != null)
//                    mRenderView.setVideoRotation(arg2);
                break;
            case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                break;
        }
    }
    //**********↑↑↑↑↑↑↑↑↑↑ --IMediaPlayer Listeners override methods end-- ↑↑↑↑↑↑↑↑↑↑**********


    //**********↓↓↓↓↓↓↓↓↓↓ --PROGRESS_TIMER methods start-- ↓↓↓↓↓↓↓↓↓↓**********
    /**
     * 启动计时器更新播放进度
     */
    private void startProgressTimer(){
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        progressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(progressTimerTask, 0, 200);
    }

    /**
     * 取消计时器
     */
    private void cancelProgressTimer(){
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (progressTimerTask != null) {
            progressTimerTask.cancel();
        }

        if (mTimerRunnable != null) {
            mHandler.removeCallbacks(mTimerRunnable);
        }

//        //取消屏幕常亮
//        ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 自定义计时器任务，回调播放进度更新事件
     */
    private class ProgressTimerTask extends TimerTask{

        @Override
        public void run() {
            if (mCurrentState == STATE_PLAYING
                    || mCurrentState == STATE_PAUSE
                    || mCurrentState == STATE_PLAYING_BUFFERING_START) {

                final long position = getCurrentPositionWhenPlaying();                   //当前播放位置
                final long duration = getDuration();                                     //总时长
                final int progress = (int) (position * 100 / (duration == 0 ? 1 : duration));   //播放进度

                Log.e(TAG, "ProgressTimerTask: run position=" + position);

                mTimerRunnable.setProgressInfo(progress, position, duration);
                //由于Timer会另开一条线程工作，因此不能操作UI，所以使用Handler让回调方法在主线程工作
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (eventCallback != null) {
//                            eventCallback.onPlayerProgressUpdate(progress, mCurrentBufferPercentage, position, duration);
//                        }
//                    }
//                });

                mHandler.post(mTimerRunnable);


            }
        }
    }

    private TimerRunnable mTimerRunnable = new TimerRunnable();

    private class TimerRunnable implements Runnable{

        private int progress;
        private long position;
        private long duration;

        public void setProgressInfo(int progress, long position, long duration){
            this.progress = progress;
            this.position = position;
            this.duration = duration;
        }
        @Override
        public void run() {
            if (eventCallback != null) {
//                Log.e(TAG, "mCurrentBufferPercentage=" + mCurrentBufferPercentage);
                if (mLastPosition == position && mIsBuffering) {

                    if (!mIsLoading) {
                        mIsLoading = true;
                        //showLoading
                        Log.e(TAG, "Player is Loading");

                        eventCallback.onStartLoading();
                    }

                } else {
                    if (mIsLoading) {
                        mIsLoading = false;
                    }
                    eventCallback.onPlayerProgressUpdate(progress, mCurrentBufferPercentage, position, duration);
                }
            }
            mLastPosition = position;
        }
    }

    /**
     * 获取当前播放位置
     * @return 当前播放位置
     */
    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSE) {
            try {
                position = XibaMediaManager.getInstance().getMediaPlayer().getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }
    /**
     * 获取视频时长
     * @return 视频总时长
     */
    public long getDuration(){
        long duration = 0;

        try {
            duration = XibaMediaManager.getInstance().getMediaPlayer().getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }
    //**********↑↑↑↑↑↑↑↑↑↑ --PROGRESS_TIMER methods end-- ↑↑↑↑↑↑↑↑↑↑**********

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --OnAutoOrientationChangedListener methods start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     * 只有设置setAutoRotate true 才会调用一下方法
     */
    @Override
    public void onPortrait() {
        if (mCurrentScreen == SCREEN_WINDOW_FULLSCREEN) {
            quitFullScreen();
        }
    }

    @Override
    public void onLandscape() {
        if (mCurrentScreen == SCREEN_NORMAL) {
            startFullScreen(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onReverseLandscape() {
        if (mCurrentScreen == SCREEN_NORMAL) {
            startFullScreen(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --OnAutoOrientationChangedListener methods end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
}
