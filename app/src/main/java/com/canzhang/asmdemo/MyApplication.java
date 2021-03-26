package com.canzhang.asmdemo;

import android.app.Application;

import com.canzhang.slow_method_lib.BlockManager;
import com.canzhang.slow_method_lib.impl.StacktraceBlockHandler;

public class MyApplication extends Application {
    static {
        BlockManager.installBlockManager(new StacktraceBlockHandler(100));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BlockManager.showSlowMethodActionView(this);
    }
}
