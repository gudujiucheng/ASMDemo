package com.canzhang.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
        final AtomicBoolean isInvokeLoadLibrary = new AtomicBoolean(false);
        List<String> mLdcList = new ArrayList<>();
        mv = new AdviceAdapter(ASM6, mv, access, outName, desc) {

            @Override
            public void visitLdcInsn(Object cst) {//访问一些常量
                if(cst instanceof  String){
                    mLdcList.add((String) cst);
                }
                super.visitLdcInsn(cst);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
//                if("com/canzhang/asmdemo/sdk/MyTest".equals(className)){
//                    LogUtils.log("--------------->>>>>\n\nopcode(操作码):" + opcode + "\n\nowner:" + owner + "\n\nname（:" + name + "\n\ndesc:" + desc + "\n\noutMethodName（上层类名_方法名）:" +className+"_"+ outName);
//                }
//                if (opcode == Opcodes.GETSTATIC && "android/os/Build".equals(owner)) {
//                    //加载一个常量
//                    mv.visitLdcInsn(className + "_" + outName + "_load: fieldName:" + name + " fieldDesc:" + desc + " fieldOwner:" + owner);
//                    //调用我们自定义的方法 (注意用/,不是.; 方法描述记得；也要)
//                    mv.visitMethodInsn(INVOKESTATIC, sdkClassPath, "recordLoadFiled", "(Ljava/lang/String;)V", false);
//                }
                super.visitFieldInsn(opcode, owner, name, desc);


            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter();
                //打印方法信息
                if (MethodCallRecordExtension.methodTest != null && MethodCallRecordExtension.methodTest.contains(outName)) {
                    LogUtils.log("----------测试打印数据---form 方法进入 -->>>>>"
                            + "\n\naccess（方法修饰符）:" + access
                            + "\n\noutName（方法名）:" + outName
                            + "\n\ndesc（方法描述（就是（参数列表）返回值类型拼接））:" + desc
                            + "\n\nsignature（方法泛型信息：）:" + signature
                            + "\n\nclassName（当前扫描的类名）:" + className);
                }
                //模糊匹配方法（忽略方法归属的类名）
                if (MethodCallRecordExtension.fuzzyMethodMap != null
                        && MethodCallRecordExtension.fuzzyMethodMap.containsKey(outName)
                        && MethodCallRecordExtension.fuzzyMethodMap.get(outName)!=null) {

                    if(MethodCallRecordExtension.fuzzyMethodMap.get(outName).size()>0){//有配置，就按照配置来匹配
                        for (String item: MethodCallRecordExtension.fuzzyMethodMap.get(outName)) {
                            if(item!=null&&item.equals(desc)){
                                //命中，则插桩
                                inputMethod(outName);
                                break;
                            }

                        }
                    }else{//没有配置就通配
                        //命中，则插桩
                        inputMethod(outName);
                    }

                }
            }

            @Override
            protected void onMethodExit(int opcode) {
                super.onMethodExit(opcode);
                if(isInvokeLoadLibrary.get() &&mLdcList.size()>0){
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("\n\n发现方法调用 loadLibrary  className（当前扫描的类名）:" + className);
                    stringBuilder.append("\n------方法体加载的常量 开始--------\n");
                    for (String item :mLdcList) {
                        stringBuilder.append(item).append("\n");
                    }
                    stringBuilder.append("------方法体加载的常量 结束--------");
                    LogUtils.log(stringBuilder
                            + "\naccess（方法修饰符）:" + access
                            + "\noutName（方法名）:" + outName
                            + "\ndesc（方法描述（就是（参数列表）返回值类型拼接））:" + desc
                            + "\nsignature（方法泛型信息：）:" + signature
                            + "\nclassName（当前扫描的类名）:" + className+"\n\n");
                }
            }

            /**
             * 访问调用方法的指令（这里仅针对调用方法的指令，其他指令还有返回指令，异常抛出指令一类的） 像接口回调这一类的是调用不到的（因为回调的点是系统api，这里捕获不到）
             * @param opcode 指令
             * @param owner  指令所调用的方法归属的类
             * @param name   方法名
             * @param descriptor 方法描述（就是（参数列表）返回值类型拼接）
             * @param isInterface 是否接口
             */
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                //打印方法信息
                if (MethodCallRecordExtension.methodTest != null && MethodCallRecordExtension.methodTest.contains(name)) {
                    LogUtils.log("----------测试打印数据---方法调用（与onMethodEnter 可能存在重复打印） -->>>>>"
                            + "\n\nopcode（方法调用指令）:" + opcode
                            + "\n\nowner（方法归属类）:" + owner
                            + "\n\naccess（方法修饰符）:" + access
                            + "\n\nname（方法名）:" + name
                            + "\n\nisInterface（是否接口方法）:" + isInterface
                            + "\n\ndescriptor（方法描述（就是（参数列表）返回值类型拼接））:" + descriptor
                            + "\n\nsignature（方法泛型信息：）:" + signature
                            + "\n\nclassName（当前扫描的类名）:" + className);
                }
                if("java/lang/System".equals(owner)&&"loadLibrary".equals(name)&&"(Ljava/lang/String;)V".equals(descriptor)){
                    isInvokeLoadLibrary.set(true);
                }

                if (MethodCallRecordExtension.accurateMethodMap != null
                        && MethodCallRecordExtension.accurateMethodMap.containsKey(owner)
                        && MethodCallRecordExtension.accurateMethodMap.get(owner) != null
                        && MethodCallRecordExtension.accurateMethodMap.get(owner).size() > 0) {
                    for (String item: MethodCallRecordExtension.accurateMethodMap.get(owner)) {
                        if(item!=null&&item.equals(name+descriptor)){
                            //命中，则插桩
                            inputMethod(name);
                            break;
                        }

                    }
                }

//                if (opcode == Opcodes.INVOKESTATIC) {//调用静态方法
//
//                    if (!isSdkPath() && ("android/provider/Settings$System".equals(owner) || "android/provider/Settings$Secure".equals(owner)) && name.equals("getString") && descriptor.equalsIgnoreCase("(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;")) {
//                        //变更父类
//                        super.visitMethodInsn(opcode, sdkClassPath, name, descriptor, isInterface);
//                        return;
//                    }
//                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            private void inputMethod(String recordMethodName) {
                if (!isSdkPath() && recordMethodName != null) {
//                    LogUtils.log("----------命中----->>>"+className + "_" + outName + "_call:" + recordMethodName);
                    //加载一个常量
                    mv.visitLdcInsn(className + "_" + outName + "_call:" + recordMethodName);
                    //调用我们自定义的方法 (注意用/,不是.; 方法描述记得；也要)
                    mv.visitMethodInsn(INVOKESTATIC, sdkClassPath, "recordMethodCall", "(Ljava/lang/String;)V", false);
                }
            }
        };
        return mv;

    }

    private boolean isSdkPath() {
        return sdkClassPath.equals(className);
    }


}