package com.joyy.neza_compiler

import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.engine.FlutterEngineProcessor
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

    fun error(msg: String) {
        getMessager()?.printMessage(
            Diagnostic.Kind.ERROR,
            "[${ClazzConfig.PROJECT_NAME}-${FlutterEngineProcessor.TAG}] $msg"
        )
    }
}