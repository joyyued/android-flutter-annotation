package com.joyy.neza_compiler.processor.basicChannel

import com.squareup.kotlinpoet.ClassName
import javax.lang.model.type.TypeMirror

data class ChannelInfo(
    val className: ClassName,
    val typeMirror: TypeMirror
)
