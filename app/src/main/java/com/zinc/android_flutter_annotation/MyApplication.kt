package com.zinc.android_flutter_annotation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.joyy.neza.manager.Flutter
import com.joyy.neza_annotation.FlutterEngine
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