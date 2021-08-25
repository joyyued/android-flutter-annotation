package com.joyy.compiler.processor.manager

import javax.annotation.processing.ProcessingEnvironment

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/25 4:30 下午
 * @email: 56002982@qq.com
 * @des: Flutter Engine Utils
 */
class FlutterEngineUtilsProcessor(
    private val processingEnv: ProcessingEnvironment
) {

    private val filer = processingEnv.filer
    private val elementUtils = processingEnv.elementUtils
    private val typeUtils = processingEnv.typeUtils
    private val message = processingEnv.messager
    private val options = processingEnv.options
    private val sourceVersion = processingEnv.sourceVersion
    private val locale = processingEnv.locale

    fun a() {
    }
}