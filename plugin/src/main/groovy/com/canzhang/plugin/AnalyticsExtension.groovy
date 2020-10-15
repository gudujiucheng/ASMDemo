
package com.canzhang.plugin
/**
 * 可以通过此配置，配置是否开启点击事件插桩功能（在需要控制的module下面的gradle配置）
 * 这里是一个思路(可不配置)，可以扩展更多参数，让外层更方便动态配置
 *
 * 范例：myTestPluginParam { isShowDebugInfo = false
 *     excludePackageList = ['com/xxxxx/component/auto_view_point',
 *                           'com/xxxx/component/debugdialog',
 *                           'android/support',
 *                           'com/mob',
 *                           'com/tencent',
 *                           'cn/sharesdk',
 *                           'com/taobao']x
 *     //排除包中  需要定向插桩的包（仅针对属于needExcludePackageList 中部分需要插桩的子包，方便配置，例如 'android/support'大部分不需要埋点，而其中的TabLayout需要特殊埋点需求）
 *     needModifyPackageList = ['android/support/design/widget/TabLayout']}
 *
 * 其中 myTestPluginParam 是在 {@link AnalyticsPlugin} 里面配置的
 *
 */
class AnalyticsExtension {
    /**
     * 是否展示调试信息
     */
    public static boolean isShowDebugInfo = false
    /**
     * 需要排除插桩的包名 （这里需要注意排除的为needModifyPackageList 一些子类型）
     *
     * 比如一些support包一些第三方代码 我们可以进行排除。
     */
    public ArrayList<String> excludePackageList = []
    /**
     * 需要定向插桩的包
     */
    public ArrayList<String> needModifyPackageList = []

}