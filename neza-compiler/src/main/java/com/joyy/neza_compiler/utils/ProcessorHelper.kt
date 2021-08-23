package com.joyy.neza_compiler.utils

import com.joyy.neza_annotation.common.Param
import com.joyy.neza_annotation.common.ParamMap
import com.joyy.neza_compiler.Printer
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/18 10:51 上午
 * @email: 56002982@qq.com
 * @des: Sender
 */
object ProcessorHelper {

    fun checkParam(
        printer: Printer,
        method: ExecutableElement,
    ): ParamType {
        val paramAnnotation = method.getAnnotation(Param::class.java)
        val paramMapAnnotation = method.getAnnotation(ParamMap::class.java)

        if (paramAnnotation != null && paramMapAnnotation != null) {
            printer.error(
                "Don't use @Param and @ParamMap annotation at the same time on " +
                        "[${method.simpleName}] function."
            )
        }

        if (paramAnnotation != null) {
            return ParamType.ORIGIN
        } else if (paramMapAnnotation != null) {
            return ParamType.MAP
        }

        return ParamType.MAP
    }

    fun getGenericsType(printer: Printer, typeMirror: TypeMirror): ArrayList<TypeName> {
        return if (typeMirror is DeclaredType) {
            val result = ArrayList<TypeName>()
            val typeArguments = typeMirror.typeArguments
            typeArguments.forEach { typeMirror ->
//                printer.note(
//                    "${typeMirror.getAnnotationsByType(Nullable::class.java)}"
//                )
//                typeMirror.getAnnotation(Nullable::class.java)
//                typeMirror.annotationMirrors
                result.add(TypeChangeUtils.change(typeMirror.asTypeName()))
            }
            result
        } else {
            ArrayList()
        }
    }
}

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/18 10:53 上午
 * @email: 56002982@qq.com
 * @des: 参数类型
 */
enum class ParamType {
    ORIGIN,
    MAP,
}