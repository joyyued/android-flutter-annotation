package com.joyy.neza_annotation.method

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/13 3:18 下午
 * @email: 56002982@qq.com
 * @des: flutter method channel 重定义方法名
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Method(val value: String)