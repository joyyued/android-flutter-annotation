package com.zinc.android_flutter_annotation.channel.sender.event

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 2:28 下午
 * @email: 56002982@qq.com
 * @des: Event Channel
 */
interface NezaEventChannel {

    fun sendImageInfo(byteArray: ByteArray)
}