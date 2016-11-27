package com.axiba.xibavideoplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.axiba.xibavideoplayer.listener.XibaMediaListener;
import com.axiba.xibavideoplayer.utils.XibaUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiba on 2016/11/26.
 */
public class XibaVideoPlayer extends FrameLayout implements TextureView.SurfaceTextureListener, XibaMediaListener {

    public static final String TAG = XibaVideoPlayer.class.getSimpleName();

    public static final int STATE_NORMAL = 0;                   //正常
    public static final int STATE_PREPAREING = 1;               //准备中
    public static final int STATE_PLAYING = 2;                  //播放中
    public static final int STATE_PLAYING_BUFFERING_START = 3;  //开始缓冲
    public static final int STATE_PAUSE = 5;                    //暂停
    public static final int STATE_AUTO_COMPLETE = 6;            //自动播放结束
    public static final int STATE_ERROR = 7;                    //错误状态

    protected int mCurrentState = -1; //当前的播放状态

    public int currentScreen = -1;
    protected String url;
    protected Map<String, String> mapHeadData = new HashMap<>();
    protected Object[] objects = null;
    protected boolean mLooping = false;
    protected AudioManager mAudioManager;   //音频焦点的监听
    private XibaResizeTextureView textureView;

    protected XibaVideoPlayerEventCallback eventCallback;
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
     * @param eventCallback
     */
    public void setEventCallback(XibaVideoPlayerEventCallback eventCallback){
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


    }

    /**
     * 播放按钮逻辑
     * 切换播放器的播放暂停状态
     * 发送事件给监听器
     * @return true 操作成功；false 操作失败
     */
    public boolean togglePlayPause(){
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        //如果当前是普通状态 或者 错误状态 -> 初始化播放视频
        if (mCurrentState == STATE_NORMAL || mCurrentState == STATE_ERROR) {


            //如果不是本地播放，同时网络状态又不是WIFI
            if (!url.startsWith("file") && !XibaUtil.isWifiConnected(getContext())) {
                return false;
            }

            prepareVideo();

        } else if (mCurrentState == STATE_PLAYING) {                    //如果当前是播放状态 -> 暂停播放
            if (eventCallback != null) {
                eventCallback.onPause();    //回调暂停方法
            }
            XibaMediaManager.getInstance().getMediaPlayer().pause();
            mCurrentState = STATE_PAUSE;
        } else if (mCurrentState == STATE_PAUSE) {                      //如果当前是暂停状态 -> 继续播放
            if (eventCallback != null) {
                eventCallback.onResume();    //回调继续播放方法
            }
            XibaMediaManager.getInstance().getMediaPlayer().start();
            mCurrentState = STATE_PLAYING;
        } else if (mCurrentState == STATE_AUTO_COMPLETE) {              //如果当前是自动播放完成状态 -> 从头开始播放

            prepareVideo();
        }
        return true;
    }

    /**
     * 释放资源
     */
    public void release() {
        XibaMediaManager.getInstance().releaseMediaPlayer();
        removeTexture();
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
        //根据视频宽高比重置播放器大小
        textureView.setVideoSize(new Point(width, height));
    }

    @Override
    public void onPrepared() {
        if (eventCallback != null) {
            eventCallback.onPrepare();  //回调准备播放
        }
        mCurrentState = STATE_PLAYING;  //修改状态为正在播放
    }

    @Override
    public void onAutoCompletion() {
        if (eventCallback != null) {
            eventCallback.onAutoComplete();
        }
        mCurrentState = STATE_AUTO_COMPLETE;
    }

    @Override
    public void onCompletion() {
        if (eventCallback != null) {
            eventCallback.onComplete();
        }
        mCurrentState = STATE_NORMAL;
    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {
        if (eventCallback != null) {
            eventCallback.onError(what, extra);
        }
        mCurrentState = STATE_ERROR;
    }

    @Override
    public void onInfo(int what, int extra) {

    }
    //**********↑↑↑↑↑↑↑↑↑↑ --IMediaPlayer Listeners override methods end-- ↑↑↑↑↑↑↑↑↑↑**********
}
