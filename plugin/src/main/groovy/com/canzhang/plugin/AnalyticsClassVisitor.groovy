package com.canzhang.plugin

import org.objectweb.asm.*

/**
 * 使用ASM的ClassReader类读取.class的字节数据，并加载类，
 * 然后用自定义的ClassVisitor，进行修改符合特定条件的方法，
 * 最后返回修改后的字节数组
 */
class AnalyticsClassVisitor extends ClassVisitor implements Opcodes {

//插入的外部类具体路径
    private String[] mInterfaces
    private ClassVisitor classVisitor
    private String mCurrentClassName

    AnalyticsClassVisitor(final ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
        this.classVisitor = classVisitor
    }

    private
    static void visitMethodWithLoadedParams(MethodVisitor methodVisitor, int opcode, String owner, String methodName, String methodDesc, int start, int count, List<Integer> paramOpcodes) {
        for (int i = start; i < start + count; i++) {
            methodVisitor.visitVarInsn(paramOpcodes[i - start], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDesc, false)
    }

    /**
     * 这里可以拿到关于.class的所有信息，比如当前类所实现的接口类表等
     * @param version 表示jdk的版本
     * @param access 当前类的修饰符 （这个和ASM 和 java有些差异，比如public 在这里就是ACC_PUBLIC）
     * @param name 当前类名
     * @param signature 泛型信息
     * @param superName 当前类的父类
     * @param interfaces 当前类实现的接口列表
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mInterfaces = interfaces
        mCurrentClassName = name

        AnalyticsUtils.logD("当前的类是：" + name)
        AnalyticsUtils.logD("当前类实现的接口有：" + mInterfaces)
    }

    /**
     * 这里可以拿到关于method的所有信息，比如方法名，方法的参数描述等
     * @param access 方法的修饰符
     * @param name 方法名
     * @param desc 方法签名（就是（参数列表）返回值类型拼接）
     * @param signature 泛型相关信息
     * @param exceptions 方法抛出的异常信息
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)

        String nameDesc = name + desc

        methodVisitor = new AnalyticsDefaultMethodVisitor(methodVisitor, access, name, desc) {

            @Override
            void visitEnd() {
                super.visitEnd()
            }

            @Override
            void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
                super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs)
            }

            @Override
            protected void onMethodExit(int opcode) {//方法退出节点
                super.onMethodExit(opcode)
            }

            @Override
            protected void onMethodEnter() {//方法进入节点
                super.onMethodEnter()

                if ((mInterfaces != null && mInterfaces.length > 0)) {
                    //如果当前类实现的接口有View$OnClickListener，并且当前进入的方法是onClick(Landroid/view/View;)V
                    //这里如果不知道怎么写，可以写个demo打印一下，就很快知道了，这里涉及一些ASM和Java中不同的写法。
                    if ((mInterfaces.contains('android/view/View$OnClickListener') && nameDesc == 'onClick(Landroid/view/View;)V')) {
                        AnalyticsUtils.logD("命中插桩------>>>>：OnClickListener nameDesc:" + nameDesc + " currentClassName:" + mCurrentClassName)

                        //这里就是插代码逻辑了
                        methodVisitor.visitVarInsn(ALOAD, 1)//从局部变量表的相应位置装载一个对象引用到操作数栈的栈顶
                        methodVisitor.visitMethodInsn(INVOKESTATIC, "com/canzhang/asmdemo/sdk/MySdk", "onViewClick", "(Landroid/view/View;)V", false)
                    }

                    /**
                     *  匿名内部类可以使你的代码更加简洁，你可以在定义一个类的同时对其进行实例化。
                     *  它与局部类很相似，不同的是它没有类名，如果某个局部类你只需要用一次，那么你就可以使用匿名内部类
                     *
                     *  new View.OnClickListener() {onClick(View view)}  就是匿名内部类，实现了View.OnClickListener接口
                     */

                }
            }

            @Override
            AnnotationVisitor visitAnnotation(String s, boolean b) {
                return super.visitAnnotation(s, b)
            }
        }
        return methodVisitor
    }
}