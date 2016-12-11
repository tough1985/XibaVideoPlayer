package com.axiba.xibavideoplayer.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.OrientationEventListener;

import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.listener.OnAutoOrientationChangedListener;

/**
 * 处理屏幕旋转的的逻辑
 */
public class OrientationUtils {

    private Activity activity;

    private OrientationEventListener mOrientationEventListener;

    /**
     * 是否自动旋转屏幕
     * true 自动旋转
     * false 手动旋转
     */
    private boolean isAutoRotate = true;

    /**
     * 当前屏幕状态
     * 使用ActivityInfo的屏幕方向常量
     */
    private int mCurrentScreenType;

    private OnAutoOrientationChangedListener mOnAutoOrientationChangedListener;

    public OrientationUtils(Activity activity, OnAutoOrientationChangedListener onAutoOrientationChangedListener) {
        this.activity = activity;
        this.mOnAutoOrientationChangedListener = onAutoOrientationChangedListener;
        init();
    }

    private void init() {

        mCurrentScreenType = activity.getRequestedOrientation();

        mOrientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                if ((orientation > 0 && orientation < 30) || orientation > 330) {   //竖屏
                    //如果当前已经是竖屏状态，直接返回
                    if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        return;
                    }

                    //只有在自动旋转状态处理逻辑
                    if (isAutoRotate) {
//                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        mOnAutoOrientationChangedListener.onPortrait();
                    }

                } else if (orientation > 240 && orientation < 310) {                //横屏
                    //如果当前已经是横屏状态，直接返回
                    if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        return;
                    }

                    //当前是自动模式 或者 手动模式但是屏幕是反向横屏模式
                    if (isAutoRotate ||
                            (!isAutoRotate && mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)) {
//                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        mOnAutoOrientationChangedListener.onLandscape();
                    }

                } else if (orientation > 50 && orientation < 130) {                 //反向横屏
                    //如果当前已经是反向横屏状态，直接返回
                    if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                        return;
                    }

                    //当前是自动模式 或者 手动模式但是屏幕是横屏模式
                    if (isAutoRotate ||
                            (!isAutoRotate && mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)) {

//                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        mOnAutoOrientationChangedListener.onReverseLandscape();
                    }
                }
            }
        };

        mOrientationEventListener.enable();
    }

    /**
     * 手动切换横竖屏状态
     */
    public void toggleOrientation(){
        isAutoRotate = false;
        if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * 手动竖屏
     */
    public void setOrientationPort(){
        isAutoRotate = false;
        if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return;
        }
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 手动横屏
     */
    public void setOrientationLand(){
        isAutoRotate = false;
        if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            return;
        }
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * 设置屏幕方向
     * @param orientation 请使用ActivityInfo的相关常量作为参数
     */
    public void setOrientation(int orientation){
        mCurrentScreenType = orientation;
        activity.setRequestedOrientation(orientation);
    }

    /**
     * 设置是否可用
     * @param enable
     */
    public void setEnable(boolean enable){
        if (enable) {
            this.mOrientationEventListener.enable();
        } else {
            this.mOrientationEventListener.disable();
        }
    }

    public boolean isAutoRotate() {
        return isAutoRotate;
    }

    public void setAutoRotate(boolean autoRotate) {
        isAutoRotate = autoRotate;
    }

}
