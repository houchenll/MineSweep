package com.yulin.minesweep;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.yulin.minesweep.advance.AdvanceActivity;
import com.yulin.minesweep.medium.MediumActivity;
import com.yulin.minesweep.simple.SimpleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "houchend_MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_level_simple).setOnClickListener(this);
        findViewById(R.id.btn_level_medium).setOnClickListener(this);
        findViewById(R.id.btn_level_advance).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        int vid = v.getId();
        if (vid == R.id.btn_level_simple) {
            startActivity(SimpleActivity.class);
        } else if (vid == R.id.btn_level_medium) {
            startActivity(MediumActivity.class);
        } else if (vid == R.id.btn_level_advance) {
            startActivity(AdvanceActivity.class);
        }
    }

    private void startActivity(Class clz) {
        startActivity(new Intent(this, clz));
    }

}
