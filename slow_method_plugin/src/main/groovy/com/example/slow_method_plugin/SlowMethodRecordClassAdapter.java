package com.example.slow_method_plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * ClassVisitor:主要负责遍历类的信息，包括类上的注解、构造方法、字段等等。
 */
public final class SlowMethodRecordClassAdapter extends ClassVisitor {

    private String className;
    private String sdkClassPath = "com/canzhang/slow_method_lib";
    //是否是插桩lib 接口实现，避免陷入死循环
    private boolean isBlockImpl = false;

    SlowMethodRecordClassAdapter(final ClassVisitor cv) {
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
        this.isBlockImpl = Arrays.toString(interfaces).contains("com/canzhang/slow_method_lib/IBlockHandler");
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
        if (isBlockImpl || isSdkPath()) {//避免插入sdk实现导致陷入死循环
            return mv;
        } else {
            return new LocalVariablesSorter(Opcodes.ASM6,access, desc, mv) {//注意这里要用四个参数的构造，否则内部可能会抛出异常
                private int startVarIndex;
                @Override
                public void visitCode() {
                    super.visitCode();
                    //调用获取时间戳，并存储到局部变量表（LSTORE）
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                    startVarIndex = newLocal(Type.LONG_TYPE);
                    mv.visitVarInsn(LSTORE, startVarIndex);
                }

                @Override
                public void visitInsn(int opcode) {
                    //遇到返回或者异常抛出指令则插入耗时统计代码。
                    if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                        mv.visitVarInsn(LLOAD, startVarIndex);
                        mv.visitInsn(LSUB);
                        int index = newLocal(Type.LONG_TYPE);
                        mv.visitVarInsn(LSTORE, index);
                        mv.visitLdcInsn(className+"_"+outName);
                        mv.visitVarInsn(LLOAD, index);
                        mv.visitMethodInsn(INVOKESTATIC, "com/canzhang/slow_method_lib/BlockManager", "timingMethod", "(Ljava/lang/String;J)V", false);
                    }
                    super.visitInsn(opcode);
                }

            };
        }

    }

    private boolean isSdkPath() {
        return className.startsWith(sdkClassPath);
    }


}