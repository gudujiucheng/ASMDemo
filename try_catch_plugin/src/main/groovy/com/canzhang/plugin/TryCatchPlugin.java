package com.canzhang.plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;


public class TryCatchPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        //可以依据这个名字（tryCatchExtension），在依赖module的 gradle 中创建一些配置参数
        project.getExtensions().create("tryCatchExtension", TryCatchExtension.class);
        boolean usePlugin = false;
        Properties properties = new Properties();
        if (project.getRootProject().file("gradle.properties").exists()) {
            try {
                properties.load(new FileInputStream(project.getRootProject().file("gradle.properties")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            usePlugin = Boolean.parseBoolean(properties.getProperty("isOpenTryCatch", "false"));
        }

        if (appExtension != null && usePlugin) {
            System.out.println("-------------注册try catch 插件--------------");
            appExtension.registerTransform(new TryCatchTransform(project), Collections.EMPTY_LIST);
        } else {
            System.out.println("-------------关闭try catch 插件--------------");
        }

    }
}
