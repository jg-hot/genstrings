package io.genstrings.common

import io.genstrings.model.FormatArg

// https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes
//
// CURRENTLY NOT SUPPORTED:
// * U+XXXX Unicode characters e.g. \uXXXX
// * Single quotes enclosed in double quotes ("This'll work", for example)
// * Whitespace preservation: e.g. <string>" &#32; &#8200; &#8195;"</string> or <string> \u0032 \u8200 \u8195</string>
private val androidEscapeChars = listOf(
    "\\@" to "@",
    "\\?" to "?",
    "\\n" to "\n",
    "\\t" to "\t",
    "\\'" to "'",
    "\\\"" to "\"",
)

fun String.decodeRawAndroidString(): String {
    return androidEscapeChars.fold(this) { acc, (escaped, original) ->
        acc.replace(escaped, original)
    }
}

fun String.encodeRawAndroidString(): String {
    return androidEscapeChars.fold(this) { acc, (escaped, original) ->
        acc.replace(original, escaped)
    }
}

// https://developer.android.com/guide/topics/resources/string-resource#formatting-strings
// https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html
//
// CURRENTLY ONLY SUPPORTS:
// * Simple format args mentioned by the official docs, like %1$s. Does not support flags or options e.g. %1$.2f
// * The final character must be a single Conversion specifier from Java.util.Formatter's docs (linked above)
fun String.decodeAndroidFormatArgs(): Pair<String, List<FormatArg>> {
    var decoded = this
    val formatArgs = mutableListOf<FormatArg>()

    "%(\\d+)\\$([BHSCXEGATbhscdoxefgat])".toRegex().findAll(this).forEach { match ->
        val position = match.groupValues[1].toInt()
        val type = match.groupValues[2]
        decoded = decoded.replaceFirst(match.value, "{$position}")
        formatArgs += FormatArg(position, type)
    }
    return decoded to formatArgs
}

fun String.encodeAndroidFormatArgs(formatArgs: List<FormatArg>): String {
    var encoded = this
    "\\{(\\d+)}".toRegex().findAll(this).forEach { match ->
        val position = match.groupValues[1].toInt()
        val type = formatArgs.find { it.position == position }?.type ?: "s"
        encoded = encoded.replaceFirst(match.value, "%${position}$${type}")
    }
    return encoded
}
