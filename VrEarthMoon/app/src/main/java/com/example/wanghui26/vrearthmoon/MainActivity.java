package com.example.wanghui26.vrearthmoon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    MySurfaceView mySurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mySurfaceView = new MySurfaceView(this);
        setContentView(mySurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constant.threadFlag = true;
        mySurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Constant.threadFlag = false;
        mySurfaceView.onPause();
    }
}
