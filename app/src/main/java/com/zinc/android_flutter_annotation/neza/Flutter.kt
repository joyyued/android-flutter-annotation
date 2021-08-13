//package com.zinc.android_flutter_annotation.neza
//
//import android.content.Context
//import com.joyy.neza.channel.NezaEventChannelImpl
//import com.joyy.neza.channel.NezaMethodChannelImpl
//import com.joyy.neza.channel.NezaMethodChannelProxy
//import com.joyy.neza.engine.NezaEngineCreator
//import com.joyy.neza_api.utils.FlutterEngineHelper
//import com.zinc.android_flutter_annotation.channel.receiver.basic.NezaBasicChannelProxy
//import io.flutter.embedding.engine.FlutterEngine
//
//object Flutter {
//
//    object Channels {
//        val nezaEventChannel = NezaEventChannelImpl.instance
//        val nezaMethodChannel = NezaMethodChannelImpl
//    }
//
//    object Engine {
//        const val DEFAULT_ENGINE = "NEZA_ENGINE_ID"
//        fun getEngine(engineId: String = DEFAULT_ENGINE): FlutterEngine? {
//            return FlutterEngineHelper.getFlutterEngine(engineId)
//        }
//    }
//
//    fun init(context: Context) {
//        // 初始化 engine
//        NezaEngineCreator.init(context)
//
//        // 初始化 method channel
//        NezaMethodChannelProxy.instance.init(context)
//
//        // 初始化 event channel
//        NezaEventChannelImpl.instance.init(context)
//
//        // 初始化 basic channel
//        NezaBasicChannelProxy.instance.init(context)
//    }
//}