package com.zinc.android_flutter_annotation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.joyy.neza_annotation.FlutterEngine
import com.zinc.android_flutter_annotation.neza.FlutterChannel
import com.zinc.android_flutter_annotation.neza.NezaChannelManager
import com.zinc.android_flutter_annotation.neza.config.FlutterConfig

@FlutterEngine(engineId = FlutterConfig.ENGINE_ID)
class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        FlutterChannel.init(this)
    }
}