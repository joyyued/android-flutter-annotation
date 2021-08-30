package com.joyy.ued.android_flutter_annotation.compiler.processor.basicChannel

import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.compiler.Printer
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/30 10:24 上午
 * @email: 56002982@qq.com
 * @des: Basic Message Channel 工具
 */
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

    fun checkCodecClass(
        codecTypeMirror: TypeMirror,
        printer: Printer
    ) {
        if (codecTypeMirror !is DeclaredType) {
            printer.error("$codecTypeMirror must be a class.")
            return
        }
        val codecElement = codecTypeMirror.asElement()
        if (codecElement !is TypeElement) {
            printer.error("$codecTypeMirror must be a class.")
            return
        }

        val enclosedElements = codecElement.enclosedElements
        var isFindInstance = false
        for (enclosedElement in enclosedElements) {
            if (enclosedElement.kind != ElementKind.FIELD) {
                continue
            }
            if (enclosedElement !is VariableElement) {
                continue
            }

            if (enclosedElement.simpleName.toString() != "INSTANCE") {
                continue
            }

            var isFindStatic = false
            for (modifier in enclosedElement.modifiers) {
                if (modifier == Modifier.STATIC) {
                    isFindStatic = true
                }
            }

            if (isFindStatic) {
                isFindInstance = true
                break
            }
        }

        if (!isFindInstance) {
            printer.error("You need support a static INSTANCE field. [$codecElement]")
            return
        }
    }
}