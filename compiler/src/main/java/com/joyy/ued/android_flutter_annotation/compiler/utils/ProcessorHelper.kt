package com.joyy.ued.android_flutter_annotation.compiler.utils

import com.joyy.ued.android_flutter_annotation.annotation.common.Param
import com.joyy.ued.android_flutter_annotation.annotation.common.ParamMap
import com.joyy.ued.android_flutter_annotation.compiler.Printer
import javax.lang.model.element.ExecutableElement
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
        method: ExecutableElement,
        parameterList: List<VariableElement>
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

        // 两者都为空，则空参数、多参数默认为 map
        // 但参数默认为 origin
        return if (parameterList.size == 1) {
            ParamType.ORIGIN
        } else {
            ParamType.MAP
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