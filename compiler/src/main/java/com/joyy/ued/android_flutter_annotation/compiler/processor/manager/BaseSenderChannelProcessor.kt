package com.joyy.ued.android_flutter_annotation.compiler.processor.manager

import com.joyy.ued.android_flutter_annotation.compiler.Printer
import com.joyy.ued.android_flutter_annotation.compiler.base.BaseProcessor
import com.joyy.ued.android_flutter_annotation.compiler.config.ClazzConfig
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/26 10:30 上午
 * @email: 56002982@qq.com
 * @des: BaseChannel 生成
 */
class BaseSenderChannelProcessor(
    printer: Printer,
    processingEnv: ProcessingEnvironment,
    roundEnv: RoundEnvironment
) : BaseProcessor(
    printer,
    processingEnv,
    roundEnv
) {
    fun process() {
        printer.note("Base Sender Channel Processor running.")

        val baseChannelClass = TypeSpec.interfaceBuilder(ClazzConfig.BASE_SENDER_CHANNEL_NAME)
            .addFunction(
                FunSpec.builder("release")
                    .addModifiers(KModifier.ABSTRACT)
                    .build()
            )

        generatorClass(ClazzConfig.PACKAGE.BASE_NAME, baseChannelClass.build())
    }
}