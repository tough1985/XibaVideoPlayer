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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiba on 2016/11/26.
 */
public class XibaVideoPlayer extends FrameLayout implements TextureView.SurfaceTextureListener, XibaMediaListener {

    public static final String TAG = XibaVideoPlayer.class.getSimpleName();

    public int currentScreen = -1;


    private XibaResizeTextureView textureView;
    protected String url;
    protected Map<String, String> mapHeadData = new HashMap<>();
    protected Object[] objects = null;

    protected boolean mLooping = false;

    protected AudioManager mAudioManager;   //音频焦点的监听
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
                    if (XibaMediaManager.getInstence().getMediaPlayer().isPlaying()) {
                        XibaMediaManager.getInstence().getMediaPlayer().pause();
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
     * @param url
     * @param screen
     * @param objects
     * @return
     */
    public boolean setUp(String url, int screen, Object... objects) {
        if (TextUtils.isEmpty(url) && TextUtils.equals(this.url, url)) {
            return false;
        }
        this.url = url;
        this.currentScreen = screen;
        this.objects = objects;
        return true;
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

        if (XibaMediaManager.getInstence().getListener() != null) {

            XibaMediaManager.getInstence().getListener().onCompletion();
        }
        //设置播放器监听
        XibaMediaManager.getInstence().setListener(this);

        addTexture();

        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        //屏幕常亮
        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //准备播放视频
        XibaMediaManager.getInstence().prepare(url, mapHeadData, mLooping);
    }

    /**
     * 释放资源
     */
    public void release() {
        XibaMediaManager.getInstence().releaseMediaPlayer();
        removeTexture();
    }
    //**********↑↑↑↑↑↑↑↑↑↑ --播放相关的方法 end-- ↑↑↑↑↑↑↑↑↑↑**********


    //**********↓↓↓↓↓↓↓↓↓↓ --SurfaceTextureListener override methods start-- ↓↓↓↓↓↓↓↓↓↓**********
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        XibaMediaManager.getInstence().setDisplay(new Surface(surface));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        XibaMediaManager.getInstence().setDisplay(null);
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

    }

    @Override
    public void onAutoCompletion() {

    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {

    }

    @Override
    public void onInfo(int what, int extra) {

    }
    //**********↑↑↑↑↑↑↑↑↑↑ --IMediaPlayer Listeners override methods end-- ↑↑↑↑↑↑↑↑↑↑**********
}
