package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.neza.channel.NezaBasicChannelProxy
import io.flutter.plugin.common.BasicMessageChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 2:33 下午
 * @email: 56002982@qq.com
 * @des:
 */
object NezaBasicChannelImpl : NezaBasicChannel {
    override fun sendJsonToFlutter(json: String) {
        CoroutineScope(Dispatchers.Main).launch {
            sendJsonToFlutterAsync(json)
        }
    }

    suspend fun sendJsonToFlutterAsync(json: String): String? {
        return suspendCoroutine {
            val callback = BasicMessageChannel.Reply<String> { reply ->
                it.resume(reply)
            }
            NezaBasicChannelProxy.instance.getChannel()
                ?.send(json, callback)
        }
    }
}