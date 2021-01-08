package com.smartahc.android.qr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.smartahc.android.coreqr.ZXingScannerView;

import java.util.ArrayList;


/**
 * Created by yuan on 2017/10/19.
 * 扫码界面
 */

public class QrMultiActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_multi);
        mScannerView = new ZXingScannerView(this);

        FrameLayout frameLayout = findViewById(R.id.container);
        frameLayout.addView(mScannerView);
        results.clear();

        textView = findViewById(R.id.cancel_button);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScannerView.isScanning()) {
                    finish();
                } else {
                    mScannerView.resumeCameraPreview(QrMultiActivity.this);
                }

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

    @Override
    public void handleResult(Result... results) {
    }

    @Override
    public void onItemSelect(Result result) {
        Log.v("new result :", result.toString());
        Toast.makeText(this, result.getText(), Toast.LENGTH_SHORT).show();
    }

}
