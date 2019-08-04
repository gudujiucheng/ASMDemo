package com.canzhang.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Google官方在Android Gradle的1.5.0 版本以后提供了 Transfrom API,
 * 允许第三方 Plugin 在打包 dex 文件之前的编译过程中操作 .class 文件，
 * 我们做的就是实现Transform进行.class文件遍历拿到所有方法，修改完成对原文件进行替换。
 */
class AnalyticsTransform extends Transform {
    private static Project project
    private AnalyticsExtension analyticsExtension

    AnalyticsTransform(Project project, AnalyticsExtension analyticsExtension) {
        this.project = project
        this.analyticsExtension = analyticsExtension
    }

    /**
     * /返回该transform对应的task名称（编译后会出现在build/intermediates/transform下生成对应的文件夹）
     * @return
     */
    @Override
    String getName() {
        return AnalyticsSetting.PLUGIN_NAME
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
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
    Set<QualifiedContent.Scope> getScopes() {
        //点进去可以看到这个包含(项目、项目依赖、外部库)
        //Scope.PROJECT,
        //Scope.SUB_PROJECTS,
        //Scope.EXTERNAL_LIBRARIES
        return TransformManager.SCOPE_FULL_PROJECT
//        return Sets.immutableEnumSet(
//                QualifiedContent.Scope.PROJECT,
//                QualifiedContent.Scope.SUB_PROJECTS)
    }

    @Override
    boolean isIncremental() {//是否增量构建
        return false
    }

    //这里需要注意，就算什么都不做，也需要把所有的输入文件拷贝到目标目录下，否则下一个Task就没有TransformInput了,
    // 如果是此方法空实现，最后会导致打包的APK缺少.class文件
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        _transform(transformInvocation.context, transformInvocation.inputs, transformInvocation.outputProvider, transformInvocation.incremental)
    }

    void _transform(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        printMsg()
        if (!incremental) {
            outputProvider.deleteAll()
        }

        /**Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，要分开遍历 */
        inputs.each { TransformInput input ->
            /**遍历目录*/
            input.directoryInputs.each { DirectoryInput directoryInput ->
                /**当前这个 Transform 输出目录*/
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                File dir = directoryInput.file

                if (dir) {
                    HashMap<String, File> modifyMap = new HashMap<>()
                    /**遍历以某一扩展名结尾的文件*/
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                        File classFile ->
                            if (AnalyticsClassModifier.isShouldModify(classFile.name, analyticsExtension)) {
                                File modified = AnalyticsClassModifier.modifyClassFile(dir, classFile, context.getTemporaryDir())
                                if (modified != null) {
                                    /**key 为包名 + 类名，如：/cn/data/autotrack/android/app/MainActivity.class*/
                                    String ke = classFile.absolutePath.replace(dir.absolutePath, "")
                                    modifyMap.put(ke, modified)//修改过后的放到一个map中然后在写回源目录，覆盖原来的文件
                                }
                            }
                    }
                    FileUtils.copyDirectory(directoryInput.file, dest)
                    modifyMap.entrySet().each {
                        Map.Entry<String, File> en ->
                            File target = new File(dest.absolutePath + en.getKey())
                            if (target.exists()) {
                                target.delete()
                            }
                            FileUtils.copyFile(en.getValue(), target)
                            en.getValue().delete()
                    }
                }
            }

            /**遍历 jar*/
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.file.name

                /**截取文件路径的 md5 值重命名输出文件,因为可能同名,会覆盖*/
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                /** 获取 jar 名字*/
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }

                /** 获得输出文件*/
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                def modifiedJar = AnalyticsClassModifier.modifyJar(jarInput.file, context.getTemporaryDir(), true, analyticsExtension)
                if (modifiedJar == null) {
                    modifiedJar = jarInput.file
                }
                FileUtils.copyFile(modifiedJar, dest)
            }
        }
    }

    /**
     * 打印提示信息
     */
    static void printMsg() {
        println()
        println("####################################################################")
        println("########                                                    ########")
        println("########                                                    ########")
        println("########                 transform 编译插件                  ########")
        println("########                   by canzhang                      ########")
        println("########                                                    ########")
        println("########                                                    ########")
        println("####################################################################")
        println()
    }
}