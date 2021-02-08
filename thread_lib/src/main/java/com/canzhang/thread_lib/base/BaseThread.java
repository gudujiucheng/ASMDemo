package com.canzhang.thread_lib.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BaseThread extends Thread{
    public BaseThread() {
    }

    public BaseThread(@Nullable Runnable target) {
        super(target);
    }

    public BaseThread(@Nullable ThreadGroup group, @Nullable Runnable target) {
        super(group, target);
    }

    public BaseThread(@NonNull String name) {
        super(name);
    }

    public BaseThread(@Nullable ThreadGroup group, @NonNull String name) {
        super(group, name);
    }

    public BaseThread(@Nullable Runnable target, @NonNull String name) {
        super(target, name);
    }

    public BaseThread(@Nullable ThreadGroup group, @Nullable Runnable target, @NonNull String name) {
        super(group, target, name);
    }

    public BaseThread(@Nullable ThreadGroup group, @Nullable Runnable target, @NonNull String name, long stackSize) {
        super(group, target, name, stackSize);
    }


    @Override
    public synchronized void start() {
        super.start();
    }
}
