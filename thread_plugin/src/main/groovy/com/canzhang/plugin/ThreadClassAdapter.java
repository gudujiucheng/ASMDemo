package com.canzhang.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import static com.canzhang.plugin.ClassConstant.S_TBaseThread;
import static com.canzhang.plugin.ClassConstant.S_Thread;
import static org.objectweb.asm.Opcodes.ASM6;

public final class ThreadClassAdapter extends ClassVisitor {

    private String className;
    private boolean changingSuper = false; // 是否处于改继承状态

    ThreadClassAdapter(final ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        if (!isSdkPath()) {
            if (S_Thread.equals(superName)) {
                System.out.println("命中 superName: " + superName + " className:" + name);
                changingSuper = true;
                //这里直接改输入的这个superName，就可以把继承替换掉了，比较方便
                super.visit(version, access, name, signature, S_TBaseThread, interfaces);
                return;
            }
        }
//        System.out.println("未命中 superName: " + superName + " className:" + name);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * 过滤替换类主路径的类，防止被插桩
     * @return
     */
    private boolean isSdkPath() {
        return className.contains("com/canzhang/asmdemo/thread/sdk");
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
//        System.out.println("访问方法---方法修饰符 :" + access + " 方法名:" + name + " 方法签名:" + desc + " 泛型信息:" + signature);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (isSdkPath()) {
            return mv;
        }
        if (changingSuper) {//替换了集成类之后，还需要修改对应的构造函数
            mv = new AdviceAdapter(ASM6, mv, access, name, desc) {

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if (name.equalsIgnoreCase("<init>")) {
                        switch (owner) {//这里要调整够赞函数所调用的方法为新基类的方法，不然调用的还是Thread的构造函数，而不是BaseThread的构造函数。
                            case S_Thread:
                                System.out.println("changingSuper className: " + className + " owner：" + owner + " name：" + name + " opcode：" + opcode);
                                mv.visitMethodInsn(opcode, S_TBaseThread, name, descriptor, false);
                                return;
                        }
                    }
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }


            };
        } else {
            mv = new AdviceAdapter(ASM6, mv, access, name, desc) {
                @Override
                public void visitTypeInsn(int opcode, String type) {
                    //修改直接new 的 Thread，改为直接new我们的构造类
                    if (opcode == Opcodes.NEW) {
                        switch (type) {
                            case S_Thread:
                                mv.visitTypeInsn(Opcodes.NEW, S_TBaseThread);
                                return;
                        }

                    }
                    super.visitTypeInsn(opcode, type);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    //这里要调整够赞函数所调用的方法为新基类的方法，不然调用的还是Thread的构造函数，而不是BaseThread的构造函数。
                    //这里就是改了一下ower
                    if (owner.equals(S_Thread) && name.equalsIgnoreCase("<init>")) {
                        mv.visitMethodInsn(opcode, S_TBaseThread, name, descriptor, false);
                        return;
                    }
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            };
        }
        return mv;

    }


}