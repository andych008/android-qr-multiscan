package com.smartahc.android.coreqr;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtils {
    private DisplayUtils() {
    }


    public static int getScreenOrientation(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        byte orientation1;
        if (display.getWidth() == display.getHeight()) {
            orientation1 = 3;
        } else if (display.getWidth() < display.getHeight()) {
            orientation1 = 1;
        } else {
            orientation1 = 2;
        }

        return orientation1;
    }
}