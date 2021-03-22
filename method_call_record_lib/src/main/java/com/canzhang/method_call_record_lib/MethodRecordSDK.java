package com.canzhang.method_call_record_lib;

import android.content.ContentResolver;
import android.provider.Settings;

/**
 * 监控sdk
 */
public class MethodRecordSDK {

    private static RecordCallListener recordCallListener = new DefaultRecordListener();

    /**
     * 记录加载的敏感字段实现
     * @param filed
     */
    public synchronized static void recordLoadFiled(String filed) {
        recordCallListener.onRecordLoadFiled(filed);
    }

    /**
     * 记录敏感函数调用
     * 对于实例方法，可以简单通过插入我们的方法记录堆栈
     *
     * @param from
     */
    public synchronized static void recordMethodCall(String from) {
        recordCallListener.onRecordMethodCall(from);
    }


    /**
     * 对于静态方法，可以直接替换实现，这样可以做更多的操作
     *
     * @param resolver
     * @param name
     * @return
     */
    public synchronized static String getString(ContentResolver resolver, String name) {
        recordCallListener.onRecordMethodCall("敏感函数 getString：" + name);
        return Settings.System.getString(resolver, name);
    }

    /**
     * 设置回调
     * @param recordCallListener
     */
    public static void setRecordCallListener(RecordCallListener recordCallListener) {
        MethodRecordSDK.recordCallListener = recordCallListener;
    }
}
