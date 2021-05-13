package com.smartahc.android.coreqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;


/**
 * Created by yuan on 2017/10/19.
 * 扫码界面
 */

public class QrMultiActivity extends FragmentActivity implements ZXingScannerView.ResultHandler {

    public static final String KEY_QR_STR = "qr_str";

    private ZXingScannerView mScannerView;
    TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_multi);
        mScannerView = new ZXingScannerView(this);

        FrameLayout frameLayout = findViewById(R.id.container);
        frameLayout.addView(mScannerView);

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
    public void handleResult(Result... results) {
    }

    @Override
    public void onItemSelect(Result result) {
        Log.v("new result :", result.toString());
        Intent intent = new Intent();
        intent.putExtra(KEY_QR_STR, result.getText());
        setResult(RESULT_OK, intent);
        finish();
    }

}
