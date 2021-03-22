package com.canzhang.method_call_record_lib;

public interface RecordCallListener {
    void onRecordMethodCall(String from);
    void onRecordLoadFiled(String field);
}
