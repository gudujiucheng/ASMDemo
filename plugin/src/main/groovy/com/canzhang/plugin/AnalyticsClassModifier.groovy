package com.canzhang.plugin

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher
import java.util.zip.ZipEntry

class AnalyticsClassModifier {


    static File modifyJar(File jarFile, File tempDir, boolean nameHex, AnalyticsExtension analyticsExtension) {
        /**
         * 读取原 jar
         */
        def file = new JarFile(jarFile, false)

        /**
         * 设置输出到的 jar
         */
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = null
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                return null
            }
            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className
                ZipEntry zipEntry = new ZipEntry(entryName)
                jarOutputStream.putNextEntry(zipEntry)

                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
                if (entryName.endsWith(".class")) {
                    className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "")
                    if (isShouldModify(className, analyticsExtension)) {
                        modifiedClassBytes = modifyClass(sourceClassBytes)
                    }
                }
                if (modifiedClassBytes == null) {
                    modifiedClassBytes = sourceClassBytes
                }
                jarOutputStream.write(modifiedClassBytes)
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    private static byte[] modifyClass(byte[] srcClass) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new AnalyticsClassVisitor(classWriter)
        //首先使用ASM的ClassReader类读取.class字节数组并加载类
        ClassReader cr = new ClassReader(srcClass)
        //然后使用自定义的ClassVisitor访问到类，并修改符合我们预设条件的方法，最后返回修改后的字节数组
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    /**
     * FIXME
     * 是否需要修改（先简单过滤一些不需要操作的文件，提高编译速度，可以根据实际情况添加更多过滤）
     * @param className
     * @return
     */
    protected
    static boolean isShouldModify(String className, com.canzhang.plugin.AnalyticsExtension analyticsExtension) {
        if (className.contains('R$') ||
                className.contains('R2$') ||//R2.class及其子类（butterknife）
                className.contains('R.class') ||
                className.contains('R2.class') ||
                className.contains('BuildConfig.class')) {
            AnalyticsUtils.logD("全埋点R/Build过滤>>>" + className)
            return false
        }
        /**
         * 方便一些needExcludePackageList子类型 定向插桩
         */
        if (analyticsExtension != null && analyticsExtension.needModifyPackageList != null && analyticsExtension.needModifyPackageList.size() > 0) {
            Iterator<String> iterator = analyticsExtension.needModifyPackageList.iterator()
            while (iterator.hasNext()) {
                String packageName = iterator.next()
                if (className.startsWith(packageName)) {
                    AnalyticsUtils.logD("需要埋点的包名>>>packageName:" + packageName)
                    AnalyticsUtils.logD("需要插桩的类>>>" + className)
                    return true
                }
            }
        }
        if (analyticsExtension != null && analyticsExtension.excludePackageList != null && analyticsExtension.excludePackageList.size() > 0) {
            Iterator<String> iterator = analyticsExtension.excludePackageList.iterator()
            while (iterator.hasNext()) {
                String packageName = iterator.next()
                if (className.startsWith(packageName)) {
                    AnalyticsUtils.logD("需要过滤的包名>>>packageName:" + packageName)
                    AnalyticsUtils.logD("需要过滤的类>>>" + className)
                    return false
                }
            }
        }
        AnalyticsUtils.logD("need Modify：>>>" + className+" 测试"+File.separator)
        return true
    }

    static File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified = null
        try {
            String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
            byte[] modifiedClassBytes = modifyClass(sourceClassBytes)
            if (modifiedClassBytes) {//这种用法相当于if(modifiedClassBytes!=null)
                AnalyticsUtils.logD("current modify class:" + className)
                modified = new File(tempDir, className.replace('.', '') + '.class')
                if (modified.exists()) {
                    modified.delete()
                }
                modified.createNewFile()
                new FileOutputStream(modified).write(modifiedClassBytes)
            }
        } catch (Exception e) {
            e.printStackTrace()
            modified = classFile
        }
        return modified
    }

    static String path2ClassName(String pathName) {
        pathName.replace(File.separator, ".").replace(".class", "")
    }
}