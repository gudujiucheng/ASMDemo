package com.canzhang.method_call_record_lib;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.concurrent.atomic.AtomicInteger;


public class MethodRecordSDK {
    private static AtomicInteger methodCallNum =new AtomicInteger(0) ;//可能涉及多个进程 累加就好
    private static AtomicInteger filedCallNum =new AtomicInteger(0) ;
    public synchronized static void  recordLoadFiled(String from) {
        if(!from.contains("MODEL")){//只关注这个字段
            return;
        }
        Log.d("MethodRecordSDK_", "~~~~~~~~~~~~~~~~~~~~~~~ 加载的敏感字段是：" + from);
        printStackTrace("敏感字段:"+filedCallNum.addAndGet(1));
    }
    public synchronized static void recordMethodCall(String from) {
//        if(from.contains("getHostAddress")){
//            return;
//        }
//        if(from.contains("getRunningAppProcesses")){
//            return;
//        }
//        if(from.contains("queryIntentActivities")){
//            return;
//        }
        Log.e("MethodRecordSDK", "调用的方法是：" + from);
        printStackTrace("敏感函数:"+methodCallNum.addAndGet(1));
    }

    public synchronized static void requestPermissions(final @NonNull Activity activity,
                                          final @NonNull String[] permissions, final @IntRange(from = 0) int requestCode) {
        if (permissions != null && permissions.length > 0) {
            for (String name : permissions) {
                Log.e("MethodRecordSDK", "请求的权限是：" + name);
            }
        }
        printStackTrace("请求权限");
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    private synchronized static void printStackTrace(String tips) {
        Log.e("MethodRecordSDK", String.format("\n\n----------------------%s调用堆栈开始------------------------\n\n", tips));
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            Log.d("MethodRecordSDK", stackTraceElements[i].toString());
        }
        Log.e("MethodRecordSDK", String.format("\n\n----------------------%s调用堆栈结束------------------------\n\n", tips));
    }

    public synchronized static String getString(ContentResolver resolver, String name) {
        printStackTrace("敏感函数 getString：" + name+" "+methodCallNum.addAndGet(1));
        return Settings.System.getString(resolver, name);
    }
}
