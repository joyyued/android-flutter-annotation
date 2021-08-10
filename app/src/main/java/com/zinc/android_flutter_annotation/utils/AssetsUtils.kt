package com.zinc.android_flutter_annotation.utils

import android.content.res.Resources
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

object AssetsUtils {

    fun getAssetsFile(resources: Resources, fileName: String): ByteArray {
        val stream = resources.assets.open(fileName)
        return getBytes(stream)
    }

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 2048
        val buffer = ByteArray(bufferSize)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }
}