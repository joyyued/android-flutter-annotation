package com.joyy.neza_compiler.processor.common

import com.joyy.neza_annotation.common.Param
import com.joyy.neza_annotation.common.ParamMap
import com.joyy.neza_compiler.Printer
import com.sun.org.apache.xpath.internal.operations.Bool
import javax.lang.model.element.VariableElement

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