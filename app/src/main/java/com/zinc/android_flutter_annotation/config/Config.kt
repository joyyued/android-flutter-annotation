package com.zinc.android_flutter_annotation.config

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/16 10:30 上午
 * @email: 56002982@qq.com
 * @des: 配置
 */
object Config {
    // 引擎 id
    const val ENGINE_ID = "NEZA_ENGINE_ID"

    const val METHOD_CHANNEL = "com.zinc.android_flutter_annotation/nezaMethodChannel"

    const val METHOD_CHANNEL_NONE_SENDER = "com.zinc" +
            ".android_flutter_annotation/nezaMethodChannelNoneSender"

    const val METHOD_CHANNEL_NONE_RECEIVER = "com.zinc" +
            ".android_flutter_annotation/nezaMethodChannelNoneReceiver"

    const val EVENT_CHANNEL = "com.zinc.android_flutter_annotation/nezaEventChannel"

    const val STRING_BASIC_CHANNEL = "com.zinc.android_flutter_annotation/nezaStringBasicChannel"

    const val STRING_BASIC_CHANNEL_NONE_SENDER = "com.zinc" +
            ".android_flutter_annotation/nezaStringBasicChannelNoneSender"

    const val STANDER_BASIC_CHANNEL = "com.zinc.android_flutter_annotation/nezaStandardBasicChannel"

    const val BINARY_BASIC_CHANNEL = "com.zinc.android_flutter_annotation/nezaBinaryBasicChannel"

    const val BINARY_JSON_CHANNEL = "com.zinc.android_flutter_annotation/nezaBinaryJsonChannel"

    const val BINARY_CUSTOMER_CHANNEL =
        "com.zinc.android_flutter_annotation/nezaCustomerBasicChannel"
}