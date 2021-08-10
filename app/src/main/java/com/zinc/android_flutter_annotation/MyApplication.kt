package com.zinc.android_flutter_annotation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.zinc.android_flutter_annotation.neza.NezaChannelManager

class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        NezaChannelManager.init(this)
    }
}