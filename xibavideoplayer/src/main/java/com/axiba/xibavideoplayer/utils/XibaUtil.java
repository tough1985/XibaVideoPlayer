package com.axiba.xibavideoplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by xiba on 2016/11/26.
 */
public class XibaUtil {

    /**
     * wifi是否连接
     * @param context
     * @return true 已连接; false wifi未连接
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == connectivityManager.TYPE_WIFI;
    }
}
