package com.canzhang.plugin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

public final class TryCatchMethodAdapter extends LocalVariablesSorter implements Opcodes {

    private Label l1;

    private String methodName;

    public TryCatchMethodAdapter(String name, int access, String desc, MethodVisitor mv) {
        super(Opcodes.ASM5, access, desc, mv);
        this.methodName = name.replace("/", ".");
        System.out.println("methodName:" + methodName + "\n\n");
    }

    @Override
    public void visitCode() {
        super.visitCode();
        /**
         * A label represents a position in the bytecode of a method. Labels are used
         * for jump, goto, and switch instructions, and for try catch blocks. A label
         * designates the <i>instruction</i> that is just after. Note however that there
         * can be other elements between a label and the instruction it designates (such
         * as other labels, stack map frames, line numbers, etc.).
         *
         * 标签是用于标示字节码在方法中的位置，可以用于跳转、转到和切换命令，也可以用于异常捕获块。
         *
         * 标签指定的指令紧随其后，但是其他的元素也可以在这个标签和它标示的指定之间，比如其他标签，堆栈，行号等等。
         */
        Label l0 = new Label();//标记异常处理程序作用域的开头（含）
        l1 = new Label();//标记异常作用域的结尾（不含）
        Label l2 = new Label();//标记异常作用域代码的开头位置
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");//最后这个参数是表名需要捕获的异常
        mv.visitLabel(l0);//访问标签对应的指令 就是异常作用域的开头
    }

    @Override
    public void visitInsn(int opcode) {
        mv.visitLabel(l1);//访问异常作用域的结尾
        Label l4 = new Label();
        mv.visitJumpInsn(GOTO, l4);
        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Exception"});
        mv.visitVarInsn(ASTORE, 3);
        mv.visitLabel(l4);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        super.visitInsn(opcode);
    }

}
