package com.joyy.ued.android_flutter_annotation.compiler.processor.manager

import com.joyy.ued.android_flutter_annotation.compiler.Printer
import com.joyy.ued.android_flutter_annotation.compiler.base.BaseProcessor
import com.joyy.ued.android_flutter_annotation.compiler.config.ClazzConfig
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/25 4:30 下午
 * @email: 56002982@qq.com
 * @des: Flutter Engine Utils
 */
class FlutterEngineUtilsProcessor(
    printer: Printer,
    processingEnv: ProcessingEnvironment,
    roundEnv: RoundEnvironment
) : BaseProcessor(
    printer,
    processingEnv,
    roundEnv
) {

    private val contextClassName = ClassName(
        ClazzConfig.Android.CONTEXT_PACKAGE,
        ClazzConfig.Android.CONTEXT_NAME
    )
    private val engineCacheClassName = ClassName(
        ClazzConfig.Flutter.ENGINE_PACKAGE,
        ClazzConfig.Flutter.ENGINE_CACHE_NAME
    )
    private val engineClassName = ClassName(
        ClazzConfig.Flutter.ENGINE_PACKAGE,
        ClazzConfig.Flutter.ENGINE_NAME
    )
    private val dartExecutorClassName = ClassName(
        ClazzConfig.Flutter.DART_EXECUTOR_PACKAGE,
        ClazzConfig.Flutter.DART_EXECUTOR_NAME
    )

    fun process() {
        printer.note("Engine Utils running.")

        val engineCreatorClazz = TypeSpec.objectBuilder(ClazzConfig.ENGINE_UTILS_NAME)
            .addFunction(createEngineListFunction())
            .addFunction(createEngineFunction())
            .addFunction(removeEngineListFunction())
            .addFunction(removeEngineFunction())
            .addFunction(getEngineFunction())
            .build()

        generatorClass(ClazzConfig.PACKAGE.ENGINE_NAME, engineCreatorClazz)
    }

    private fun createEngineListFunction(): FunSpec {
        return FunSpec.builder("createEngine")
            .addParameter("context", contextClassName)
            .addParameter(
                "engineIdList",
                List::class.parameterizedBy(
                    String::class
                )
            )
            .beginControlFlow("for (engineId in engineIdList)")
            .addStatement("createEngine(context, engineId)")
            .endControlFlow()
            .build()
    }

    private fun createEngineFunction(): FunSpec {
        return FunSpec.builder("createEngine")
            .addParameter("context", contextClassName)
            .addParameter("engineId", String::class)
            .addStatement(
                "var engine = %T.getInstance().get(engineId)",
                engineCacheClassName
            )
            .beginControlFlow("if (engine == null)")
            .beginControlFlow(
                "engine = %T(context).apply",
                engineClassName
            )
            .addStatement(
                "dartExecutor.executeDartEntrypoint(%T.DartEntrypoint.createDefault())",
                dartExecutorClassName
            )
            .endControlFlow()
            .addStatement("%T.getInstance().put(engineId, engine)", engineCacheClassName)
            .endControlFlow()
            .build()
    }

    private fun removeEngineListFunction(): FunSpec {
        return FunSpec.builder("releaseEngine")
            .addParameter(
                "engineIdList",
                List::class.parameterizedBy(
                    String::class
                )
            )
            .beginControlFlow("for (engineId in engineIdList)")
            .addStatement("releaseEngine(engineId)")
            .endControlFlow()
            .build()
    }

    private fun removeEngineFunction(): FunSpec {
        return FunSpec.builder("releaseEngine")
            .addParameter("engineId", String::class)
            .addStatement(
                "var engine = %T.getInstance().get(engineId)",
                engineCacheClassName
            )
            .addStatement(
                "%T.getInstance().remove(engineId)",
                engineCacheClassName
            )
            .addStatement("engine?.destroy()")
            .build()
    }

    private fun getEngineFunction(): FunSpec {
        return FunSpec.builder("getEngine")
            .addParameter("engineId", String::class)
            .addStatement(
                "return %T.getInstance().get(engineId)",
                engineCacheClassName
            )
            .returns(engineClassName.copy(nullable = true))
            .build()
    }
}