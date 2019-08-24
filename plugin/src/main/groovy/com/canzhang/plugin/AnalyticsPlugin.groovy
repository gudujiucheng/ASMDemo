package com.canzhang.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 可以通过配置主工程目录中的gradle.properties 中的
 * canPlugin.disablePlugin字段来控制是否开启此插件
 */
class AnalyticsPlugin implements Plugin<Project> {
    void apply(Project project) {

        //这个AnalyticsExtension 以及canPlugin名称，可以提供我们在外层配置一些参数，从而支持外层扩展
        AnalyticsExtension extension = project.extensions.create("canPlugin", AnalyticsExtension)

        //这个可以读取工程的gradle.properties 里面的can.disablePlugin 字段，控住是否注册此插件
        boolean disableAnalyticsPlugin = false
        Properties properties = new Properties()
        if (project.rootProject.file('gradle.properties').exists()) {
            properties.load(project.rootProject.file('gradle.properties').newDataInputStream())
            disableAnalyticsPlugin = Boolean.parseBoolean(properties.getProperty("disablePlugin", "false"))
        }

        if (!disableAnalyticsPlugin) {
            println("------------您开启了全埋点插桩插件--------------")
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            //注册我们的transform类
            appExtension.registerTransform(new com.canzhang.plugin.AnalyticsTransform(project, extension))
        } else {
            println("------------您已关闭了全埋点插桩插件--------------")
        }
    }
}