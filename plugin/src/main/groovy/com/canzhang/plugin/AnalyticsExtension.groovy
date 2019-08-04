
package com.canzhang.plugin
/**
 * 可以通过此配置，配置是否开启点击事件插桩功能（在需要控制的module下面的gradle配置）
 * 这里是一个思路，可以扩展更多参数，让外层更方便动态配置
 *
 * 范例：fqlAnalytics { disableAppClick = false}* */
class AnalyticsExtension {
    /**
     * 是否展示调试信息
     */
    public static boolean isShowDebugInfo = false
    /**
     * 需要排除插桩的包名 （这里需要注意排除的为needModifyPackageList 一些子类型）
     */
    public ArrayList<String> excludePackageList = []
    /**
     * 需要插桩的包名
     */
    public ArrayList<String> needModifyPackageList = []

}