package com.joyy.neza_compiler.processor.common

import com.joyy.neza_annotation.common.Param
import com.joyy.neza_annotation.common.ParamMap
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.utils.DebugUtils
import com.joyy.neza_compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import sun.reflect.annotation.TypeAnnotation
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
        parameterList: List<VariableElement>
    ): ParamType {
        if (parameterList.isEmpty()) {
            return ParamType.MAP
        }

        val size = parameterList.size
        if (size == 1) {
            val variableElement = parameterList[0]
            val paramAnnotation = variableElement.getAnnotation(Param::class.java)
            val paramMapAnnotation = variableElement.getAnnotation(ParamMap::class.java)
            if (paramAnnotation != null && paramMapAnnotation != null) {
                printer.error(
                    "Don't use @Param and @ParamMap annotation at the same time on " +
                            "[${variableElement.simpleName}] param."
                )
            } else if (paramAnnotation == null && paramMapAnnotation == null) {
                printer.error(
                    "Must use one of @Param or @ParamMap annotation  on " +
                            "[${variableElement.simpleName}] param."
                )
            } else if (paramAnnotation != null) {
                return ParamType.ORIGIN
            } else if (paramMapAnnotation != null) {
                return ParamType.MAP
            }
        }

        for (variableElement in parameterList) {
            val paramAnnotation = variableElement.getAnnotation(Param::class.java)
            val paramMapAnnotation = variableElement.getAnnotation(ParamMap::class.java)
            if (paramAnnotation != null) {
                printer.error(
                    "Don't use the @Param annotation on [${variableElement.simpleName}] param, " +
                            "when the function has more than one param."
                )
            }
            if (paramMapAnnotation == null) {
                printer.error(
                    "Must use the @ParamMap annotation on [${variableElement.simpleName}] param, " +
                            "when the function has more than one param."
                )
            }
        }
        return ParamType.MAP
    }

//    fun changeType(variableElement: VariableElement): TypeName {
//        val genericsType = getGenericsType(variableElement)
//        variableElement.
//    }

//    fun changeGenericsToKotlin(variableElement: VariableElement){
//        val typeName = variableElement.asType()
//        var type = typeName.asTypeName()
//
//        if (typeName is DeclaredType) {
//            val resultType = TypeChangeUtils.change(
//                typeName.asElement().asType().asTypeName()
//            )
//            if (resultType is ClassName) {
//                val genericsType = getGenericsType(typeName)
//                type = if(genericsType.isNotEmpty()){
//                    resultType.parameterizedBy(
//                        genericsType
//                    )
//                }else{
//                    resultType
//                }
//            }
//        } else {
//            type = TypeChangeUtils.change(type)
//        }
//    }

    fun getGenericsType(printer: Printer, typeMirror: TypeMirror): ArrayList<TypeName> {
        return if (typeMirror is DeclaredType) {
            val result = ArrayList<TypeName>()
            val typeArguments = typeMirror.typeArguments
            typeArguments.forEach { typeMirror ->
                printer.note(
                    "${ typeMirror.getAnnotationsByType(Nullable::class.java)}"
                )

                typeMirror.getAnnotation(Nullable::class.java)
                typeMirror.getAnnotationMirrors()
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