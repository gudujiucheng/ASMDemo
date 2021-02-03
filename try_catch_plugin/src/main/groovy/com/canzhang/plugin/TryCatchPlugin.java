package com.canzhang.plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;


public class TryCatchPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        if(appExtension!=null){
            System.out.println("-------------注册try catch 插件--------------");
            appExtension.registerTransform(new TryCatchTransform(project), Collections.EMPTY_LIST);
        }else{
            System.out.println("TryCatchPlugin 异常~~~~~~~~~~~~~~~");
        }

    }
}
