package com.smartahc.android.qr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.smartahc.android.coreqr.MultiQrUtil;


public class MainActivity extends AppCompatActivity {

    private Button btnTest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTest = (Button) findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultiQrUtil.scan(MainActivity.this, new MultiQrUtil.OnCompleteListener() {
                    @Override
                    public void onComplete(String str) {

                        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }
}
