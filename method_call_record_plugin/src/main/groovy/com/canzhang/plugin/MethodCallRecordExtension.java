package com.canzhang.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodCallRecordExtension {


    /**
     * 精准匹配：需要方法名、方法描述、归属类严格匹配（这个适用于严格匹配类名和方法的，不适用于点击回调一类的，点击回调是在系统api内部调用的，插桩不生效）
     *
     * @ key :类名  如：android/telephony/TelephonyManager
     * @ list item value :方法名+方法描述  如：getLine1Number()Ljava/lang/String; 支持多个方法
     */
    public static Map<String, List<String>> accurateMethodMap = new HashMap<>();

    /**
     * 模糊匹配：只关注方法名和方法描述，方法归属的类不做判断
     *
     * @ key :方法名 如：getLine1Number
     * @ list item value :方法描述  如：()Ljava/lang/String;
     */
    public static Map<String,  List<String>> fuzzyMethodMap = new HashMap<>();

    /**
     * 不知类的路径，和方法描述怎么写，可以在这里添加方法名，然后build一下会自动打印出来
     * <p>
     * item value: 方法名：如 getLine1Number
     */
    public static List<String> methodTest = new ArrayList<>();
}
