package com.joyy.neza_compiler.utils

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/12 10:30 上午
 * @email: 56002982@qq.com
 * @des: 类型转换
 */
object TypeChangeUtils {

    fun change(type: String): String {
        return when (type) {
            java.lang.Integer::class.java.canonicalName -> "Int"
            java.lang.Short::class.java.canonicalName -> "Short"
            java.lang.Long::class.java.canonicalName -> "Long"
            java.lang.Double::class.java.canonicalName -> "Double"
            java.lang.Float::class.java.canonicalName -> "Float"
            java.lang.String::class.java.canonicalName -> "String"
            java.lang.Boolean::class.java.canonicalName -> "Boolean"
            java.lang.Byte::class.java.canonicalName -> "Byte"
            java.lang.Object::class.java.canonicalName -> "Any"
            else -> type
        }
    }

    fun change(type: TypeName): TypeName {
        return when (type.toString()) {
            java.lang.Integer::class.java.asTypeName().toString() -> Int::class.asTypeName()
            java.lang.Short::class.java.asTypeName().toString() -> Short::class.asTypeName()
            java.lang.Long::class.java.asTypeName().toString() -> Long::class.asTypeName()
            java.lang.Double::class.java.asTypeName().toString() -> Double::class.asTypeName()
            java.lang.Float::class.java.asTypeName().toString() -> Float::class.asTypeName()
            java.lang.String::class.java.asTypeName().toString() -> String::class.asTypeName()
            java.lang.Boolean::class.java.asTypeName().toString() -> Boolean::class.asTypeName()
            java.lang.Byte::class.java.asTypeName().toString() -> Byte::class.asTypeName()
            java.lang.Object::class.java.asTypeName().toString() -> Any::class.asTypeName()
            else -> type
        }
    }
}