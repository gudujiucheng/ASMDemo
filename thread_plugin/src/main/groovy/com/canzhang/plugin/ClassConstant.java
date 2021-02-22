package com.canzhang.plugin;

class ClassConstant {

    /**
     * 系统类
     */
    static final String S_Thread = "java/lang/Thread";
    static final String S_ThreadPoolExecutor = "java/util/concurrent/ThreadPoolExecutor";
    static final String S_ScheduledThreadPoolExecutor = "java/util/concurrent/ScheduledThreadPoolExecutor";
    static final String S_Executors = "java/util/concurrent/Executors";
    static final String S_Timer = "java/util/Timer";
    static final String S_HandlerThread = "android/os/HandlerThread";

    /**
     * 自定义的替换类类
     */
    static final String S_TBaseThread = "com/canzhang/thread_lib/proxy/TBaseThread";
    static final String S_TBaseThreadPoolExecutor = "com/canzhang/thread_lib/proxy/TBaseThreadPoolExecutor";
    //因ScheduledThreadPoolExecutor extends ThreadPoolExecutor ,但此继承关系在sdk中，asm无法改变(只能变更非系统api)，所以这个也需要处理，
    static final String S_TBaseScheduledThreadPoolExecutor = "com/canzhang/thread_lib/proxy/TBaseScheduledThreadPoolExecutor";
    //针对Executors.xxx创建线程池的方式进行字节码替换，替换为ProxyExecutors.xxx ，进行统一的线程池收拢
    static final String S_ProxyExecutors = "com/canzhang/thread_lib/proxy/ProxyExecutors";
    static final String S_TBaseTimer = "com/canzhang/thread_lib/proxy/TBaseTimer";
    static final String S_TBaseHandlerThread = "com/canzhang/thread_lib/proxy/TBaseHandlerThread";

}
