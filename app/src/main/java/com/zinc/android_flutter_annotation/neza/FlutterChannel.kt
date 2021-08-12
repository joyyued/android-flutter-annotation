package com.zinc.android_flutter_annotation.neza

import android.content.Context
import com.joyy.neza.channel.NezaMethodChannelImpl
import com.joyy.neza.channel.NezaMethodChannelProxy
import com.joyy.neza.engine.NezaEngineCreator
import com.zinc.android_flutter_annotation.channel.receiver.basic.NezaBasicChannelProxy
import com.zinc.android_flutter_annotation.channel.sender.event.NezaEventChannelImpl

object Flutter {

    object Channels{
        val nezaEventChannel = NezaEventChannelImpl.instance
        val nezaMethodChannel = NezaMethodChannelImpl
    }

    object Engine{

    }


    fun init(context: Context) {
        // 初始化 engine
        NezaEngineCreator.init(context)

        // 初始化 method channel
        NezaMethodChannelProxy.instance.init(context)

        // 初始化 event channel
        NezaEventChannelImpl.instance.init(context)

        // 初始化 basic channel
        NezaBasicChannelProxy.instance.init(context)
    }
}