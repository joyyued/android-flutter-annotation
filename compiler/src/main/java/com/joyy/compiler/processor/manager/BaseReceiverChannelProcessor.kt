package com.joyy.compiler.processor.manager

import com.joyy.compiler.Printer
import com.joyy.compiler.base.BaseProcessor
import com.joyy.compiler.config.ClazzConfig
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
class BaseReceiverChannelProcessor(
    printer: Printer,
    processingEnv: ProcessingEnvironment,
    roundEnv: RoundEnvironment
) : BaseProcessor(
    printer,
    processingEnv,
    roundEnv
) {
    fun process() {
        printer.note("Base Receiver Channel Processor running.")

        val baseChannelClass = TypeSpec.interfaceBuilder(ClazzConfig.BASE_RECEIVER_CHANNEL_NAME)
            .addFunction(
                FunSpec.builder("getEngineId")
                    .returns(String::class)
                    .addModifiers(KModifier.ABSTRACT)
                    .build()
            )
            .addFunction(
                FunSpec.builder("getChannelName")
                    .returns(String::class)
                    .addModifiers(KModifier.ABSTRACT)
                    .build()
            )
            .addFunction(
                FunSpec.builder("release")
                    .addModifiers(KModifier.ABSTRACT)
                    .build()
            )

        generatorClass(ClazzConfig.PACKAGE.BASE_NAME, baseChannelClass.build())
    }
}