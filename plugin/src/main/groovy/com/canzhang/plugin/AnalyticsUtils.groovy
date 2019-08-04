package com.canzhang.plugin

import org.objectweb.asm.Opcodes

/**
 * 工具类
 */
class AnalyticsUtils implements Opcodes {


    static void logD(String tips) {
        if (AnalyticsExtension.isShowDebugInfo) {
            println(tips)
        }
    }
}
