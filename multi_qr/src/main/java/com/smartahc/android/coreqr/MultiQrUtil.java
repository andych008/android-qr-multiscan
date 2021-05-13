package com.smartahc.android.coreqr;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


import com.ypx.imagepicker.helper.launcher.PLauncher;

import java.io.Serializable;

/**
 * 多二维码扫描入口类
 *
 * @author: dongwen.wang
 * @date:  2021/1/8 19:19
 */
public class MultiQrUtil {
    /**
     * 回调
     */
    public interface OnCompleteListener extends Serializable {
        void onComplete(String str);
    }

    /**
     * 调用拍照
     *
     * @param activity      调用activity
     * @param listener      回调
     */
    public static void scan(Activity activity, OnCompleteListener listener) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ) {
            Intent intent = new Intent(activity, QrMultiActivity.class);
            PLauncher.init(activity).startActivityForResult(intent, new CameraActivityCallBack(listener));
        } else {
            //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private static class CameraActivityCallBack implements PLauncher.Callback {
        private OnCompleteListener listener;

        private CameraActivityCallBack(OnCompleteListener listener) {
            this.listener = listener;
        }

        @Override
        public void onActivityResult(int resultCode, Intent data) {
            if (listener != null
                    && resultCode == Activity.RESULT_OK
                    && data.hasExtra(QrMultiActivity.KEY_QR_STR)) {
                String str = data.getStringExtra(QrMultiActivity.KEY_QR_STR);

                listener.onComplete(str);
            }
        }
    }



}
