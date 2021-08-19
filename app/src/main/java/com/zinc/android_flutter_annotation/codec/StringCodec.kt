package com.zinc.android_flutter_annotation.codec

import io.flutter.plugin.common.MessageCodec
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class StringCodec private constructor() : MessageCodec<String> {
    override fun encodeMessage(message: String?): ByteBuffer? {
        if (message == null) {
            return null
        }
        val bytes = message.toByteArray(UTF8)
        val buffer = ByteBuffer.allocateDirect(bytes.size)
        buffer.put(bytes)
        return buffer
    }

    override fun decodeMessage(message: ByteBuffer?): String {
        if (message == null) {
            return ""
        }
        val bytes: ByteArray
        val offset: Int
        val length = message.remaining()
        if (message.hasArray()) {
            bytes = message.array()
            offset = message.arrayOffset()
        } else {
            bytes = ByteArray(length)
            message[bytes]
            offset = 0
        }
        return String(bytes, offset, length, UTF8)
    }

    companion object {
        private val UTF8 = StandardCharsets.UTF_8
        val INSTANCE = StringCodec()
    }
}