package com.joyy.ued.android_flutter_annotation.annotation.common

/**
 * @author: Jiang PengYong
 * @date: 2021/8/13 3:18 下午
 * @email: 56002982@qq.com
 * @des: 接受者——用于标记处理接收到信息的方法
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Receive(
    val name: String = ""
)