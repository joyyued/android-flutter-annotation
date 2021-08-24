package com.joyy.neza_compiler.utils

import com.joyy.neza_compiler.Printer
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.sun.xml.internal.fastinfoset.util.StringArray
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

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

    private val ARRAY_SKIP_LIST = arrayListOf(
        Int::class.asTypeName() to ByteArray::class.asTypeName(),
        Short::class.asTypeName() to ShortArray::class.asTypeName(),
        Long::class.asTypeName() to LongArray::class.asTypeName(),
        Double::class.asTypeName() to DoubleArray::class.asTypeName(),
        Float::class.asTypeName() to FloatArray::class.asTypeName(),
        String::class.asTypeName() to StringArray::class.asTypeName(),
        Boolean::class.asTypeName() to BooleanArray::class.asTypeName(),
        Byte::class.asTypeName() to ByteArray::class.asTypeName()
    )
    private val TYPE_MAP = mapOf(
        java.util.HashMap::class.java.asTypeName().toString() to HashMap::class.asTypeName(),
        java.util.ArrayList::class.java.asTypeName().toString() to ArrayList::class.asTypeName(),
        Array<Any>::class.java.asTypeName().toString() to Array<Any>::class.asTypeName()
    )

    private fun changeSelf(typeName: TypeName): TypeName {
        val type = typeName.toString()
        return when (type) {
            java.lang.Integer::class.java.asTypeName().toString() -> Int::class.asTypeName()
            java.lang.Short::class.java.asTypeName().toString() -> Short::class.asTypeName()
            java.lang.Long::class.java.asTypeName().toString() -> Long::class.asTypeName()
            java.lang.Double::class.java.asTypeName().toString() -> Double::class.asTypeName()
            java.lang.Float::class.java.asTypeName().toString() -> Float::class.asTypeName()
            java.lang.String::class.java.asTypeName().toString() -> String::class.asTypeName()
            java.lang.Boolean::class.java.asTypeName().toString() -> Boolean::class.asTypeName()
            java.lang.Byte::class.java.asTypeName().toString() -> Byte::class.asTypeName()
            java.lang.Object::class.java.asTypeName().toString() -> Any::class.asTypeName()
            else -> {
                for ((key, item) in ARRAY_SKIP_LIST) {
                    val checkType = Array<Any>::class.asTypeName().parameterizedBy(
                        key
                    )
                    if (type.startsWith(checkType.toString())) {
                        return item
                    }
                }
                for ((key, item) in TYPE_MAP) {
                    if (type.startsWith(key)) {
                        return item
                    }
                }
                return typeName
            }
        }
    }

    fun change(printer: Printer, typeMirror: TypeMirror): TypeName {
        var result: TypeName
        if (typeMirror is DeclaredType) {
            val typeArguments = typeMirror.typeArguments
            val arrayList = ArrayList<TypeName>()
            for (typeArgument in typeArguments) {
                arrayList.add(change(printer, typeArgument))
            }
            result = changeSelf(typeMirror.asTypeName())
            if (result is ClassName) {
                if (arrayList.isNotEmpty()) {
                    result = result.parameterizedBy(arrayList)
                }
            }
        } else {
            result = changeSelf(typeMirror.asTypeName())
        }
        return result
    }
}