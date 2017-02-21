package com.axiba.xibavideoplayer.listUtils;

import android.graphics.Bitmap;

/**
 * Created by xiba on 2017/2/21.
 */

public class PlayerStateInfo {
    private int currentState;   //播放器当前状态
    private long position;      //当前位置
    private long duration;     //总时长
    private Bitmap cacheBitmap; //暂停时的缓存图片
    private int currentScreen;

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Bitmap getCacheBitmap() {
        return cacheBitmap;
    }

    public void setCacheBitmap(Bitmap cacheBitmap) {
        if (cacheBitmap != null) {
            this.cacheBitmap = cacheBitmap.copy(Bitmap.Config.ARGB_8888, false);
        } else {
            this.cacheBitmap = null;
        }
    }

    public void releaseBitmap(){
        if (cacheBitmap != null) {
            cacheBitmap.recycle();
        }
    }

    public int getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentScreen(int currentScreen) {
        this.currentScreen = currentScreen;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PlayerStateInfo" + " -> \n");
        sb.append("currentState=" + currentState + " ：");
        sb.append("position=" + position + " ：");
        sb.append("duration=" + duration + " ：");
        sb.append("currentScreen=" + currentScreen + " ：");
        sb.append("cacheBitmap isNull ? " + (cacheBitmap == null));

        return super.toString();
    }
}
