package com.joyy.neza_compiler.utils

import com.joyy.neza_annotation.method.ParseData
import com.joyy.neza_compiler.Printer
import java.lang.StringBuilder
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

object DebugUtils {
    fun showMethodInfo(printer: Printer, element: ExecutableElement) {
        val stringBuilder = StringBuilder()
        stringBuilder.appendLine(element)
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

    fun showPropertyInfo(printer: Printer, element: VariableElement) {
        val stringBuilder = StringBuilder()
        stringBuilder.appendLine(element)
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
}