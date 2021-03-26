package com.canzhang.slow_method_lib;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.canzhang.floatview.FloatingMagnetView;
import com.canzhang.floatview.FloatingView;
import com.canzhang.floatview.MagnetViewListener;

/**
 * Asm will insert bytecode of {@link BlockManager#timingMethod(String, long)} to end of some method.
 */
public class BlockManager {

    private static IBlockHandler iBlockHandler = new IBlockHandler() {

        private static final String TAG = "Default-IBlockHandler";

        private static final int BLOCK_THRESHOLD = 100;

        @Override
        public void timingMethod(String method, int cost) {
            if (cost >= threshold()) {
                Log.i(TAG, method + " costed " + cost);
            }
        }

        @Override
        public String dump() {
            return "";
        }

        @Override
        public void clear() {

        }

        @Override
        public int threshold() {
            return BLOCK_THRESHOLD;
        }
    };

    public static void installBlockManager(IBlockHandler custom) {
        BlockManager.iBlockHandler = custom;
    }

    public static void timingMethod(String method, long cost) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            iBlockHandler.timingMethod(method, (int) cost);
        }
    }


    /**
     * 显示慢方法检测的操作布局按钮
     * @param application
     */
    public static void showSlowMethodActionView(final Application application){
        if(application==null){
            return;
        }
        final View view = LayoutInflater.from(application).inflate(R.layout.custom_float_layout, null);
        view.findViewById(R.id.bt_float_print).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iBlockHandler.dump();
                Toast.makeText(application,"打印已经记录的慢方法",Toast.LENGTH_LONG).show();
            }
        });
        view.findViewById(R.id.bt_float_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iBlockHandler.clear();
                Toast.makeText(application,"清空已经记录的慢方法",Toast.LENGTH_SHORT).show();
            }
        });
        FloatingView.get().listener(new MagnetViewListener() {
            @Override
            public void onRemove(FloatingMagnetView magnetView) {

            }

            @Override
            public void onClick(FloatingMagnetView magnetView) {
                PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                popupWindow.showAsDropDown(FloatingView.get().getView());
            }
        });

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                FloatingView.get().attach(activity);
                FloatingView.get().add();
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                FloatingView.get().detach(activity);
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }

}
