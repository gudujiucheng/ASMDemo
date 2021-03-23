package com.example.slow_method_plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

public class SlowMethodRecordPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        boolean usePlugin = false;
        Properties properties = new Properties();
        if (project.getRootProject().file("gradle.properties").exists()) {
            try {
                properties.load(new FileInputStream(project.getRootProject().file("gradle.properties")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            usePlugin = Boolean.parseBoolean(properties.getProperty("isOpenSlowMethodRecordPlugin", "false"));
        }

        if (appExtension != null && usePlugin) {
            System.out.println("-------------注册 慢方法检测 插件--------------");
            appExtension.registerTransform(new SlowMethodRecordTransform(project), Collections.EMPTY_LIST);
        } else {
            System.out.println("-------------关闭 慢方法检测 插件--------------");
        }

    }
}
