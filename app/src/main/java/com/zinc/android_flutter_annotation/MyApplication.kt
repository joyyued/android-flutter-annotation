package com.zinc.android_flutter_annotation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.joyy.neza_annotation.FlutterEngine
import com.joyy.neza_api.config.FlutterConfig
import com.zinc.android_flutter_annotation.neza.Flutter

@FlutterEngine(engineId = FlutterConfig.ENGINE_ID)
class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        Flutter.init(this)
    }
}