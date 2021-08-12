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

    object Channel {
        const val CHANNEL_PACKAGE = "com.joyy.neza_api.channel"
        const val METHOD_CHANNEL_NAME = "MethodChannelInterface"
    }

    object Flutter {
        const val ENGINE_PACKAGE = "io.flutter.embedding.engine"
        const val ENGINE_NAME = "FlutterEngine"

        const val METHOD_CHANNEL_PACKAGE = "io.flutter.plugin.common"
        const val METHOD_CHANNEL_NAME = "MethodChannel"
    }

    object Android {
        const val CONTEXT_PACKAGE = "android.content"
        const val CONTEXT_NAME = "Context"
    }

    object PACKAGE {
        // 包名
        const val NEZA_PACKAGE = "com.joyy.neza"

        // 引擎包
        const val NEZA_ENGINE = "$NEZA_PACKAGE.engine"

        // channel
        const val NEZA_CHANNEL = "$NEZA_PACKAGE.channel"
    }
}