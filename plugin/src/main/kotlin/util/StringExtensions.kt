package io.genstrings.util

import java.util.Locale

fun String.cap() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

fun String.indent(indentation: String = "    ") =
    this.lineSequence().joinToString("\n") { indentation + it }
