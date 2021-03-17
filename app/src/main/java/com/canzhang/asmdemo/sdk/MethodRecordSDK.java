package com.canzhang.asmdemo.sdk;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class MethodRecordSDK {
    public static void recordMethodCall(String from) {
        Log.e("MethodRecordSDK", "\n\n----------------------敏感函数调用堆栈开始------------------------\n\n");
        Log.e("MethodRecordSDK", "调用的方法是：" + from);
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            Log.d("MethodRecordSDK", stackTraceElements[i].toString());
        }
        Log.e("MethodRecordSDK", "\n\n----------------------敏感函数调用堆栈结束------------------------\n\n");
    }

    public static void requestPermissions(final @NonNull Activity activity,
                                          final @NonNull String[] permissions, final @IntRange(from = 0) int requestCode) {
        if (permissions != null && permissions.length > 0) {
            for (String name : permissions) {
                Log.e("MethodRecordSDK", "请求的权限是：" + name);
            }
        }
        Log.e("MethodRecordSDK", "\n\n----------------------请求权限调用堆栈开始------------------------\n\n");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            Log.d("MethodRecordSDK", stackTraceElements[i].toString());
        }
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
        Log.e("MethodRecordSDK", "\n\n----------------------请求权限调用调用堆栈结束------------------------\n\n");
    }
}
