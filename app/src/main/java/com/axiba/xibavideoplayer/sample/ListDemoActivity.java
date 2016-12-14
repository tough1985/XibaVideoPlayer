package com.axiba.xibavideoplayer.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.XibaListPlayUtil;
import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.utils.XibaUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiba on 2016/12/11.
 */

public class ListDemoActivity extends AppCompatActivity {
    public static final String TAG = ListDemoActivity.class.getSimpleName();

    private ListView playerList;
    private PlayerListAdapter adapter;
    private ListEventCallback eventCallback;

    private XibaListPlayUtil mXibaListPlayUtil;

    private String[] urls = {
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=10935&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11528&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11527&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11526&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11525&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11524&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11523&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11522&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11521&editionType=default",
            "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11520&editionType=default"
    };

    private List<Map<String, Object>> playerStates = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_demo);

        for (int i = 0; i < urls.length; i++) {
            Map<String, Object> stateMap = new HashMap<>();
            stateMap.put("url", urls[i]);
            stateMap.put("isSetUp", false);
            playerStates.add(stateMap);
        }

        playerList = (ListView) findViewById(R.id.player_list);
        adapter = new PlayerListAdapter(this);
        playerList.setAdapter(adapter);

        mXibaListPlayUtil = new XibaListPlayUtil(this);

        eventCallback = new ListEventCallback();
    }

    private class PlayerListAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        public PlayerListAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return urls.length;
        }

        @Override
        public String getItem(int position) {
            return urls[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.player_list_item, parent, false);
                holder = new ViewHolder();
//                holder.player = (XibaVideoPlayer) convertView.findViewById(R.id.player_list_item_XibaPlayer);
                holder.container = (FrameLayout) convertView.findViewById(R.id.player_list_item_container);
                holder.startBN = (Button) convertView.findViewById(R.id.player_list_item_play);
                holder.fullscreenBN = (Button) convertView.findViewById(R.id.player_list_item_fullscreen);
                holder.currentTimeTV = (TextView) convertView.findViewById(R.id.player_list_item_current_time);
                holder.totalTimeTV = (TextView) convertView.findViewById(R.id.player_list_item_total_time);
                holder.progressSeek = (SeekBar) convertView.findViewById(R.id.player_list_item_demo_seek);
//                holder.cacheIV = (ImageView) convertView.findViewById(R.id.player_list_item_cache_IV);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.startBN.setOnClickListener(new StartListener(holder, position, getItem(position)));
            XibaListPlayUtil.PlayerStateInfo playerStateInfo = mXibaListPlayUtil.resolveItem(position, holder.container, eventCallback);


            Log.e(TAG, "position" + position);
            Log.e(TAG, "playerStateInfo == null?" + (playerStateInfo == null));

            initHolderUIByPlayerInfo(playerStateInfo, holder);

            return convertView;
        }
    }

    private class ViewHolder{
//        XibaVideoPlayer player;
        FrameLayout container;
        Button startBN;
        Button fullscreenBN;
        TextView currentTimeTV;
        TextView totalTimeTV;
        SeekBar progressSeek;
//        ImageView cacheIV;
    }

    private class StartListener implements View.OnClickListener{
        private ViewHolder holder;
        private int position;
        private String url;

        public StartListener(ViewHolder holder, int position, String url) {
            this.holder = holder;
            this.position = position;
            this.url = url;
        }

        @Override
        public void onClick(View v) {

            mXibaListPlayUtil.togglePlay(url, position, holder.container, eventCallback);
            eventCallback.setHolder(holder);


        }
    }

    /**
     * 主要解决暂停问题
     * @param playerStateInfo
     * @param holder
     */
    private void initHolderUIByPlayerInfo(XibaListPlayUtil.PlayerStateInfo playerStateInfo, ViewHolder holder){
        if (playerStateInfo != null) {
            if (playerStateInfo.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
                holder.startBN.setText("暂停");
            } else {
                holder.startBN.setText("播放");
            }

            long totalTimeDuration = playerStateInfo.getDuration();
            long currentTimePosition = playerStateInfo.getPosition();

            holder.currentTimeTV.setText(XibaUtil.stringForTime(currentTimePosition));
            holder.totalTimeTV.setText(XibaUtil.stringForTime(totalTimeDuration));

            int progress = (int) (currentTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度

//            Log.e(TAG, "totalTimeDuration=" + totalTimeDuration);
//            Log.e(TAG, "currentTimePosition=" + currentTimePosition);
//            Log.e(TAG, "progress=" + progress);

            holder.progressSeek.setProgress(progress);
            holder.progressSeek.setEnabled(true);
        } else {
            holder.startBN.setText("播放");
            holder.currentTimeTV.setText("00:00");
            holder.totalTimeTV.setText("00:00");
            holder.progressSeek.setProgress(0);
            holder.progressSeek.setEnabled(false);
        }
    }

    private class ListEventCallback implements XibaVideoPlayerEventCallback{

        private ViewHolder holder;

        public void setHolder(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onPlayerPrepare() {
            holder.startBN.setText("暂停");
        }

        @Override
        public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {

            holder.currentTimeTV.setText(XibaUtil.stringForTime(currentTime));
            holder.totalTimeTV.setText(XibaUtil.stringForTime(totalTime));

            holder.progressSeek.setProgress(progress);
            holder.progressSeek.setSecondaryProgress(secProgress);
            if (!holder.progressSeek.isEnabled()) {
                holder.progressSeek.setEnabled(true);
            }
        }

        @Override
        public void onPlayerPause() {
            holder.startBN.setText("播放");
        }

        @Override
        public void onPlayerResume() {
            holder.startBN.setText("暂停");
        }

        @Override
        public void onPlayerComplete() {
            holder.startBN.setText("播放");
        }

        @Override
        public void onPlayerAutoComplete() {
            holder.startBN.setText("播放");
        }

        @Override
        public void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration) {
            int progress = (int) (seekTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度
            holder.progressSeek.setProgress(progress);
        }

        @Override
        public void onChangingPositionEnd() {

        }

        @Override
        public void onChangingVolume(int percent) {

        }

        @Override
        public void onChangingVolumeEnd() {

        }

        @Override
        public void onChangingBrightness(int percent) {

        }

        @Override
        public void onChangingBrightnessEnd() {

        }

        @Override
        public void onPlayerError(int what, int extra) {

        }

        @Override
        public ViewGroup onEnterFullScreen() {
            return null;
        }

        @Override
        public void onQuitFullScreen() {

        }

        @Override
        public void onEnterTinyScreen() {

        }

        @Override
        public void onQuitTinyScreen() {

        }

        @Override
        public void onSingleTap() {

        }

        @Override
        public void onDoubleTap() {

        }

        @Override
        public void onTouchLockedScreen() {

        }

    }

    @Override
    protected void onDestroy() {
        mXibaListPlayUtil.release();
        super.onDestroy();
    }
}
