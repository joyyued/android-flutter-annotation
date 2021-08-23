package com.joyy.neza_compiler.utils

import com.joyy.neza_annotation.method.ParseData
import com.joyy.neza_compiler.Printer
import com.squareup.kotlinpoet.TypeName
import java.lang.StringBuilder
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

object DebugUtils {
    fun showInfo(printer: Printer, element: ExecutableElement) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("【 Executable Element 】").appendLine(element)
            .append("simpleName: ").appendLine(element.simpleName)
            .append("kind: ").appendLine(element.kind)
            .append("modifiers: ").appendLine(element.modifiers)
            .append("parameters: ").appendLine(element.parameters)
            .append("defaultValue: ").appendLine(element.defaultValue)
            .append("isDefault: ").appendLine(element.isDefault)
            .append("isVarArgs: ").appendLine(element.isVarArgs)
            .append("receiverType: ").appendLine(element.receiverType)
            .append("returnType: ").appendLine(element.returnType)
            .append("thrownTypes: ").appendLine(element.thrownTypes)
            .append("annotationMirrors: ").appendLine(element.annotationMirrors)
            .append("enclosedElements: ").appendLine(element.enclosedElements)
            .append("enclosingElement: ").appendLine(element.enclosingElement)
            .append("typeParameters: ").appendLine(element.typeParameters)
            .append("asType: ").appendLine(element.asType())
        printer.note(stringBuilder.toString())
    }

    fun showInfo(printer: Printer, element: VariableElement) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("【 Variable Element 】").appendLine(element)
            .append("simpleName: ").appendLine(element.simpleName)
            .append("kind: ").appendLine(element.kind)
            .append("modifiers: ").appendLine(element.modifiers)
            .append("constantValue: ").appendLine(element.constantValue)
            .append("annotationMirrors: ").appendLine(element.annotationMirrors)
            .append("enclosedElements: ").appendLine(element.enclosedElements)
            .append("enclosingElement: ").appendLine(element.enclosingElement)
            .append("asType: ").appendLine(element.asType())
        printer.note(stringBuilder.toString())
    }

    fun showInfo(printer: Printer, typeMirror: TypeMirror?) {
        typeMirror ?: return
        val stringBuilder = StringBuilder()
        stringBuilder.append("【 Type Mirror 】").appendLine(typeMirror)
            .append("annotationMirrors: ").appendLine(typeMirror.annotationMirrors)
            .append("kind: ").appendLine(typeMirror.kind)

        when (typeMirror.kind) {
//            TypeKind.BOOLEAN -> TODO()
//            TypeKind.BYTE -> TODO()
//            TypeKind.SHORT -> TODO()
//            TypeKind.INT -> TODO()
//            TypeKind.LONG -> TODO()
//            TypeKind.CHAR -> TODO()
//            TypeKind.FLOAT -> TODO()
//            TypeKind.DOUBLE -> TODO()
//            TypeKind.VOID -> TODO()
//            TypeKind.NONE -> TODO()
//            TypeKind.NULL -> TODO()
            TypeKind.ARRAY -> {
                if(typeMirror is ArrayType){
                    stringBuilder.append("componentType: ").appendLine(typeMirror.componentType)
                        .append("asElement: ").appendLine(typeMirror.annotationMirrors)
                }
            }
            TypeKind.DECLARED -> {
                if (typeMirror is DeclaredType) {
                    stringBuilder.append("asElement: ").appendLine(typeMirror.asElement())
                        .append("enclosingType: ").appendLine(typeMirror.enclosingType)
                        .append("typeArguments: ").appendLine(typeMirror.typeArguments)
                        .append("annotationMirrors: ").appendLine(typeMirror.annotationMirrors)
                }
            }
//            TypeKind.ERROR -> TODO()
//            TypeKind.TYPEVAR -> TODO()
//            TypeKind.WILDCARD -> TODO()
//            TypeKind.PACKAGE -> TODO()
//            TypeKind.EXECUTABLE -> TODO()
//            TypeKind.OTHER -> TODO()
//            TypeKind.UNION -> TODO()
//            TypeKind.INTERSECTION -> TODO()
        }

        printer.note(stringBuilder.toString())
    }

    fun showInfo(printer: Printer, typeName: TypeName) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("【 Type Name 】").appendLine(typeName)
            .append("annotations: ").appendLine(typeName.annotations)
            .append("isAnnotated: ").appendLine(typeName.isAnnotated)
            .append("isNullable: ").appendLine(typeName.isNullable)
        printer.note(stringBuilder.toString())
    }
}