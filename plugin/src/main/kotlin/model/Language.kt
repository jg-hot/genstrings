package io.genstrings.model

import io.genstrings.common.AndroidLocaleQualifier
import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val name: String,
    val locale: String,
) : java.io.Serializable {

    val parsedLocale: AndroidLocaleQualifier
        get() = AndroidLocaleQualifier.parse(locale)
}
