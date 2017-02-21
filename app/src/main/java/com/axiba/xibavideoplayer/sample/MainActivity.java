package com.axiba.xibavideoplayer.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.axiba.xibavideoplayer.sample.listViewDemo.ListDemoActivity;
import com.axiba.xibavideoplayer.sample.recyclerViewDemo.RecyclerViewDemoActivity;
import com.axiba.xibavideoplayer.sample.simpleDemo.SimpleDemoActivity;
import com.axiba.xibavideoplayer.sample.viewPagerDemo.ViewPagerDemoActivity;
import com.axiba.xibavideoplayer.sample.viewPagerWithListView.PagerWithListActivity;
import com.axiba.xibavideoplayer.sample.viewPagerWithRecyclerView.PagerWithRecyclerActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = MainActivity.class.getSimpleName();

    private Button xibaSimpleDemoBN;
    private Button listBN;
    private Button recyclerviewDemoBN;
    private Button viewpagerDemoBN;
    private Button pagerWithListBN;
    private Button pagerWithRecyclerBN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xibaSimpleDemoBN = (Button) findViewById(R.id.xiba_sample_demo_BN);
        listBN = (Button) findViewById(R.id.list_demo_BN);
        recyclerviewDemoBN = (Button) findViewById(R.id.recyclerview_demo_BN);
        viewpagerDemoBN = (Button) findViewById(R.id.viewpager_demo_BN);
        pagerWithListBN = (Button) findViewById(R.id.pager_with_list_BN);
        pagerWithRecyclerBN = (Button) findViewById(R.id.pager_with_recycler_BN);

        xibaSimpleDemoBN.setOnClickListener(this);
        listBN.setOnClickListener(this);
        recyclerviewDemoBN.setOnClickListener(this);
        viewpagerDemoBN.setOnClickListener(this);
        pagerWithListBN.setOnClickListener(this);
        pagerWithRecyclerBN.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        Class target = null;
        switch (v.getId()) {
            case R.id.xiba_sample_demo_BN:
                target = SimpleDemoActivity.class;
                break;
            case R.id.list_demo_BN:
                target = ListDemoActivity.class;
                break;
            case R.id.recyclerview_demo_BN:
                target = RecyclerViewDemoActivity.class;
                break;
            case R.id.viewpager_demo_BN:
                target = ViewPagerDemoActivity.class;
                break;
            case R.id.pager_with_list_BN:
                target = PagerWithListActivity.class;
                break;
            case R.id.pager_with_recycler_BN:
                target = PagerWithRecyclerActivity.class;
                break;

        }
        startActivity(new Intent(this, target));
    }
}
