package com.axiba.xibavideoplayer.sample;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.HashMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by xiba on 2016/11/26.
 */

public class IjkDemoActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener,
        IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener{

    public static final String TAG = IjkDemoActivity.class.getSimpleName();

    private TextureView texture;

    private Button start;

    public Surface surface;

    public IjkMediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijk_demo);
        init();
    }

    private void init(){

//        AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
//                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        texture = (TextureView) findViewById(R.id.texture);
        start = (Button) findViewById(R.id.start);
        texture.setSurfaceTextureListener(this);


        mediaPlayer = new IjkMediaPlayer();

        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);


        String url = "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11086&editionType=default";
        try {
            mediaPlayer.setDataSource(url, new HashMap<String, String>());
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            surface.release();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface = new Surface(surface);
        mediaPlayer.setSurface(this.surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        Log.e(TAG, "onPrepared");
        iMediaPlayer.start();
        Log.e(TAG, "iMediaPlayer.getVideoWidth()=" + iMediaPlayer.getVideoWidth());
        Log.e(TAG, "iMediaPlayer.getVideoHeight()=" + iMediaPlayer.getVideoHeight());
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int sar_num, int sar_den){
        Log.e(TAG, "onVideoSizeChanged");
        Log.e(TAG, "width=" + width);
        Log.e(TAG, "height=" + height);
        Log.e(TAG, "sar_num=" + sar_num);
        Log.e(TAG, "sar_den=" + sar_den);
        Log.e(TAG, "iMediaPlayer.getVideoWidth()=" + iMediaPlayer.getVideoWidth());
        Log.e(TAG, "iMediaPlayer.getVideoHeight()=" + iMediaPlayer.getVideoHeight());
    }

    public AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

        }
    };
}
