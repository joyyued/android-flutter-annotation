package com.zinc.android_flutter_annotation.channel.sender.event

import android.content.Context
import com.zinc.android_flutter_annotation.neza.channel.EventChannelInterface
import com.zinc.android_flutter_annotation.neza.utils.FlutterEngineHelper
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 2:33 下午
 * @email: 56002982@qq.com
 * @des:
 */
class NezaEventChannelImpl private constructor() :
    NezaEventChannel,
    EventChannelInterface {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            NezaEventChannelImpl()
        }
    }

    private val engineId = "NEZA_ENGINE_ID"
    private val name = "com.zinc.android_flutter_annotation/nezaEventChannel"
    private var engine: FlutterEngine? = null
    private var channel: EventChannel? = null

    private var eventSink: EventChannel.EventSink? = null

    override fun init(context: Context) {
        engine = FlutterEngineHelper.getFlutterEngine(engineId)
            ?.apply {
                channel = EventChannel(
                    this.dartExecutor.binaryMessenger,
                    name
                ).apply {
                    setStreamHandler(
                        object : EventChannel.StreamHandler {
                            override fun onListen(
                                arguments: Any?,
                                events: EventChannel.EventSink
                            ) {
                                eventSink = events
                            }

                            override fun onCancel(arguments: Any?) {
                                eventSink = null
                            }
                        })
                }
            }
    }

    override fun getChannel(): EventChannel? = channel

    override fun getChannelName(): String = name

    override fun getEventSink(): EventChannel.EventSink? = eventSink

    override fun sendImageInfo(byteArray: ByteArray) {
        // TODO No:2
        eventSink?.success(byteArray)

        eventSink?.error()

        eventSink?.endOfStream()
    }

    sendImageInfor(type= type.success, type)
    fun sendImageInfor(
                       type :Type = ErrorType.error, byteArray: ByteArray) {
        eventSink?.error()
    }


}