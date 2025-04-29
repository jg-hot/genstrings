package io.genstrings.model

import kotlinx.serialization.Serializable

@Serializable
data class StringsTemplate(
    val strings: List<StringResource>,

    val targetLanguages: List<Language>,
) {
    val translatableStrings: List<StringResource>
        get() = strings.filter {
            it.translatable != false
        }
}
