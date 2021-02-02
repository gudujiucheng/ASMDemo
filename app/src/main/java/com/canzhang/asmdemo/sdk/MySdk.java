package com.canzhang.asmdemo.sdk;

import androidx.annotation.Keep;
import android.util.Log;
import android.view.View;

public class MySdk {
    /**
     * 常规view 被点击，自动埋点
     *
     * @param view View
     */
    @Keep
    public static void onViewClick(View view) {
        Log.e("Test","成功插入 666666："+view);
    }
}
