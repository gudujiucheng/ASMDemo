package com.canzhang.plugin;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.quinn.hunter.transform.HunterTransform;

import org.gradle.api.Project;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Google官方在Android Gradle的1.5.0 版本以后提供了 Transfrom API,
 * 允许第三方 Plugin 在打包 dex 文件之前的编译过程中操作 .class 文件，
 * 我们做的就是实现Transform进行.class文件遍历拿到所有方法，修改完成对原文件进行替换。
 */
public class TryCatchTransform extends HunterTransform {
    private Project project;
    private TryCatchExtension tryCatchExtension;

    public TryCatchTransform(Project project) {
        super(project);
        this.project = project;
        this.bytecodeWeaver = new TryCatchWeaver();
    }


    @Override
    public void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        tryCatchExtension = (TryCatchExtension) project.getExtensions().getByName("tryCatchExtension");
        if (tryCatchExtension.methodMap != null && tryCatchExtension.methodMap.size() > 0) {
            System.out.println("配置项------开始--->>>>>>>>>>>>>>>>>>");
            for (String key : tryCatchExtension.methodMap.keySet()) {
                List<String> methodNameList = tryCatchExtension.methodMap.get(key);
                if (methodNameList == null) {
                    System.out.println("------发现异常配置项，方法列表为空------类："+key);
                    continue;
                }
                for (String methodName : methodNameList) {
                    System.out.println("className:" + key + "\nmethodName:" + methodName);
                }
            }
            System.out.println("配置项------结束--->>>>>>>>>>>>>>>>>>");
        }

        bytecodeWeaver.setExtension(tryCatchExtension);
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental);
    }

}
