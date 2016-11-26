package com.axiba.xibavideoplayer;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by xiba on 2016/11/26.
 */
public class XibaVideoPlayer extends FrameLayout implements TextureView.SurfaceTextureListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnVideoSizeChangedListener{

    public static final String TAG = XibaVideoPlayer.class.getSimpleName();

    private IjkMediaPlayer mediaPlayer;

    private XibaResizeTextureView textureView;

    private String url;
    public int currentScreen = -1;
    public Object[] objects = null;
    public Map<String, String> mapHeadData = new HashMap<>();

    public XibaVideoPlayer(Context context) {
        super(context);
        init();
    }

    public XibaVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //初始化
    private void init(){
        mediaPlayer = new IjkMediaPlayer();
    }

    //设置视频源
    public boolean setUp(String url, int screen, Object... objects){
        if (TextUtils.isEmpty(url) && TextUtils.equals(this.url, url)) {
            return false;
        }
        this.url = url;
        this.currentScreen = screen;
        this.objects = objects;
        return true;
    }

    //添加texture
    private void addTexture(){
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

    //移出texture
    private void removeTexture(){
        if (this.getChildCount() > 0) {
            this.removeAllViews();
        }
    }

    //准备播放
    public void prepareVideo(){
        addTexture();

        mediaPlayer.release();
        mediaPlayer = new IjkMediaPlayer();

        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setOnPreparedListener(this);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);

        try {
            mediaPlayer.setDataSource(url, mapHeadData);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //释放资源
    public void release(){
        mediaPlayer.stop();
        mediaPlayer.release();
        removeTexture();
    }


    //******************** --SurfaceTextureListener override methods start-- ********************
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mediaPlayer.setSurface(new Surface(surface));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
    //******************** --SurfaceTextureListener override methods end-- ********************


    //******************** --IMediaPlayer Listeners override methods start-- ********************
    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int sar_num, int sar_den){
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "onVideoSizeChanged");
        }

        textureView.setVideoSize(new Point(width, height));
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "onPrepared");
        }
    }
    //******************** --IMediaPlayer Listeners override methods end-- ********************
}
