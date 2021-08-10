package com.zinc.android_flutter_annotation.neza

import android.content.Context
import com.zinc.android_flutter_annotation.channel.receiver.NezaMethodChannelProxy
import com.zinc.android_flutter_annotation.channel.sender.event.NezaEventChannelImpl
import com.zinc.android_flutter_annotation.neza.utils.FlutterEngineHelper

object NezaChannelManager {

    fun init(context: Context) {
        // 初始化 engine
        FlutterEngineHelper.createEngine(context, "NEZA_ENGINE_ID")

        // 初始化 method channel
        NezaMethodChannelProxy.instance.init(context)

        // 初始化 event channel
        NezaEventChannelImpl.instance.init(context)
    }
}