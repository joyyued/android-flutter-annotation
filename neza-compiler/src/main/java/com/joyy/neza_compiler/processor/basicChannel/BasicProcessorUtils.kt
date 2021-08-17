package com.joyy.neza_compiler.processor.basicChannel

import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_compiler.Printer
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

object BasicProcessorUtils {

    fun getCodecTypeMirror(channelAnnotation: FlutterBasicChannel): TypeMirror? {
        var codecTypeMirror: TypeMirror? = null
        try {
            channelAnnotation.codecClass
        } catch (e: MirroredTypeException) {
            codecTypeMirror = e.typeMirror
        }
        return codecTypeMirror
    }

    fun getGenericsType(codecTypeMirror: TypeMirror, printer: Printer): TypeMirror? {
        if (codecTypeMirror !is DeclaredType) {
            printer.error("$codecTypeMirror must be a class.")
            return null
        }
        val codecElement = codecTypeMirror.asElement()
        if (codecElement !is TypeElement) {
            printer.error("$codecTypeMirror must be a class.")
            return null
        }

        var extendTypeMirror: TypeMirror? = null
        for (interfaceTypeMirror in codecElement.interfaces) {
            if (!interfaceTypeMirror.toString().startsWith(
                    "io.flutter.plugin.common.MessageCodec"
                )
            ) {
                continue
            }
            if (interfaceTypeMirror !is DeclaredType) {
                continue
            }

            val typeArguments = interfaceTypeMirror.typeArguments
            if (typeArguments.size <= 0) {
                continue
            }
            extendTypeMirror = typeArguments[0]
        }

        return extendTypeMirror
    }
}