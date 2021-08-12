//package com.zinc.android_flutter_annotation.channel.receiver.method
//
//import android.content.Context
//import com.joyy.neza_api.channel.MethodChannelInterface
//import com.joyy.neza_api.utils.FlutterEngineHelper
//import io.flutter.embedding.engine.FlutterEngine
//import io.flutter.plugin.common.MethodChannel
//
///**
// * @author: Jiang Pengyong
// * @date: 2021/8/10 11:21 上午
// * @email: 56002982@qq.com
// * @des:
// */
//class NezaMethodChannelProxy private constructor() : MethodChannelInterface {
//
//    companion object {
//        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
//            NezaMethodChannelProxy()
//        }
//    }
//
//    private val engineId = "NEZA_ENGINE_ID"
//    private val name = "com.zinc.android_flutter_annotation/nezaMethodChannel"
//    private var engine: FlutterEngine? = null
//    private var channel: MethodChannel? = null
//
//    override fun init(context: Context) {
//        engine = FlutterEngineHelper.getFlutterEngine(engineId)?.apply {
//            channel = MethodChannel(
//                dartExecutor.binaryMessenger,
//                name
//            ).apply {
//                setMethodCallHandler { call, result ->
//                    val method = call.method
//                    val arguments = call.arguments
//                    when (method) {
//
//                        "sayHelloToNative" -> {
//                            NezaMethodChannel.sayHelloToNative()
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    override fun getChannel() = channel
//
//    override fun getChannelName() = name
//}