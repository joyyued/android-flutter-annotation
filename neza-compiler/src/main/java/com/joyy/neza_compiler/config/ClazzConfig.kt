package com.joyy.neza_compiler.config

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 10:03 上午
 * @email: 56002982@qq.com
 * @des: 类的配置
 */
object ClazzConfig {

    // 项目名称
    const val PROJECT_NAME = "Neza"

    // 引擎类名称
    const val ENGINE_CREATOR_NAME = "${PROJECT_NAME}EngineCreator"

    const val ENGINE_HELPER_PACKAGE = "com.joyy.neza_api.utils"
    const val ENGINE_HELPER_NAME = "FlutterEngineHelper"

    object Android {
        const val CONTEXT_PACKAGE = "android.content"
        const val CONTEXT_NAME = "Context"
    }

    object PACKAGE {
        const val PATH = "app/src/main/java"

        // 包名
        const val NEZA_PACKAGE = "com.joyy.neza"

        // 引擎包
        const val NEZA_ENGINE = "$NEZA_PACKAGE.engine"
    }
}