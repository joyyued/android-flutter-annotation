package com.joyy.ued.android_flutter_annotation.compiler.processor.basicChannel

import com.squareup.kotlinpoet.ClassName
import javax.lang.model.type.TypeMirror

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/30 10:24 上午
 * @email: 56002982@qq.com
 * @des: 渠道信息
 */
data class ChannelInfo(
    val className: ClassName,
    val typeMirror: TypeMirror
)
