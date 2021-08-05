package com.canzhang.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodCallRecordExtension {


    //    {
//        "classPath": "android/telephony/TelephonyManager",
//        "methodDesc": "getLine1Number()Ljava/lang/String;",
//    }
    //类/方法描述
    public static Map<String, String> methodMap = new HashMap<>();

    //不知类的路径，和方法描述怎么写，可以在这里添加方法名，然后build一下会自动打印出来
    public static List<String> methodTest = new ArrayList<>();
}
