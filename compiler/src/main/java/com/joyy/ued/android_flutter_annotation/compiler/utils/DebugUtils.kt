package com.joyy.ued.android_flutter_annotation.compiler.utils

import com.joyy.ued.android_flutter_annotation.compiler.Printer
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

object DebugUtils {
    fun showInfo(printer: Printer, element: ExecutableElement) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("【 Executable Element 】").append(element).append("\n")
            .append("simpleName: ").append(element.simpleName).append("\n")
            .append("kind: ").append(element.kind).append("\n")
            .append("modifiers: ").append(element.modifiers).append("\n")
            .append("parameters: ").append(element.parameters).append("\n")
            .append("defaultValue: ").append(element.defaultValue).append("\n")
            .append("isDefault: ").append(element.isDefault).append("\n")
            .append("isVarArgs: ").append(element.isVarArgs).append("\n")
            .append("receiverType: ").append(element.receiverType).append("\n")
            .append("returnType: ").append(element.returnType).append("\n")
            .append("thrownTypes: ").append(element.thrownTypes).append("\n")
            .append("annotationMirrors: ").append(element.annotationMirrors).append("\n")
            .append("enclosedElements: ").append(element.enclosedElements).append("\n")
            .append("enclosingElement: ").append(element.enclosingElement).append("\n")
            .append("typeParameters: ").append(element.typeParameters).append("\n")
            .append("asType: ").append(element.asType()).append("\n")
        printer.note(stringBuilder.toString())
    }

    fun showInfo(printer: Printer, element: VariableElement) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("【 Variable Element 】").append(element).append("\n")
            .append("simpleName: ").append(element.simpleName).append("\n")
            .append("kind: ").append(element.kind).append("\n")
            .append("modifiers: ").append(element.modifiers).append("\n")
            .append("constantValue: ").append(element.constantValue).append("\n")
            .append("annotationMirrors: ").append(element.annotationMirrors).append("\n")
            .append("enclosedElements: ").append(element.enclosedElements).append("\n")
            .append("enclosingElement: ").append(element.enclosingElement).append("\n")
            .append("asType: ").append(element.asType()).append("\n")
        printer.note(stringBuilder.toString())
    }

    fun showInfo(printer: Printer, typeMirror: TypeMirror?) {
        typeMirror ?: return
        val stringBuilder = StringBuilder()
        stringBuilder.append("【 Type Mirror 】").append(typeMirror).append("\n")
            .append("annotationMirrors: ").append(typeMirror.annotationMirrors).append("\n")
            .append("kind: ").append(typeMirror.kind).append("\n")

        when (typeMirror.kind) {
            TypeKind.ARRAY -> {
                if(typeMirror is ArrayType){
                    stringBuilder.append("componentType: ").append(typeMirror.componentType).append("\n")
                        .append("asElement: ").append(typeMirror.annotationMirrors).append("\n")
                }
            }
            TypeKind.DECLARED -> {
                if (typeMirror is DeclaredType) {
                    stringBuilder.append("asElement: ").append(typeMirror.asElement()).append("\n")
                        .append("enclosingType: ").append(typeMirror.enclosingType).append("\n")
                        .append("typeArguments: ").append(typeMirror.typeArguments).append("\n")
                        .append("annotationMirrors: ").append(typeMirror.annotationMirrors).append("\n")
                }
            }
        }

        printer.note(stringBuilder.toString())
    }

    fun showInfo(printer: Printer, typeName: TypeName) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("【 Type Name 】").append(typeName).append("\n")
            .append("annotations: ").append(typeName.annotations).append("\n")
            .append("isAnnotated: ").append(typeName.isAnnotated).append("\n")
            .append("isNullable: ").append(typeName.isNullable).append("\n")
        printer.note(stringBuilder.toString())
    }
}