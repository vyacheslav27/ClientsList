package ru.clientslist

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText

const val MASK = "+7 ___ ___-__-__"

val maskSymbols = mapOf(
    -1 to "+7 ",
    2 to " ",
    5 to "-",
    7 to "-",
)

fun toNumberMask(string: String): String = StringBuilder().run {
    maskSymbols[-1]?.let(::append)
    for (i in string.indices) {
        append(string[i])
        maskSymbols[i]?.let(::append)
    }
    append(MASK.takeLast(MASK.length - length))

}.toString()

fun mobileNumberFilter(text: AnnotatedString, color: Color): TransformedText {
    val maxLength = 10
    val trimmed = text.text.run {
        val range = 0 until maxLength
        if (length > maxLength) substring(range) else this
    }

    val annotatedString = AnnotatedString.Builder().run {
        maskSymbols[-1]?.let(::append)
        for (i in trimmed.indices) {
            append(trimmed[i])
            maskSymbols[i]?.let(::append)
        }
        pushStyle(SpanStyle(color))
        append(MASK.takeLast(MASK.length - length))
        toAnnotatedString()
    }

    val phoneNumberOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset == 0) return 3
            if (offset < 3) return offset + 3
            if (offset <= 5) return offset + 4
            if (offset <= 7) return offset + 5
            if (offset <= 9) return offset + 6
            return 16
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (text.length <= offset) return text.length
            if (offset <= 3) return 0
            if (offset <= 5) return offset - 4
            if (offset <= 7) return offset - 5
            if (offset <= 9) return offset - 6
            return text.length
        }
    }

    return TransformedText(annotatedString, phoneNumberOffsetTranslator)
}