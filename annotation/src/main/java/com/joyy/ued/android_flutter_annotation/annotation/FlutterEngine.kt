package com.joyy.ued.android_flutter_annotation.annotation

/**
 * @author: Jiang PengYong
 * @date: 2021/8/13 3:17 下午
 * @email: 56002982@qq.com
 * @des: 用于标记 flutter engine ，框架会使用此处的 engine id 作为默认 id
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class FlutterEngine(
    val engineId: String
)