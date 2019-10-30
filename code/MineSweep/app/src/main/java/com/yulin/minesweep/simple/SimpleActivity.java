package com.yulin.minesweep.simple;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.yulin.minesweep.R;
import com.yulin.minesweep.base.BaseActivity;

public class SimpleActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "houchend_SimpleActivity";

    private Button mBtnOpen, mBtnInsertFlag;
    private SimpleGridLayout mGridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        mBtnOpen = findViewById(R.id.btn_open);
        mBtnInsertFlag = findViewById(R.id.btn_insert_flag);

        mBtnOpen.setOnClickListener(this);
        mBtnInsertFlag.setOnClickListener(this);

        mBtnOpen.setSelected(true);
        mBtnInsertFlag.setSelected(false);

        mGridLayout = findViewById(R.id.simple_grid_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.btn_open) {
            Log.d(TAG, "onClick: open");
            mBtnOpen.setSelected(true);
            mBtnInsertFlag.setSelected(false);
            mGridLayout.setStatus(SimpleGridLayout.STATUS_OPEN);
        } else if (vid == R.id.btn_insert_flag) {
            Log.d(TAG, "onClick: flag");
            mBtnOpen.setSelected(false);
            mBtnInsertFlag.setSelected(true);
            mGridLayout.setStatus(SimpleGridLayout.STATUS_INSERT_FLAG);
        }
    }

}
