package com.joyy.ued.android_flutter_annotation.compiler.utils

import com.joyy.ued.android_flutter_annotation.annotation.FlutterEngine
import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.event.FlutterEventChannel
import com.joyy.ued.android_flutter_annotation.annotation.method.FlutterMethodChannel
import java.lang.RuntimeException
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/30 10:29 上午
 * @email: 56002982@qq.com
 * @des: 引擎辅助
 */
object EngineHelper {

    fun getFlutterEngineElements(roundEnv: RoundEnvironment): ArrayList<Element> {
        val result = ArrayList<Element>()

        val engineElements = roundEnv.getElementsAnnotatedWith(FlutterEngine::class.java)
        for (engineElement in engineElements) {
            var isValid = true
            val annotationMirrors = engineElement.annotationMirrors
            annotationMirrors.forEach { annotationMirror ->
                // 获取注解全路径
                val typeElement = annotationMirror.annotationType.asElement() as TypeElement
                val clazzName = typeElement.qualifiedName.toString()

                if (clazzName == FlutterMethodChannel::class.java.canonicalName
                    || clazzName == FlutterEventChannel::class.java.canonicalName
                    || clazzName == FlutterBasicChannel::class.java.canonicalName
                ) {
                    isValid = false
                }
            }
            if (isValid) {
                result.add(engineElement)
            }
        }

        return result
    }

    /**
     * 获取第一个 engine id
     */
    fun getEngineId(roundEnv: RoundEnvironment): String {
        val flutterEngineElements = getFlutterEngineElements(roundEnv)
        val size = flutterEngineElements.size

        if (size > 1) {
            throw RuntimeException("FlutterEngine can only use once time in app.")
        } else if (size <= 0) {
            throw RuntimeException(
                "You should use FlutterEngine annotation to declare the engineId first."
            )
        }

        return flutterEngineElements.first().getAnnotation(FlutterEngine::class.java).engineId
    }
}