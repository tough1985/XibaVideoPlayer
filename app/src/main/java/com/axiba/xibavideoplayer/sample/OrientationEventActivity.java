package com.axiba.xibavideoplayer.sample;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;

/**
 * Created by xiba on 2016/12/3.
 */

public class OrientationEventActivity extends AppCompatActivity{

    public static final String TAG = OrientationEventActivity.class.getSimpleName();

    private Button orientationChangeBN;
    private Button orientationEventBN;

    private int mCurrentScreenType; //当前屏幕状态

    private OrientationEventListener orientationEventListener;

    private boolean isAutoRotate = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_event);

        Log.e(TAG, "onCreate");
        orientationChangeBN = (Button) findViewById(R.id.orientation_change_BN);

        orientationEventBN = (Button) findViewById(R.id.orientation_event_BN);

        mCurrentScreenType = this.getRequestedOrientation();
        Log.e(TAG, "mCurrentScreenType=" + mCurrentScreenType);

        orientationChangeBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isAutoRotate = false;
//                setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                toggleOrientation();
            }
        });

        orientationEventBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                orientationEventListener = new OrientationEventListener(OrientationEventActivity.this) {
                    @Override
                    public void onOrientationChanged(int orientation) {
                        Log.e(TAG, "orientation = " + orientation);


                        if ((orientation > 0 && orientation < 30) || orientation > 330) {   //竖屏

                            if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                                return;
                            }

                            if (isAutoRotate) {
                                setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            }

                        } else if (orientation > 240 && orientation < 310) {                //横屏
                            if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                return;
                            }

                            if (isAutoRotate ||
                                    (!isAutoRotate && mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)) {
                                setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            }

                        } else if (orientation > 50 && orientation < 130) {                 //反向横屏
                            if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                                return;
                            }

                            if (isAutoRotate || //自动模式
                                    (!isAutoRotate && mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)) {   //手动模式，但是屏幕是横屏

                                setOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                            }


                        }
                    }
                };
                orientationEventListener.enable();
            }
        });
    }

    private void toggleOrientation(){
        isAutoRotate = false;
        if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void clickToPort(){
        isAutoRotate = false;
        if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return;
        }
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void clickToLand(){
        isAutoRotate = false;
        if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            return;
        }
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void setOrientation(int orientation){
        mCurrentScreenType = orientation;
        OrientationEventActivity.this.setRequestedOrientation(orientation);
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        Log.e(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }
}
