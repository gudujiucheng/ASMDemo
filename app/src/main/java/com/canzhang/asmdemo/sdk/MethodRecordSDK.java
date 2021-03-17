package com.canzhang.asmdemo.sdk;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.canzhang.asmdemo.MainActivity;

public class MethodRecordSDK {
    public static void recordMethodCall(String from) {
        Log.e("MethodRecordSDK", "调用的方法是：" + from);
        printStackTrace("敏感函数");
    }

    public static void requestPermissions(final @NonNull Activity activity,
                                          final @NonNull String[] permissions, final @IntRange(from = 0) int requestCode) {
        if (permissions != null && permissions.length > 0) {
            for (String name : permissions) {
                Log.e("MethodRecordSDK", "请求的权限是：" + name);
            }
        }
        printStackTrace("请求权限");
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    private static void printStackTrace(String tips) {
        Log.e("MethodRecordSDK", String.format("\n\n----------------------%s调用堆栈开始------------------------\n\n", tips));
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            Log.d("MethodRecordSDK", stackTraceElements[i].toString());
        }
        Log.e("MethodRecordSDK", String.format("\n\n----------------------%s调用堆栈结束------------------------\n\n", tips));
    }

    public static String getString(ContentResolver resolver, String name) {
        printStackTrace("getString：" + name+" ");
        return Settings.System.getString(resolver, name);
    }
}
