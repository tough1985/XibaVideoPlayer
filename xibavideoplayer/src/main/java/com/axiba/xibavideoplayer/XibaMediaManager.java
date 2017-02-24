package com.axiba.xibavideoplayer;

import android.graphics.Point;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.axiba.xibavideoplayer.bean.VideoSource;
import com.axiba.xibavideoplayer.listener.XibaMediaListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by xiba on 2016/11/26.
 */

public class XibaMediaManager implements IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnSeekCompleteListener,
        IMediaPlayer.OnErrorListener, IMediaPlayer.OnBufferingUpdateListener {

    public static final String TAG = XibaMediaManager.class.getSimpleName();

    //MediaHandler的消息
    public static final int MESSAGE_PREPARE = 0;        //准备播放
    public static final int MESSAGE_SET_DISPLAY = 1;    //设置Texture
    public static final int MESSAGE_RELEASE = 2;        //释放资源
    public static final int MESSAGE_START = 3;
    public static final int MESSAGE_PAUSE = 4;
    //单例
    private static XibaMediaManager instance;
    public int currentVideoWidth = 0;
    public int currentVideoHeigth = 0;
    private IjkMediaPlayer mediaPlayer;         //Ijk播放器
    private HandlerThread mMediaHandlerThread;  //创建播放器线程
    private MediaHandler mMediaHandler;         //播放器线程的Handler，用来操作播放器
    private Handler mainThreadHandler;          //主线程的Handler，用来刷新UI
    private WeakReference<XibaMediaListener> listener;
    private WeakReference<XibaMediaListener> lastListener;

    private XibaMediaManager() {
        mediaPlayer = new IjkMediaPlayer();
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
        mainThreadHandler = new Handler();
    }

    public static synchronized XibaMediaManager getInstance() {
        if (instance == null) {
            instance = new XibaMediaManager();
        }
        return instance;
    }

    //**********↓↓↓↓↓↓↓↓↓↓ --MediaHandler.class start-- ↓↓↓↓↓↓↓↓↓↓**********

    //**********↓↓↓↓↓↓↓↓↓↓ --IMediaPlayer Listeners override methods start-- ↓↓↓↓↓↓↓↓↓↓**********
    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, final int i) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    getListener().onBufferingUpdate(i);
                }
            }
        });
    }
    //**********↑↑↑↑↑↑↑↑↑↑ --MediaHandler.class end-- ↑↑↑↑↑↑↑↑↑↑**********

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    getListener().onCompletion();
                }
            }
        });
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    getListener().onError(what, extra);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    getListener().onInfo(what, extra);
                }
            }
        });
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    getListener().onPrepared();
                }
            }
        });
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    getListener().onSeekComplete();
                }
            }
        });
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, final int width, final int height, int sar_num, int sar_den) {
        currentVideoWidth = width;
        currentVideoHeigth = height;
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    getListener().onVideoSizeChanged(width, height);
                }
            }
        });
    }

    //**********↓↓↓↓↓↓↓↓↓↓ --getter and setter methods start-- ↓↓↓↓↓↓↓↓↓↓**********
    public XibaMediaListener getListener() {
        if (listener == null) {
            return null;
        }
        return listener.get();
    }
    //**********↑↑↑↑↑↑↑↑↑↑ --IMediaPlayer Listeners override methods end-- ↑↑↑↑↑↑↑↑↑↑**********

    public void setListener(XibaMediaListener listener) {
        if (listener == null) {
            this.listener = null;
        } else {
            this.listener = new WeakReference<XibaMediaListener>(listener);
        }
    }

    public XibaMediaListener getLastListener() {
        if (lastListener == null) {
            return null;
        }
        return lastListener.get();
    }

    public void setLastListener(XibaMediaListener lastListener) {
        if (lastListener == null) {
            this.lastListener = null;
        } else {
            this.lastListener = new WeakReference<XibaMediaListener>(lastListener);
        }
    }

    public IjkMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * 通知Media线程，准备播放器
     *
     * @param url   播放地址
     * @param mapHeadData   设置的参数
     * @param loop  是否循环播放
     */
    public void prepare(String url, final Map<String, String> mapHeadData, boolean loop) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        mMediaHandler.obtainMessage(MESSAGE_PREPARE, new VideoSource(url, mapHeadData, loop)).sendToTarget();
    }
    //**********↑↑↑↑↑↑↑↑↑↑ --getter and setter methods end-- ↑↑↑↑↑↑↑↑↑↑**********


    //**********↓↓↓↓↓↓↓↓↓↓ --media methods start-- ↓↓↓↓↓↓↓↓↓↓**********

    /**
     * 通知Media线程，释放播放器资源
     */
    public void releaseMediaPlayer() {
        mMediaHandler.obtainMessage(MESSAGE_RELEASE).sendToTarget();
    }

    /**
     * 通知Media线程，给播放器设置surface
     *
     * @param surface 播放器绘制需要的surface
     */
    public void setDisplay(Surface surface) {
        mMediaHandler.obtainMessage(MESSAGE_SET_DISPLAY, surface).sendToTarget();
    }

    /**
     * 获取视频尺寸
     * @return 视频的宽和高
     */
    public Point getVideoSize() {
        if (currentVideoWidth != 0 && currentVideoHeigth != 0) {
            return new Point(currentVideoWidth, currentVideoHeigth);
        } else {
            return null;
        }
    }

    /**
     * 开始播放
     */
    public void startPlayer() {
        mMediaHandler.obtainMessage(MESSAGE_START).sendToTarget();
    }

    /**
     * 暂停播放
     */
    public void pausePlayer() {
        mMediaHandler.obtainMessage(MESSAGE_PAUSE).sendToTarget();
    }

    /**
     * MediaHandler在media线程工作，用于播放器相关的操作，与主线程分离
     */
    private class MediaHandler extends Handler {

        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //准备播放器
                case MESSAGE_PREPARE:
                    Log.d(TAG, "MediaHandler: MESSAGE_PREPARE");
                    try {

                        currentVideoWidth = 0;
                        currentVideoHeigth = 0;
                        mediaPlayer.release();
                        mediaPlayer = new IjkMediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                        VideoSource source = (VideoSource) msg.obj;
                        mediaPlayer.setDataSource(source.getUrl(), source.getMapHeadData());
                        mediaPlayer.setLooping(source.isLooping());

                        //添加监听
                        mediaPlayer.setOnPreparedListener(XibaMediaManager.this);
                        mediaPlayer.setOnVideoSizeChangedListener(XibaMediaManager.this);
                        mediaPlayer.setOnInfoListener(XibaMediaManager.this);
                        mediaPlayer.setOnCompletionListener(XibaMediaManager.this);
                        mediaPlayer.setOnSeekCompleteListener(XibaMediaManager.this);
                        mediaPlayer.setOnErrorListener(XibaMediaManager.this);
                        mediaPlayer.setOnBufferingUpdateListener(XibaMediaManager.this);

                        mediaPlayer.setScreenOnWhilePlaying(true);
                        mediaPlayer.prepareAsync();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                //设置Texture
                case MESSAGE_SET_DISPLAY:
                    Log.d(TAG, "MediaHandler: MESSAGE_SET_DISPLAY");
                    if (msg.obj == null) {
                        mediaPlayer.setDisplay(null);
                    } else {
                        Surface holder = (Surface) msg.obj;
                        if (holder.isValid()) {
                            mediaPlayer.setSurface(holder);
                        }
                    }
                    break;
                //释放
                case MESSAGE_RELEASE:
                    Log.d(TAG, "MediaHandler: MESSAGE_RELEASE");
                    mediaPlayer.release();
                    break;
                //开始
                case MESSAGE_START:
                    Log.d(TAG, "MediaHandler: MESSAGE_START");
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    getListener().onStart();
                                }
                            }
                        });

                    }
                    break;
                //暂停
                case MESSAGE_PAUSE:
                    Log.d(TAG, "MediaHandler: MESSAGE_PAUSE");
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        synchronized (listener) {
                            mediaPlayer.pause();
//                        mainThreadHandler.post(new Runnable() {
                            mainThreadHandler.postAtFrontOfQueue(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        getListener().onPause();
                                    }
                                }
                            });
                        }
                    }

                    break;
            }
        }
    }
    //**********↑↑↑↑↑↑↑↑↑↑ --media methods end-- ↑↑↑↑↑↑↑↑↑↑**********
}
