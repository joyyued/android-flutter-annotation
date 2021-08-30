package com.joyy.ued.android_flutter_annotation.compiler.base

import com.joyy.ued.android_flutter_annotation.compiler.Printer
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

/**
 * @author: Jiang PengYong
 * @date: 2021/8/26 10:29 上午
 * @email: 56002982@qq.com
 * @des: 处理器基类
 */
abstract class BaseProcessor(
    protected val printer: Printer,
    protected val processingEnv: ProcessingEnvironment,
    protected val roundEnv: RoundEnvironment
) {
    protected val filer = processingEnv.filer
    protected val elementUtils = processingEnv.elementUtils
    protected val typeUtils = processingEnv.typeUtils
    protected val message = processingEnv.messager
    protected val options = processingEnv.options
    protected val sourceVersion = processingEnv.sourceVersion
    protected val locale = processingEnv.locale

    fun generatorClass(packagePath: String, typeSpec: TypeSpec) {
        FileSpec.get(packagePath, typeSpec).writeTo(filer)
    }
}
