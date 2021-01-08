package com.smartahc.android.qr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.zxing.Result;
import com.smartahc.android.coreqr.ZXingScannerView;
import com.smartahc.android.coreqr.ZXingType;

import java.util.ArrayList;


/**
 * Created by yuan on 2017/10/19.
 * 扫码界面
 */

public class QrActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        mScannerView = new ZXingScannerView(this);

        FrameLayout frameLayout = findViewById(R.id.container);
        frameLayout.addView(mScannerView);
        results.clear();

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QrActivity.this.results.clear();
                lastTime = 0;
                mScannerView.resumeCameraPreview(QrActivity.this);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private ArrayList<String> results = new ArrayList<>();
    private long lastTime;
    @Override
    public void handleResult(Result... results) {
        Log.v("result len:", ""+results.length);
        if (results.length > 0) {
            for (int i = 0; i < results.length; i++) {
                Result result = results[i];
                printLog(result.getText());
            }
            //
            long currentTimeMillis = System.currentTimeMillis();
            long offTime = currentTimeMillis - lastTime;

            if (results.length >= 4) {
                mScannerView.stopCameraPreview();
            } else if (offTime < 16000) {
                if (this.results.size() == results.length) {
                    lastTime = currentTimeMillis;
                    if (offTime < 1000) {
                        this.results.clear();
                        mScannerView.resumeCameraPreview(this);
                    }
                } else {
                    this.results.clear();
                    mScannerView.resumeCameraPreview(this);
                }
            }
        }
    }

    private void printLog(String result) {
        if (!results.contains(result)) {
            Log.v("new result :", result);
            results.add(result);
            Log.v("multi results : " + results.size(), results.toString());
            if (results.size() == 30) {
                Toast.makeText(this, "总数:" + results.size(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
