package com.canzhang.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * ClassVisitor:主要负责遍历类的信息，包括类上的注解、构造方法、字段等等。
 */
public final class MethodCallRecordClassAdapter extends ClassVisitor {

    private String className;
    private String sdkClassPath = "com/canzhang/method_call_record_lib/MethodRecordSDK";


    MethodCallRecordClassAdapter(final ClassVisitor cv) {
        //注意这里的版本号要留意，不同版本可能会抛出异常，仔细观察异常
        super(ASM6, cv);
    }

    /**
     * 这里可以拿到关于.class的所有信息，比如当前类所实现的接口类表等
     *
     * @param version    表示jdk的版本
     * @param access     当前类的修饰符 （这个和ASM 和 java有些差异，比如public 在这里就是ACC_PUBLIC）
     * @param name       当前类名
     * @param signature  泛型信息
     * @param superName  当前类的父类
     * @param interfaces 当前类实现的接口列表
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }


    /**
     * 这里可以拿到关于method的所有信息，比如方法名，方法的参数描述等
     *
     * @param access     方法的修饰符
     * @param outName    方法名
     * @param desc       方法描述（就是（参数列表）返回值类型拼接）
     * @param signature  泛型相关信息
     * @param exceptions 方法抛出的异常信息
     * @return
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String outName,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, outName, desc, signature, exceptions);
        mv = new AdviceAdapter(ASM6, mv, access, outName, desc) {


            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
//                if("com/canzhang/asmdemo/sdk/MyTest".equals(className)){
//                    LogUtils.log("--------------->>>>>\n\nopcode(操作码):" + opcode + "\n\nowner:" + owner + "\n\nname（:" + name + "\n\ndesc:" + desc + "\n\noutMethodName（上层类名_方法名）:" +className+"_"+ outName);
//
//                }
                if (opcode == Opcodes.GETSTATIC && "android/os/Build".equals(owner)) {
                    //加载一个常量
                    mv.visitLdcInsn(className + "_" + outName + "_load: fieldName:" + name + " fieldDesc:" + desc + " fieldOwner:" + owner);
                    //调用我们自定义的方法 (注意用/,不是.; 方法描述记得；也要)
                    mv.visitMethodInsn(INVOKESTATIC, sdkClassPath, "recordLoadFiled", "(Ljava/lang/String;)V", false);
                }
                super.visitFieldInsn(opcode, owner, name, desc);


            }

            /**
             * 访问调用方法的指令（这里仅针对调用方法的指令，其他指令还有返回指令，异常抛出指令一类的）
             * @param opcode 指令
             * @param owner  指令所调用的方法归属的类
             * @param name   方法名
             * @param descriptor 方法描述（就是（参数列表）返回值类型拼接）
             * @param isInterface 是否接口
             */
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                if ("getInstalledApplications".equals(name)) {
                    LogUtils.log("--------------->>>>>\n\nopcode(操作码):" + opcode + "\n\nowner(归属类):" + owner + "\n\nname（方法名）:" + name + "\n\ndescriptor（方法描述符）:" + descriptor + "\n\nisInterface（是否接口）:" + isInterface + "\n\noutMethodName（上层类名_方法名）:" + className + "_" + outName);
                }
//                ApplicationPackageManager
                if (opcode == Opcodes.INVOKEVIRTUAL) {//调用实例方法
                    //归属类、方法名、方法描述（返回值、入参类型）
                    String recordMethodName = null;
                    if ("android/telephony/TelephonyManager".equals(owner) && name.equals("getLine1Number") && descriptor.equalsIgnoreCase("()Ljava/lang/String;")) {
                        recordMethodName = "getLine1Number";
                    }

                    if ("android/telephony/TelephonyManager".equals(owner) && name.equals("getDeviceId") && descriptor.equalsIgnoreCase("()Ljava/lang/String;")) {
                        recordMethodName = "getDeviceId";
                    }

                    if ("android/telephony/TelephonyManager".equals(owner) && name.equals("getSimSerialNumber") && descriptor.equalsIgnoreCase("()Ljava/lang/String;")) {
                        recordMethodName = "getSimSerialNumber";
                    }

                    if ("android/telephony/TelephonyManager".equals(owner) && name.equals("getSubscriberId") && descriptor.equalsIgnoreCase("()Ljava/lang/String;")) {
                        recordMethodName = "getSubscriberId";
                    }


                    if ("android/net/wifi/WifiInfo".equals(owner) && name.equals("getMacAddress") && descriptor.equalsIgnoreCase("()Ljava/lang/String;")) {
                        recordMethodName = "getMacAddress";
                    }

                    //阀门把这个api 认为是获取用户mac地址
                    if ("java/net/NetworkInterface".equals(owner) && name.equals("getInetAddresses") && descriptor.equalsIgnoreCase("()Ljava/util/Enumeration;")) {
                        recordMethodName = "getInetAddresses";
                    }

                    if ("java/net/InetAddress".equals(owner) && name.equals("getHostAddress") && descriptor.equalsIgnoreCase("()Ljava/lang/String;")) {
                        recordMethodName = "getHostAddress";
                    }

                    //阀门把下面的api 都认为是获取已安装应用列表（
                    // 1.queryIntentActivities
                    //2.getInstalledPackages
                    //3.getInstalledApplications
                    //4.getRunningServices
                    //5.getRunningAppProcesses
                    //6.pm pm list package （这个是命令行 暂未添加监控））
                    if ("android/content/pm/PackageManager".equals(owner) && name.equals("getInstalledPackages") && descriptor.equalsIgnoreCase("(I)Ljava/util/List;")) {
                        recordMethodName = "getInstalledPackages";
                    }

                    if ("android/content/pm/PackageManager".equals(owner) && name.equals("getInstalledApplications") && descriptor.equalsIgnoreCase("(I)Ljava/util/List;")) {
                        recordMethodName = "getInstalledApplications";
                    }

                    if ("android/app/ActivityManager".equals(owner) && name.equals("getRunningServices") && descriptor.equalsIgnoreCase("(I)Ljava/util/List;")) {
                        recordMethodName = "getRunningServices";
                    }
                    if ("android/app/ActivityManager".equals(owner) && name.equals("getRunningAppProcesses") && descriptor.equalsIgnoreCase("()Ljava/util/List;")) {
                        recordMethodName = "getRunningAppProcesses";
                    }

                    if ("android/content/pm/PackageManager".equals(owner) && name.equals("queryIntentActivities") && descriptor.equalsIgnoreCase("(Landroid/content/Intent;I)Ljava/util/List;")) {
                        recordMethodName = "queryIntentActivities";
                    }


                    if (recordMethodName != null) {
                        //加载一个常量
                        mv.visitLdcInsn(className + "_" + outName + "_call:" + recordMethodName);
                        //调用我们自定义的方法 (注意用/,不是.; 方法描述记得；也要)
                        mv.visitMethodInsn(INVOKESTATIC, sdkClassPath, "recordMethodCall", "(Ljava/lang/String;)V", false);
                    }


                }
                if (opcode == Opcodes.INVOKESTATIC) {//调用静态方法

                    if (!isSdkPath() && ("android/provider/Settings$System".equals(owner) || "android/provider/Settings$Secure".equals(owner)) && name.equals("getString") && descriptor.equalsIgnoreCase("(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;")) {
                        //变更父类
                        super.visitMethodInsn(opcode, sdkClassPath, name, descriptor, isInterface);
                        return;
                    }


                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        };
        return mv;

    }

    private boolean isSdkPath() {
        return sdkClassPath.equals(className);
    }


}