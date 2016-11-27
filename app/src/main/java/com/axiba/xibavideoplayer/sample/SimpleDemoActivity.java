package com.axiba.xibavideoplayer.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2016/11/26.
 */
public class SimpleDemoActivity extends AppCompatActivity implements XibaVideoPlayerEventCallback{

    public static final String TAG = SimpleDemoActivity.class.getSimpleName();

    private XibaVideoPlayer xibaVP;
    private Button play;
    private TextView currentTimeTV;
    private TextView totalTimeTV;

    String url = "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11086&editionType=default";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_demo);

        xibaVP = (XibaVideoPlayer) findViewById(R.id.demo_xibaVP);
        play = (Button) findViewById(R.id.demo_play);
        currentTimeTV = (TextView) findViewById(R.id.current_time);
        totalTimeTV = (TextView) findViewById(R.id.total_time);

        xibaVP.setUp(url, 0, new Object[]{});
        xibaVP.setEventCallback(this);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                xibaVP.prepareVideo();
                xibaVP.togglePlayPause();
            }
        });
    }

    @Override
    protected void onStop() {
        xibaVP.release();
        super.onStop();
    }

    /**
     * **********↓↓↓↓↓↓↓↓↓↓ --XibaVideoPlayerEventCallback override methods start-- ↓↓↓↓↓↓↓↓↓↓**********
     */
    @Override
    public void onPlayerPrepare() {
        play.setText("暂停");
    }

    @Override
    public void onPlayerProgressUpdate(int progress, int secProgress, int currentTime, int totalTime) {

        currentTimeTV.setText("currentTime=" + XibaUtil.stringForTime(currentTime) + " : secProgress=" + secProgress);
        totalTimeTV.setText("totalTime=" +  XibaUtil.stringForTime(totalTime));
    }

    @Override
    public void onPlayerPause() {
        play.setText("播放");
    }

    @Override
    public void onPlayerResume() {
        play.setText("暂停");
    }

    @Override
    public void onPlayerComplete() {
        play.setText("播放");
    }

    @Override
    public void onPlayerAutoComplete() {
        play.setText("播放");
    }

    @Override
    public void onSeekPositionChange(String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {

    }

    @Override
    public void onSeekVolumeChange(float percent) {

    }


    @Override
    public void onSeekBrightnessSlide(float percent) {

    }

    @Override
    public void onPlayerError(int what, int extra) {

    }
    //**********↑↑↑↑↑↑↑↑↑↑ --XibaVideoPlayerEventCallback methods end-- ↑↑↑↑↑↑↑↑↑↑**********

}
