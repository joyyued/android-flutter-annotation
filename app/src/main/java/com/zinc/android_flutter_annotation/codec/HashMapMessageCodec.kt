package com.zinc.android_flutter_annotation.codec

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.flutter.plugin.common.MessageCodec
import io.flutter.plugin.common.StringCodec
import org.json.JSONException
import java.nio.ByteBuffer

class HashMapMessageCodec private constructor() : MessageCodec<HashMap<String, String>> {
    var gson = Gson()

    override fun encodeMessage(message: HashMap<String, String>?): ByteBuffer? {
        if (message == null) {
            return null
        }
        return StringCodec.INSTANCE.encodeMessage(gson.toJson(message))
    }

    override fun decodeMessage(message: ByteBuffer?): HashMap<String, String> {
        return if (message == null) {
            HashMap()
        } else {
            try {
                val json = StringCodec.INSTANCE.decodeMessage(message)
                val type = object : TypeToken<HashMap<String, Any>>() {}.type
                val fromJson: HashMap<String, String> = gson.fromJson(json, type)
                fromJson
            } catch (e: JSONException) {
                throw IllegalArgumentException("Invalid JSON", e)
            }
        }
    }

    companion object {
        val INSTANCE = HashMapMessageCodec()
    }
}