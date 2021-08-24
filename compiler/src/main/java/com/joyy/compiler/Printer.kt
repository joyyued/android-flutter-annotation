package com.joyy.compiler

import com.joyy.compiler.config.ClazzConfig
import com.joyy.compiler.processor.engine.FlutterEngineProcessor
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

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