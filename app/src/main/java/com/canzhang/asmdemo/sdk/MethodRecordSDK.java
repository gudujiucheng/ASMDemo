package com.canzhang.asmdemo.sdk;

import android.util.Log;

public class MethodRecordSDK {
    public static void recordMethodCall(String from) {
        Log.e("MethodRecordSDK", "\n\n----------------------敏感函数调用堆栈开始------------------------\n\n");
        Log.e("MethodRecordSDK", "调用的方法是："+from);
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            System.out.println(stackTraceElements[i]);
        }
        Log.e("MethodRecordSDK", "\n\n----------------------敏感函数调用堆栈结束------------------------\n\n");
    }
}
