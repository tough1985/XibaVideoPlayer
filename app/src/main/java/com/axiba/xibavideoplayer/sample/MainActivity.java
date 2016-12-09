package com.axiba.xibavideoplayer.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.HashMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = MainActivity.class.getSimpleName();

    private Button ijkDemoBN;
    private Button xibaSimpleDemoBN;
    private Button orientationBN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ijkDemoBN = (Button) findViewById(R.id.ijk_demo_BN);
        xibaSimpleDemoBN = (Button) findViewById(R.id.xiba_sample_demo_BN);
        orientationBN = (Button) findViewById(R.id.orientation_demo_BN);

        ijkDemoBN.setOnClickListener(this);
        xibaSimpleDemoBN.setOnClickListener(this);
        orientationBN.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        Class target = null;
        switch (v.getId()) {
            case R.id.ijk_demo_BN:
                target = IjkDemoActivity.class;
                break;
            case R.id.xiba_sample_demo_BN:
                target = SimpleDemoActivity.class;
                break;
            case R.id.orientation_demo_BN:
                target = OrientationEventActivity.class;
                break;
        }
        startActivity(new Intent(this, target));
    }
}
