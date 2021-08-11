package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.content.Context
import com.joyy.neza_api.channel.BasicChannelInterface
import com.joyy.neza_api.utils.FlutterEngineHelper
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StringCodec

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des:
 */
class NezaBasicChannelProxy private constructor() : BasicChannelInterface<String> {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            NezaBasicChannelProxy()
        }
    }

    private val engineId = "NEZA_ENGINE_ID"
    private val name = "com.zinc.android_flutter_annotation/nezaBasicChannel"
    private var engine: FlutterEngine? = null
    private var channel: BasicMessageChannel<String>? = null

    override fun init(context: Context) {

        engine = FlutterEngineHelper.getFlutterEngine(engineId)?.apply {
            // TODO No:3
            channel = BasicMessageChannel(
                dartExecutor.binaryMessenger,
                name,
                StringCodec.INSTANCE
            ).apply {
                setMessageHandler { message, reply ->
                    NezaBasicChannel.receiverJsonFromFlutter(message ?: "")
                }
            }
        }
    }

    override fun getChannel(): BasicMessageChannel<String>? = channel

    override fun getChannelName(): String = name
}