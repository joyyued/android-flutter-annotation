package com.joyy.ued.android_flutter_annotation.annotation.common

/**
 * @author: Jiang PengYong
 * @date: 2021/8/30 9:51 上午
 * @email: 56002982@qq.com
 * @des: 发送者——用于改变发送的方法名
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Send(
    val name: String
)