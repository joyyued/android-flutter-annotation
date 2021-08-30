package com.joyy.ued.android_flutter_annotation.compiler.utils

import com.joyy.ued.android_flutter_annotation.annotation.common.Callback
import com.joyy.ued.android_flutter_annotation.compiler.Printer
import javax.lang.model.element.VariableElement

/**
 * @author: Jiang PengYong
 * @date: 2021/8/27 10:05 上午
 * @email: 56002982@qq.com
 * @des: receiver 的辅助类
 */
object ReceiverHelper {

    fun checkCallbackType(
        printer: Printer,
        resultType: String,
        parameter: VariableElement
    ) {
        if (!isCallback(parameter)) {
            return
        }
        val paramType = parameter.asType()?.toString() ?: ""
        if (paramType != resultType) {
            printer.error("@Callback annotation only can use on $resultType type parameter.")
        }
    }

    fun isCallback(parameter: VariableElement): Boolean {
        return parameter.getAnnotation(Callback::class.java) != null
    }

    fun getParamSize(parameterList: List<VariableElement>): Int {
        var size = 0
        parameterList.forEach { parameter ->
            if (!isCallback(parameter)) {
                ++size
            }
        }
        return size
    }
}