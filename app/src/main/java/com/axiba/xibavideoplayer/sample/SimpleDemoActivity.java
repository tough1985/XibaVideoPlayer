package com.axiba.xibavideoplayer.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.axiba.xibavideoplayer.XibaVideoPlayer;

/**
 * Created by xiba on 2016/11/26.
 */
public class SimpleDemoActivity extends AppCompatActivity {

    public static final String TAG = SimpleDemoActivity.class.getSimpleName();

    private XibaVideoPlayer xibaVP;
    private Button play;

    String url = "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11086&editionType=default";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_demo);

        xibaVP = (XibaVideoPlayer) findViewById(R.id.demo_xibaVP);
        play = (Button) findViewById(R.id.demo_play);

        xibaVP.setUp(url, 0, new Object[]{});

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xibaVP.prepareVideo();
            }
        });
    }

    @Override
    protected void onStop() {
        xibaVP.release();
        super.onStop();
    }
}
