package com.joyy.neza_api.utils

import android.content.Context
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

object FlutterEngineHelper {

    fun getFlutterEngine(engineId: String): FlutterEngine? {
        return FlutterEngineCache.getInstance().get(engineId)
    }

    fun createEngine(context: Context, engineId: String): FlutterEngine {
        var engine = FlutterEngineCache.getInstance().get(engineId)
        if (engine == null) {
            engine = FlutterEngine(context).apply {
                dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            }
            FlutterEngineCache.getInstance().put(engineId, engine)
        }
        return engine
    }
}