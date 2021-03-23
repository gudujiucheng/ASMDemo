package com.canzhang.method_call_record_lib;

import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultRecordListener implements RecordCallListener {
    private static AtomicInteger methodCallNum =new AtomicInteger(0) ;//可能涉及多个进程 累加就好
    private static AtomicInteger filedCallNum =new AtomicInteger(0) ;
    @Override
    public void onRecordMethodCall(String from) {
        Log.e("MethodRecordSDK", "调用的方法是：" + from);
        printStackTrace("敏感函数:"+methodCallNum.addAndGet(1));
    }

    @Override
    public void onRecordLoadFiled(String field) {
        Log.d("MethodRecordSDK", "~~~~~~~~~~~~~~~~~~~~~~~ 加载的敏感字段是：" + field);
        printStackTrace("敏感字段:"+filedCallNum.addAndGet(1));
    }


    private synchronized static void printStackTrace(String tips) {
        Log.e("MethodRecordSDK", String.format("\n\n----------------------%s调用堆栈开始------------------------\n\n", tips));
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            Log.d("MethodRecordSDK", stackTraceElements[i].toString());
        }
        Log.e("MethodRecordSDK", String.format("\n\n----------------------%s调用堆栈结束------------------------\n\n", tips));
    }

}
