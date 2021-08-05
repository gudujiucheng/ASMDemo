package com.canzhang.asmdemo;

import android.app.Application;

import com.canzhang.method_call_record_lib.MethodRecordSDK;
import com.canzhang.method_call_record_lib.RecordCallListener;
import com.canzhang.slow_method_lib.BlockManager;
import com.canzhang.slow_method_lib.impl.StacktraceBlockHandler;

public class MyApplication extends Application {
    static {
        BlockManager.installBlockManager(new StacktraceBlockHandler(100));

        //测试敏感函数调用
        MethodRecordSDK.setRecordCallListener(new RecordCallListener() {
            @Override
            public void onRecordMethodCall(String s) {
                if(s.contains("queryIntentActivities")){
                    return;
                }
                if(s.contains("getRunningAppProcesses")){
                    return;
                }
                if(s.contains("getHostAddress")){
                    return;
                }
                android.util.Log.e("MethodRecordSDK", "调用的方法是：" + s);
                android.util.Log.e("MethodRecordSDK", String.format("\n\n----------------------%s调用堆栈开始------------------------\n\n", "敏感函数"));
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

                for(int i = 0; i < stackTraceElements.length; ++i) {
                    android.util.Log.d("MethodRecordSDK", stackTraceElements[i].toString());
                }

                android.util.Log.e("MethodRecordSDK", String.format("\n\n----------------------%s调用堆栈结束------------------------\n\n", "敏感函数"));
            }

            @Override
            public void onRecordLoadFiled(String s) {

            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BlockManager.showSlowMethodActionView(this);


    }
}
