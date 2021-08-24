package com.joyy.compiler.config

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 10:03 上午
 * @email: 56002982@qq.com
 * @des: 类的配置
 */
object ClazzConfig {

    // 项目名称
    const val PROJECT_NAME = "FlutterAnnotation"

    // 引擎类名称
    const val ENGINE_CREATOR_NAME = "${PROJECT_NAME}EngineCreator"

    const val ENGINE_HELPER_PACKAGE = "com.joyy.api.utils"
    const val ENGINE_HELPER_NAME = "FlutterEngineHelper"

    const val METHOD_RESULT_MODEL_PACKAGE = "com.joyy.annotation.model"
    const val METHOD_RESULT_NAME = "MethodChannelResult"
    const val METHOD_RESULT_SUCCESS_NAME = "SuccessResult"
    const val METHOD_RESULT_ERROR_NAME = "ErrorResult"
    const val METHOD_RESULT_TYPE_NAME = "MethodChannelResultType"

    const val EVENT_CHANNEL_SENDER_TYPE_PACKAGE = "com.joyy.annotation.model"
    const val EVENT_CHANNEL_SENDER_ERROR_TYPE_NAME = "EventChannelSenderErrorType"
    const val EVENT_CHANNEL_SENDER_EOS_TYPE_NAME = "EventChannelSenderEOSType"

    const val FLUTTER_MANAGER_NAME = "Flutter"
    const val FLUTTER_CHANNEL_NAME = "Channels"
    const val FLUTTER_ENGINE_NAME = "Engine"
    const val FLUTTER_INIT_NAME = "init"

    object Channel {
        const val CHANNEL_PACKAGE = "com.joyy.api.channel"

        const val METHOD_CHANNEL_NAME = "MethodChannelInterface"
        const val EVENT_CHANNEL_NAME = "EventChannelInterface"
        const val BASIC_CHANNEL_NAME = "BasicChannelInterface"
    }

    object Flutter {
        const val ENGINE_PACKAGE = "io.flutter.embedding.engine"
        const val ENGINE_NAME = "FlutterEngine"
        const val ENGINE_CACHE_NAME = "FlutterEngineCache"

        const val DART_EXECUTOR_PACKAGE = "io.flutter.embedding.engine.dart"
        const val DART_EXECUTOR_NAME = "DartExecutor"

        const val METHOD_CHANNEL_PACKAGE = "io.flutter.plugin.common"
        const val METHOD_CHANNEL_NAME = "MethodChannel"
        const val EVENT_CHANNEL_NAME = "EventChannel"
        const val BASIC_CHANNEL_NAME = "BasicMessageChannel"

        const val SINK_NAME = "$EVENT_CHANNEL_NAME.EventSink"

        const val METHOD_RESULT_PACKAGE = "$METHOD_CHANNEL_PACKAGE.$METHOD_CHANNEL_NAME"
        const val METHOD_RESULT_NAME = "Result"

        const val BASIC_REPLY_PACKAGE = "$METHOD_CHANNEL_PACKAGE.$BASIC_CHANNEL_NAME"
        const val BASIC_REPLY_NAME = "Reply"
    }

    object Android {
        const val CONTEXT_PACKAGE = "android.content"
        const val CONTEXT_NAME = "Context"

        const val ANDROID_UTIL_PACKAGE = "android.util"
        const val ANDROID_LOG_NAME = "Log"
    }

    object PACKAGE {
        // 包名
        const val PACKAGE_NAME = "com.joyy.flutter_annotation"

        // 引擎包
        const val ENGINE_NAME = "$PACKAGE_NAME.engine"

        // channel
        const val CHANNEL_NAME = "$PACKAGE_NAME.channel"

        // manager
        const val FLUTTER_MANAGER = "$PACKAGE_NAME.manager"
    }

    object Coroutine {
        const val COROUTINE_X_PACKAGE = "kotlinx.coroutines"
        const val COROUTINE_PACKAGE = "kotlin.coroutines"

        const val COROUTINE_SCOPE_NAME = "CoroutineScope"
        const val COROUTINE_DISPATCHERS_NAME = "Dispatchers"
        const val COROUTINE_ASYNC_NAME = "async"
        const val COROUTINE_RESUME_NAME = "resume"
        const val COROUTINE_SUSPEND_COROUTINE_NAME = "suspendCoroutine"

        const val DEFERRED_NAME = "Deferred"
    }
}