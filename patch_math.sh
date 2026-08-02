#!/bin/bash

# Add the evaluate function before AddTransactionDialog
sed -i '/fun AddTransactionDialog(/i \
fun evaluateMathExpression(expression: String): String {\
    val clean = expression.replace("\\\\s+".toRegex(), "")\
    if (clean.isEmpty()) return ""\
    try {\
        var result = 0.0\
        var currentNumberStr = ""\
        var currentSign = 1\
        for (char in clean) {\
            if (char == '+') {\
                if (currentNumberStr.isNotEmpty()) {\
                    result += currentSign * (currentNumberStr.toDoubleOrNull() ?: 0.0)\
                    currentNumberStr = ""\
                }\
                currentSign = 1\
            } else if (char == '-') {\
                if (currentNumberStr.isNotEmpty()) {\
                    result += currentSign * (currentNumberStr.toDoubleOrNull() ?: 0.0)\
                    currentNumberStr = ""\
                }\
                currentSign = -1\
            } else if (char.isDigit() || char == '"'"'.'"'"') {\
                currentNumberStr += char\
            } else {\
                return expression\
            }\
        }\
        if (currentNumberStr.isNotEmpty()) {\
            result += currentSign * (currentNumberStr.toDoubleOrNull() ?: 0.0)\
        }\
        return if (result % 1.0 == 0.0) result.toInt().toString() else String.format(java.util.Locale.US, "%.2f", result)\
    } catch (e: Exception) {\
        return expression\
    }\
}\
' app/src/main/java/com/example/ui/AddTransactionDialog.kt

# Update the onValueChange to allow + and -
sed -i 's/if (input.all { it.isDigit() || it == '\''.'\'' }) amountStr = input/if (input.all { it.isDigit() || it == '\''.'\'' || it == '\''+'\'' || it == '\''-'\'' }) amountStr = input/' app/src/main/java/com/example/ui/AddTransactionDialog.kt

