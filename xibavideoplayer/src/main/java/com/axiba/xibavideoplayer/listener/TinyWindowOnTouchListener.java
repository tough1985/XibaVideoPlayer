package com.axiba.xibavideoplayer.listener;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by xiba on 2016/12/7.
 */
public class TinyWindowOnTouchListener implements View.OnTouchListener {

    public static final String TAG = TinyWindowOnTouchListener.class.getSimpleName();

    private float mDownX;
    private float mDownY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        float vX = v.getX();
        float vY = v.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mDownX;      //右移为正，左移为负
                float deltaY = y - mDownY;      //下移为正，上移为负

                float targetX = vX + deltaX;
                float targetY = vY + deltaY;

                if (targetX <= 0) {
                    targetX = 0;
                }
                if (targetY <= 0) {
                    targetY = 0;
                }

                //让目标控件的移动不超出父容器的范围
                if (v.getParent() != null) {
                    int parentWidth = ((View)v.getParent()).getWidth();
                    int parentHeight = ((View)v.getParent()).getHeight();

                    if (targetX + v.getWidth() >= parentWidth) {
                        targetX = parentWidth - v.getWidth();
                    }

                    if (targetY + v.getHeight() >= parentHeight) {
                        targetY = parentHeight - v.getHeight();
                    }
                }

                v.setX(targetX);
                v.setY(targetY);

                break;

        }
        return true;
    }
}
