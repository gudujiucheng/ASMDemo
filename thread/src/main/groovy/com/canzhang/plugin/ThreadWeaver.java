package com.canzhang.plugin;
import com.quinn.hunter.transform.asm.BaseWeaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.List;

/**
 * Created by Quinn on 09/07/2017.
 */
public final class ThreadWeaver extends BaseWeaver {

    /**
     * 判断是否需要拦截处理此class（从transform调用过来的）
     * @param fullQualifiedClassName
     * @return
     */
    @Override
    public boolean isWeavableClass(String fullQualifiedClassName) {
        return  super.isWeavableClass(fullQualifiedClassName);
    }

    @Override
    protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return new ThreadClassAdapter(classWriter);
    }

}
