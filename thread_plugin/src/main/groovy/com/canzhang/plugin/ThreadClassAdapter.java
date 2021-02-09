package com.canzhang.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import static com.canzhang.plugin.ClassConstant.S_Executors;
import static com.canzhang.plugin.ClassConstant.S_HandlerThread;
import static com.canzhang.plugin.ClassConstant.S_ProxyExecutors;
import static com.canzhang.plugin.ClassConstant.S_ScheduledThreadPoolExecutor;
import static com.canzhang.plugin.ClassConstant.S_TBaseHandlerThread;
import static com.canzhang.plugin.ClassConstant.S_TBaseScheduledThreadPoolExecutor;
import static com.canzhang.plugin.ClassConstant.S_TBaseThread;
import static com.canzhang.plugin.ClassConstant.S_TBaseThreadPoolExecutor;
import static com.canzhang.plugin.ClassConstant.S_TBaseTimer;
import static com.canzhang.plugin.ClassConstant.S_Thread;
import static com.canzhang.plugin.ClassConstant.S_ThreadPoolExecutor;
import static com.canzhang.plugin.ClassConstant.S_Timer;
import static org.objectweb.asm.Opcodes.ASM6;

public final class ThreadClassAdapter extends ClassVisitor {

    private String className;


    ThreadClassAdapter(final ClassVisitor cv) {
        //注意这里的版本号要留意，不同版本可能会抛出异常，仔细观察异常
        super(ASM6, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        String tempSuperName = null;
        if (!isSdkPath()&&superName!=null) {
            switch (superName) {//如果命中我们特定的类，则替换为我们的类
                case S_Thread:
                    tempSuperName = S_TBaseThread;
                    break;
                case S_ThreadPoolExecutor:
                    tempSuperName = S_TBaseThreadPoolExecutor;
                    break;
                case S_ScheduledThreadPoolExecutor:
                    tempSuperName = S_TBaseScheduledThreadPoolExecutor;
                    break;
                case S_Timer:
                    tempSuperName = S_TBaseTimer;
                    break;
                case S_HandlerThread:
                    tempSuperName = S_TBaseHandlerThread;
                    break;
            }
        }
        if (tempSuperName != null) {
            LogUtils.log("命中父类，修改父类：\nclassName:" + className + "\nsuperName:" + superName + "\n更换父类为:" + tempSuperName);
        }
        if(superName==null){
            LogUtils.log("\n----------\n----------\n----------\n----------\n\n没有父类这是什么类：\nclassName:" + className +"\n----------\n----------\n----------\n----------\n\n");
        }
        super.visit(version, access, name, signature, tempSuperName == null ? superName : tempSuperName, interfaces);
    }

    /**
     * 过滤替换类主路径的类，防止被插桩
     *
     * @return
     */
    private boolean isSdkPath() {
        return className.contains("com/canzhang/thread_lib");
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
//        System.out.println("访问方法---方法修饰符 :" + access + " 方法名:" + name + " 方法签名:" + desc + " 泛型信息:" + signature);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (isSdkPath()) {
            return mv;
        }
        mv = new AdviceAdapter(ASM6, mv, access, name, desc) {

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                String tempOwner = null;
                if (name.equalsIgnoreCase("<init>")) {//替换了集成类之后，还需要修改对应的构造函数
                    switch (owner) {//这里要调整够赞函数所调用的方法为新基类的方法，不然调用的还是Thread的构造函数，而不是BaseThread的构造函数。
                        case S_Thread:
                            tempOwner = S_TBaseThread;
                            break;
                        case S_ThreadPoolExecutor:
                            tempOwner = S_TBaseThreadPoolExecutor;
                            break;
                        case S_ScheduledThreadPoolExecutor:
                            tempOwner = S_TBaseScheduledThreadPoolExecutor;
                            break;
                        case S_Timer:
                            tempOwner = S_TBaseTimer;
                            break;
                        case S_HandlerThread:
                            tempOwner = S_TBaseHandlerThread;
                            break;
                    }
                } else if (opcode == Opcodes.INVOKESTATIC && owner.equals(S_Executors)) {//替换线程池实现
                    if ((name.equals("newFixedThreadPool") && descriptor.equalsIgnoreCase("(I)Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("newWorkStealingPool") && descriptor.equalsIgnoreCase("(I)Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("newWorkStealingPool") && descriptor.equalsIgnoreCase("()Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("newFixedThreadPool") && descriptor.equalsIgnoreCase("(ILjava/util/concurrent/ThreadFactory;)Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("newSingleThreadExecutor") && descriptor.equalsIgnoreCase("()Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("newSingleThreadExecutor") && descriptor.equalsIgnoreCase("(Ljava/util/concurrent/ThreadFactory;)Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("newCachedThreadPool") && descriptor.equalsIgnoreCase("()Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("newCachedThreadPool") && descriptor.equalsIgnoreCase("(Ljava/util/concurrent/ThreadFactory;)Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("newSingleThreadScheduledExecutor") && descriptor.equalsIgnoreCase("()Ljava/util/concurrent/ScheduledExecutorService;"))

                            || (name.equals("newSingleThreadScheduledExecutor") && descriptor.equalsIgnoreCase("(Ljava/util/concurrent/ThreadFactory;)Ljava/util/concurrent/ScheduledExecutorService;"))

                            || (name.equals("newScheduledThreadPool") && descriptor.equalsIgnoreCase("(I)Ljava/util/concurrent/ScheduledExecutorService;"))

                            || (name.equals("newScheduledThreadPool") && descriptor.equalsIgnoreCase("(ILjava/util/concurrent/ThreadFactory;)Ljava/util/concurrent/ScheduledExecutorService;"))

                            || (name.equals("unconfigurableExecutorService") && descriptor.equalsIgnoreCase("(Ljava/util/concurrent/ExecutorService;)Ljava/util/concurrent/ExecutorService;"))

                            || (name.equals("unconfigurableScheduledExecutorService") && descriptor.equalsIgnoreCase("(Ljava/util/concurrent/ScheduledExecutorService;)Ljava/util/concurrent/ScheduledExecutorService;"))

                    ) {
                        tempOwner = S_ProxyExecutors;
                    }
                }
                if (tempOwner != null) {
                    LogUtils.log("调整为调用新父类的构造函数: \nclassName:" + className + "\n原归属类:" + owner + "\n新归属类:" + tempOwner);
                }

                super.visitMethodInsn(opcode, tempOwner == null ? owner : tempOwner, name, descriptor, isInterface);
            }


            @Override
            public void visitTypeInsn(int opcode, String type) {
                //修改直接new的线程相关对象，改为直接new我们的构造类
                String tempNewType = null;
                if (opcode == Opcodes.NEW) {
                    switch (type) {
                        case S_Thread:
                            tempNewType = S_TBaseThread;
                            break;
                        case S_ThreadPoolExecutor:
                            tempNewType = S_TBaseThreadPoolExecutor;
                            break;
                        case S_ScheduledThreadPoolExecutor:
                            tempNewType = S_TBaseScheduledThreadPoolExecutor;
                            break;
                        case S_Timer:
                            tempNewType = S_TBaseTimer;
                            break;
                        case S_HandlerThread:
                            tempNewType = S_TBaseHandlerThread;
                            break;
                    }
                }
                if (tempNewType != null) {
                    LogUtils.log("命中new 对象事件：\nclassName：" + className + "\n原类型：" + type + "\n更换为：" + tempNewType);
                }
                super.visitTypeInsn(opcode, tempNewType == null ? type : tempNewType);
            }
        };
        return mv;

    }


}