package com.canzhang.slow_method_lib;

import android.os.Looper;
import android.util.Log;

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

}
