package com.canzhang.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;

import static org.objectweb.asm.Opcodes.ASM6;

public final class TryCatchClassAdapter extends ClassVisitor {

    private String className;
    private List<String> tryCatchMethodNameList;


    TryCatchClassAdapter(final ClassVisitor cv, List<String> tryCatchMethodNameList) {
        super(Opcodes.ASM5, cv);
        this.tryCatchMethodNameList = tryCatchMethodNameList;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        System.out.println("访问方法---方法修饰符 :" + access + " 方法名:" + name + " 方法签名:" + desc + " 泛型信息:" + signature);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if(tryCatchMethodNameList==null||!tryCatchMethodNameList.contains(name)){
            System.out.println("未命中方法:"+name);
            return mv;
        }
        System.out.println("命中方法:"+name);
        return new AdviceAdapter(ASM6, mv, access, name, desc) {

            private Label from = new Label(),
                    to = new Label(),
                    target = new Label();
            @Override
            protected void onMethodEnter() {
                //标志：try块开始位置
                visitLabel(from);
                visitTryCatchBlock(from,
                        to,
                        target,
                        "java/lang/Exception");
            }

            @Override
            protected void onMethodExit(int opcode) {

            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                //标志：try块结束
                mv.visitLabel(to);

                //标志：catch块开始位置
                mv.visitLabel(target);
                mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Exception"});

                // 异常信息保存到局部变量
                int local = newLocal(Type.LONG_TYPE);
                mv.visitVarInsn(ASTORE, local);

                // 抛出异常
                mv.visitVarInsn(ALOAD, local);
                mv.visitInsn(ATHROW);
                super.visitMaxs(maxStack, maxLocals);
            }
        };
//        return mv == null ? null : new TryCatchMethodAdapter(className + File.separator + name, access, desc, mv);

    }


}