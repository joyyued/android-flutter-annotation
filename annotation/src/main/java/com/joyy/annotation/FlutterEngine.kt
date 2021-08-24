package com.joyy.annotation

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/13 3:17 下午
 * @email: 56002982@qq.com
 * @des: flutter engine
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class FlutterEngine(val engineId: String)