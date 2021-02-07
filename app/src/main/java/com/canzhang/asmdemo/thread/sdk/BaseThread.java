package com.canzhang.asmdemo.thread.sdk;

public class BaseThread extends Thread {
    public BaseThread() {
        System.out.println(this.getClass().getSimpleName()+"----------线程创建了");
    }
}
