package com.canzhang.plugin;
import com.quinn.hunter.transform.asm.BaseWeaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.List;

/**
 * Created by Quinn on 09/07/2017.
 */
public final class TryCatchWeaver extends BaseWeaver {


    private TryCatchExtension tryCatchExtension;
    private List<String> tryCatchMethodNameList;

    @Override
    public void setExtension(Object extension) {
        if(extension == null) return;
        this.tryCatchExtension = (TryCatchExtension) extension;
    }

    /**
     * 判断是否需要拦截处理此class（从transform调用过来的）
     * @param fullQualifiedClassName
     * @return
     */
    @Override
    public boolean isWeavableClass(String fullQualifiedClassName) {
        boolean superResult = super.isWeavableClass(fullQualifiedClassName);
        if(tryCatchExtension != null) {
            //whitelist is prior to to blacklist
            if(!tryCatchExtension.methodMap.isEmpty()) {
                boolean isIn = false;
                for(String key : tryCatchExtension.methodMap.keySet()) {
                    if(fullQualifiedClassName.startsWith(key)) {
                        System.out.println("命中class--------->>>>>>>>>>>>>>>>>>:"+fullQualifiedClassName);
                        isIn = true;
                        tryCatchMethodNameList = tryCatchExtension.methodMap.get(key);
                        break;
                    }
                }
                return superResult && isIn;
            }
        }
        return false;
    }

    @Override
    protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return new TryCatchClassAdapter(classWriter,tryCatchMethodNameList);
    }

}
