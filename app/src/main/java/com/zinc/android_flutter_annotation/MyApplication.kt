package com.zinc.android_flutter_annotation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.joyy.annotation.FlutterEngine
import com.joyy.flutter_annotation.manager.Flutter
import com.zinc.android_flutter_annotation.config.Config

@FlutterEngine(engineId = Config.ENGINE_ID)
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