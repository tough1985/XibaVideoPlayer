package com.axiba.xibavideoplayer.bean;

import java.util.Map;

/**
 * Created by xiba on 2016/11/26.
 */
public class VideoSource {

    private String Url;
    private Map<String, String> mapHeadData;
    boolean looping;

    public VideoSource(String url, Map<String, String> mapHeadData, boolean looping) {
        Url = url;
        this.mapHeadData = mapHeadData;
        this.looping = looping;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public Map<String, String> getMapHeadData() {
        return mapHeadData;
    }

    public void setMapHeadData(Map<String, String> mapHeadData) {
        this.mapHeadData = mapHeadData;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }
}
