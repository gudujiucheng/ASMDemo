package com.example.slow_method_plugin;

public class LogUtils {
    static synchronized void  log(String str) {
        System.out.println(str);
    }
}
