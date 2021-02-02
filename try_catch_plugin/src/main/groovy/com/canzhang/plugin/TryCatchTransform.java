package com.canzhang.plugin;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Google官方在Android Gradle的1.5.0 版本以后提供了 Transfrom API,
 * 允许第三方 Plugin 在打包 dex 文件之前的编译过程中操作 .class 文件，
 * 我们做的就是实现Transform进行.class文件遍历拿到所有方法，修改完成对原文件进行替换。
 */
public class TryCatchTransform extends Transform {
    @Override
    public String getName() {
        return "try_catch_plugin";
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * <p>
     * 这里我们仅要处理字节码即可
     */
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    /**
     * 指 Transform 要操作内容的范围，官方文档 Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     * @return
     */
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    //这里需要注意，就算什么都不做，也需要把所有的输入文件拷贝到目标目录下，否则下一个Task就没有TransformInput了,
    // 如果是此方法空实现，最后会导致打包的APK缺少.class文件
    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        _transform(transformInvocation.getContext(), transformInvocation.getInputs(), transformInvocation.getOutputProvider(), transformInvocation.isIncremental());
    }

    void _transform(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        printMsg();
        if (!isIncremental) {//如果是非增量编译，则清空已经输出的内容
            outputProvider.deleteAll();
        }

        /**Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，要分开遍历 */
        for (TransformInput input : inputs) {
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {//遍历目录
                /**当前这个 Transform 输出目录*/
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(),
                        Format.DIRECTORY);
                File dir = directoryInput.getFile();
                if (dir != null) {
                    HashMap<String, File> modifyMap = new HashMap<>();
                    /**遍历以某一扩展名结尾的文件*/
                    getAllFileFromDir(dir, new CallBack() {
                        @Override
                        public void callBack(File file) {
//                            if (file != null && file.getName().endsWith(".class")) {
//
//                            }
                        }
                    });

                }


            }


            for (JarInput jarInput : input.getJarInputs()) {//遍历jar包

            }


        }

    }


    private static void getAllFileFromDir(File dir, CallBack callBack) {
        if (dir == null) {
            return;
        }
        File[] fs = dir.listFiles();
        if (fs == null) {
            return;
        }
        for (File f : fs) {
            if (f.isDirectory())    //若是目录，则递归打印该目录下的文件
                getAllFileFromDir(f, callBack);
            if (f.isFile()) {        //若是文件，直接打印
                callBack.callBack(f);
                System.out.println("---try catch 插件--->" + f);
            }
        }
    }


    public interface CallBack {
        void callBack(File file);
    }

    /**
     * 打印提示信息
     */
    static void printMsg() {
        System.out.println("####################################################################");
        System.out.println("########                                                    ########");
        System.out.println("########                                                    ########");
        System.out.println("########                 try catch 编译插件                  ########");
        System.out.println("########                   by canzhang                      ########");
        System.out.println("########                                                    ########");
        System.out.println("########                                                    ########");
        System.out.println("####################################################################");
    }
}
