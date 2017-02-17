package com.axiba.xibavideoplayer.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.axiba.xibavideoplayer.sample.listViewDemo.ListDemoActivity;
import com.axiba.xibavideoplayer.sample.recyclerViewDemo.RecyclerViewDemoActivity;
import com.axiba.xibavideoplayer.sample.viewPagerDemo.ViewPagerDemoActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = MainActivity.class.getSimpleName();

    private Button ijkDemoBN;
    private Button xibaSimpleDemoBN;
    private Button orientationBN;
    private Button listBN;
    private Button recyclerviewDemoBN;
    private Button viewpagerDemoBN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ijkDemoBN = (Button) findViewById(R.id.ijk_demo_BN);
        xibaSimpleDemoBN = (Button) findViewById(R.id.xiba_sample_demo_BN);
        orientationBN = (Button) findViewById(R.id.orientation_demo_BN);
        listBN = (Button) findViewById(R.id.list_demo_BN);
        recyclerviewDemoBN = (Button) findViewById(R.id.recyclerview_demo_BN);
        viewpagerDemoBN = (Button) findViewById(R.id.viewpager_demo_BN);

        ijkDemoBN.setOnClickListener(this);
        xibaSimpleDemoBN.setOnClickListener(this);
        orientationBN.setOnClickListener(this);
        listBN.setOnClickListener(this);
        recyclerviewDemoBN.setOnClickListener(this);
        viewpagerDemoBN.setOnClickListener(this);
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
            case R.id.list_demo_BN:
                target = ListDemoActivity.class;
                break;
            case R.id.recyclerview_demo_BN:
                target = RecyclerViewDemoActivity.class;
                break;
            case R.id.viewpager_demo_BN:
                target = ViewPagerDemoActivity.class;
                break;

        }
        startActivity(new Intent(this, target));
    }
}
