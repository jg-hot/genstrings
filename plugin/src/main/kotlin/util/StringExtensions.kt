package io.genstrings.util

import java.util.Locale

fun String.cap() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}
