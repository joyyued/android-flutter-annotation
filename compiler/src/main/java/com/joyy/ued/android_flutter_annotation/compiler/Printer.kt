package com.joyy.ued.android_flutter_annotation.compiler

import com.joyy.ued.android_flutter_annotation.compiler.config.ClazzConfig
import com.joyy.ued.android_flutter_annotation.compiler.processor.manager.FlutterEngineProcessor
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/30 10:29 上午
 * @email: 56002982@qq.com
 * @des: 打印
 */
interface Printer {
    fun getMessager(): Messager?

    fun note(msg: String) {
        getMessager()?.printMessage(
            Diagnostic.Kind.NOTE,
            "[${ClazzConfig.PROJECT_NAME}-${FlutterEngineProcessor.TAG}] $msg"
        )
    }

    fun warning(msg: String) {
        getMessager()?.printMessage(
            Diagnostic.Kind.WARNING,
            "[${ClazzConfig.PROJECT_NAME}-${FlutterEngineProcessor.TAG}] $msg"
        )
    }

    fun error(msg: String) {
        getMessager()?.printMessage(
            Diagnostic.Kind.ERROR,
            "[${ClazzConfig.PROJECT_NAME}-${FlutterEngineProcessor.TAG}] $msg"
        )
        throw Exception(msg)
    }
}