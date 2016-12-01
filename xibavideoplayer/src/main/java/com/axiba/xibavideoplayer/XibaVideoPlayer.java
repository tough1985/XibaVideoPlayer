package com.axiba.xibavideoplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Handler;
import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.axiba.xibavideoplayer.listener.XibaMediaListener;
import com.axiba.xibavideoplayer.utils.XibaUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by xiba on 2016/11/26.
 */
public class XibaVideoPlayer extends FrameLayout implements TextureView.SurfaceTextureListener, XibaMediaListener {

    public static final String TAG = XibaVideoPlayer.class.getSimpleName();

    public static final int STATE_NORMAL = 0;                   //正常
    public static final int STATE_PREPARING = 1;               //准备中
    public static final int STATE_PLAYING = 2;                  //播放中
    public static final int STATE_PLAYING_BUFFERING_START = 3;  //开始缓冲
    public static final int STATE_COMPLETE = 4;                    //
    public static final int STATE_PAUSE = 5;                    //暂停
    public static final int STATE_AUTO_COMPLETE = 6;            //自动播放结束
    public static final int STATE_ERROR = 7;                    //错误状态
    public int currentScreen = -1;                          //当前屏幕状态
    protected int mCurrentState = -1;                       //当前的播放状态

    protected String url;                                   //播放地址

    protected Map<String, String> mapHeadData = new HashMap<>();
    protected Object[] objects = null;
    protected boolean mLooping = false;

    protected AudioManager mAudioManager;                   //音频焦点的监听

    protected XibaVideoPlayerEventCallback eventCallback;   //播放器事件回调接口

    private XibaResizeTextureView textureView;              //播放器显示Texture

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
    }

    /**
     * 设置视频源
     *
     * @param url
     * @param screen
     * @param objects
     * @return
     */
    public boolean setUp(String url, int screen, Object... objects) {
        if (TextUtils.isEmpty(url) && TextUtils.equals(this.url, url)) {
            return false;
        }

        mCurrentState = STATE_NORMAL;

        this.url = url;
        this.currentScreen = screen;
        this.objects = objects;
        return true;
    }

    /**
     * 设置播放器回调
     *
     * @param eventCallback
     */
    public void setEventCallback(XibaVideoPlayerEventCallback eventCallback) {
        this.eventCallback = eventCallback;
    }

    /**
     * 添加texture
     */
    private void addTexture() {
        if (this.getChildCount() > 0) {
            this.removeAllViews();
        }

        textureView = new XibaResizeTextureView(getContext());

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        textureView.setSurfaceTextureListener(this);
        this.addView(textureView, lp);
    }

    /**
     * 移出texture
     */
    private void removeTexture() {
        if (this.getChildCount() > 0) {
            this.removeAllViews();
        }
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
        //屏幕常亮
        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //准备播放视频
        XibaMediaManager.getInstance().prepare(url, mapHeadData, mLooping);

        //启动刷新播放进度的timer
        startProgressTimer();
    }

    /**
     * 播放按钮逻辑
     * 切换播放器的播放暂停状态
     * 发送事件给监听器
     *
     * @return true 操作成功；false 操作失败
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
            if (eventCallback != null) {
                eventCallback.onPlayerPause();    //回调暂停方法
            }
            XibaMediaManager.getInstance().getMediaPlayer().pause();
            setUiWithStateAndScreen(STATE_PAUSE);
        } else if (mCurrentState == STATE_PAUSE) {                      //如果当前是暂停状态 -> 继续播放
            if (eventCallback != null) {
                eventCallback.onPlayerResume();    //回调继续播放方法
            }
            XibaMediaManager.getInstance().getMediaPlayer().start();
            setUiWithStateAndScreen(STATE_PLAYING);
        } else if (mCurrentState == STATE_AUTO_COMPLETE) {              //如果当前是自动播放完成状态 -> 从头开始播放
            //准备初始化播放
            prepareVideo();
        }
        return true;
    }

    /**
     * 指定位置开始播放
     * @param progress
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

    //根据播放器状态和屏幕状态设置UI
    public void setUiWithStateAndScreen(int state) {
        mCurrentState = state;
        switch (mCurrentState) {

            case STATE_COMPLETE:
                cancelProgressTimer();
                break;
            case STATE_NORMAL:
            case STATE_ERROR:
                release();
                break;
            case STATE_PREPARING:
//                resetProgressAndTime();
                break;
            case STATE_PLAYING:
            case STATE_PAUSE:
            case STATE_PLAYING_BUFFERING_START:
                startProgressTimer();
                break;
            case STATE_AUTO_COMPLETE:
                cancelProgressTimer();
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;

                mTouchCurrentFeature = 0;

                break;

            case MotionEvent.ACTION_MOVE:
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
                        mSeekTimePosition = (long) (mDownPosition + totalTimeDuration * deltaX / mScreenWidth);
                        if (mSeekTimePosition > totalTimeDuration) {
                            mSeekTimePosition = totalTimeDuration;
                        } else if (mSeekTimePosition < 0) {
                            mSeekTimePosition = 0;
                        }

                        eventCallback.onChangingPosition(mDownPosition, mSeekTimePosition, totalTimeDuration); //回调播放位置变化
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

                        eventCallback.onChangingVolume(volumePercent);  //回调音量改变方法
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
                        eventCallback.onChangingBrightness(brightneesPercent);      //回调亮度改变方法

                        break;

                    //如果当前没有给mTouchCurrentFeature添加任何功能，将在这里进行判断
                    default:
                        if (absDeltaX >= TOUCH_SLOP) {      //改变播放进度
                            mTouchCurrentFeature = CHANGING_POSITION;
                            mDownPosition = getCurrentPositionWhenPlaying();//获取当前的播放位置

                            cancelProgressTimer();
                        } else if (absDeltaY > TOUCH_SLOP) {
                            if (mDownX < mScreenWidth * 0.5f) {
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


                            } else {
                                mTouchCurrentFeature = CHANGING_VOLUME;     //改变音量
                                mDownVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            }
                        }
                        break;
                }

                break;

            case MotionEvent.ACTION_UP:
//                if (mTouchCurrentFeature == CHANGING_POSITION) {
//                    XibaMediaManager.getInstance().getMediaPlayer().seekTo(mSeekTimePosition);
//                    eventCallback.onChangingPositionEnd();
//                    startProgressTimer();
//                }

                switch (mTouchCurrentFeature) {
                    case CHANGING_POSITION:
                        XibaMediaManager.getInstance().getMediaPlayer().seekTo(mSeekTimePosition);
                        eventCallback.onChangingPositionEnd();
                        startProgressTimer();
                        break;
                    case CHANGING_VOLUME:
                        eventCallback.onChangingVolumeEnd();
                        break;
                    case CHANGING_BRIGHTNESS:
                        eventCallback.onChangingBrightnessEnd();
                        break;
                }
                break;

        }
        return true;
    }

    //**********↑↑↑↑↑↑↑↑↑↑ --播放相关的方法 end-- ↑↑↑↑↑↑↑↑↑↑**********


    //**********↓↓↓↓↓↓↓↓↓↓ --SurfaceTextureListener override methods start-- ↓↓↓↓↓↓↓↓↓↓**********
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        XibaMediaManager.getInstance().setDisplay(new Surface(surface));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        XibaMediaManager.getInstance().setDisplay(null);
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
    //**********↑↑↑↑↑↑↑↑↑↑ --SurfaceTextureListener override methods end-- ↑↑↑↑↑↑↑↑↑↑**********


    //**********↓↓↓↓↓↓↓↓↓↓ --IMediaPlayer Listeners override methods start-- ↓↓↓↓↓↓↓↓↓↓**********
    @Override
    public void onVideoSizeChanged(int width, int height) {
        Log.d(TAG, "onVideoSizeChanged");
        //根据视频宽高比重置播放器大小
        textureView.setVideoSize(new Point(width, height));
    }

    @Override
    public void onPrepared() {

        Log.e(TAG, "onPrepared");

        if (eventCallback != null) {
            eventCallback.onPlayerPrepare();  //回调准备播放
        }
        setUiWithStateAndScreen(STATE_PLAYING);  //修改状态为正在播放
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
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.e(TAG, "MEDIA_INFO_BUFFERING_END:");
                mCurrentBufferPercentage = 100;         //缓冲完成，设置缓冲百分比为100
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

                //由于Timer会另开一条线程工作，因此不能操作UI，所以使用Handler让回调方法在主线程工作
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        eventCallback.onPlayerProgressUpdate(progress, mCurrentBufferPercentage, position, duration);
                    }
                });
            }
        }
    }

    /**
     * 获取当前播放位置
     * @return
     */
    private long getCurrentPositionWhenPlaying() {
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
     * @return
     */
    private long getDuration(){
        long duration = 0;

        try {
            duration = (int) XibaMediaManager.getInstance().getMediaPlayer().getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }
    //**********↑↑↑↑↑↑↑↑↑↑ --PROGRESS_TIMER methods end-- ↑↑↑↑↑↑↑↑↑↑**********
}
