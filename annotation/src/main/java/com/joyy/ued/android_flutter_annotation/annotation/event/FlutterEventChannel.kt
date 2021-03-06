package com.joyy.ued.android_flutter_annotation.annotation.event

/**
 * @author: Jiang PengYong
 * @date: 2021/8/13 3:18 下午
 * @email: 56002982@qq.com
 * @des: flutter event channel 的注解
 */
@Target( AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class FlutterEventChannel(val channelName: String)