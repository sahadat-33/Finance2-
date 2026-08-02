package com.example.ui

import java.util.Locale

fun evaluateMathExpression(expression: String): String {
    val clean = expression.replace("\\s+".toRegex(), "")
    if (clean.isEmpty()) return ""
    try {
        var result = 0.0
        var currentNumberStr = ""
        var currentSign = 1
        for (char in clean) {
            if (char == '+') {
                if (currentNumberStr.isNotEmpty()) {
                    result += currentSign * (currentNumberStr.toDoubleOrNull() ?: 0.0)
                    currentNumberStr = ""
                }
                currentSign = 1
            } else if (char == '-') {
                if (currentNumberStr.isNotEmpty()) {
                    result += currentSign * (currentNumberStr.toDoubleOrNull() ?: 0.0)
                    currentNumberStr = ""
                }
                currentSign = -1
            } else if (char.isDigit() || char == '.') {
                currentNumberStr += char
            } else {
                return expression
            }
        }
        if (currentNumberStr.isNotEmpty()) {
            result += currentSign * (currentNumberStr.toDoubleOrNull() ?: 0.0)
        }
        return if (result % 1.0 == 0.0) result.toInt().toString() else String.format(Locale.US, "%.2f", result)
    } catch (e: Exception) {
        return expression
    }
}
