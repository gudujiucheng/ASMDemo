package com.canzhang.thread_lib.bean

import com.canzhang.thread_lib.bean.ShowInfo

data class ThreadInfoResult(
        var list: List<ShowInfo> = emptyList(),
        var totalNum: Int = 0, // 总线程数
        var singleThreadNum: Int = 0, // 单独线程数量
        var poolNum: Int = 0, // 线程池数量
        var poolThreadNum: Int = 0, // 线程池线程数量
        var unknownNum: Int = 0 // 未知调用栈的线程数
)