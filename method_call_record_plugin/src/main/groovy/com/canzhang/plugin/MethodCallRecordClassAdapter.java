package com.canzhang.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * ClassVisitor:主要负责遍历类的信息，包括类上的注解、构造方法、字段等等。
 */
public final class MethodCallRecordClassAdapter extends ClassVisitor {

    private String className;


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
     * @param outName       方法名
     * @param desc       方法签名（就是（参数列表）返回值类型拼接）
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
            public void visitInsn(int opcode) {
                super.visitInsn(opcode);
            }

            //访问调用方法的指令
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {

                if ("getLine1Number".equals(name)) {
                    LogUtils.log("--------------->>>>>\n\nopcode(操作码):" + opcode + "\n\nowner(归属类):" + owner + "\n\nname（方法名）:" + name + "\n\ndescriptor（方法描述符）:" + descriptor + "\n\nisInterface（是否接口）:" + isInterface + "\n\noutMethodName（上层方法名）:" + outName);
                }
                if (opcode == Opcodes.INVOKEVIRTUAL) {//调用实例方法
                    //归属类、方法名、方法描述（返回值、入参类型）
                    if ("android/telephony/TelephonyManager".equals(owner)&& name.equals("getLine1Number") && descriptor.equalsIgnoreCase("()Ljava/lang/String;")) {
                        //加载一个常量
                        mv.visitLdcInsn(className+"_"+outName+"_call:getLine1Number");
                        //调用我们自定义的方法 (注意用/,不是.; 方法描述记得；也要)
                        mv.visitMethodInsn(INVOKESTATIC, "com/canzhang/asmdemo/sdk/MethodRecordSDK", "recordMethodCall", "(Ljava/lang/String;)V", false);
                    }
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

        };
        return mv;

    }


}