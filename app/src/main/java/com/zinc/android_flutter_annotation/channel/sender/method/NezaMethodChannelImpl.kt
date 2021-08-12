package com.zinc.android_flutter_annotation.channel.sender.method

import com.joyy.neza.channel.NezaMethodChannelProxy
import com.joyy.neza_annotation.model.ErrorResult
import com.joyy.neza_annotation.model.MethodChannelResult
import com.joyy.neza_annotation.model.MethodChannelResultType
import com.joyy.neza_annotation.model.SuccessResult
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object NezaMethodChannelImpl : NezaMethodChannel {
    override fun sayHelloToFlutter() {
        val params = HashMap<String, Any>()
        CoroutineScope(Dispatchers.Main).launch {
            sayHelloToFlutter(params = params)
        }
    }

    suspend fun sayHelloToFlutter(
        params: HashMap<String, Any>
    ) = suspendCoroutine<MethodChannelResult> {
        NezaMethodChannelProxy.instance
            .getChannel()
            ?.invokeMethod("sayHelloToFlutter", params, null)
    }

    override fun sayHelloToFlutterWithCallback(name: String, age: Int, height: Int?) {
        val params = HashMap<String, Any>()
        params["name"] = name
        params["age"] = age
        CoroutineScope(Dispatchers.Main).launch {
            sayHelloToFlutterWithCallback(params)
        }
    }

    suspend fun sayHelloToFlutterWithCallback(
        params: HashMap<String, Any>
    ) = suspendCoroutine<MethodChannelResult> {
        val callback = object : Result {
            override fun success(result: Any?) {
                it.resume(
                    MethodChannelResult(
                        resultType = MethodChannelResultType.SUCCESS,
                        successResult = SuccessResult(result),
                    )
                )
            }

            override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
                it.resume(
                    MethodChannelResult(
                        resultType = MethodChannelResultType.ERROR,
                        errorResult = ErrorResult(
                            errorCode = errorCode,
                            errorMessage = errorMessage,
                            errorDetails = errorDetails,
                        ),
                    )
                )
            }

            override fun notImplemented() {
                it.resume(
                    MethodChannelResult(
                        resultType = MethodChannelResultType.NOT_IMPLEMENTED,
                    )
                )
            }
        }
        NezaMethodChannelProxy.instance
            .getChannel()
            ?.invokeMethod("sayHelloToFlutterWithCallback", params, callback)
    }
}
