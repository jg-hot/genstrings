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

    fun buildTranslationList(existingTranslations: Map<SourceKey, Translation>): TranslationList {
        val outTranslations = this.translatableStrings.mapNotNull { string ->
            val translation = existingTranslations[string.toSourceKey()]
            translation?.copy(
                name = string.name
            )
        }
        return TranslationList(outTranslations)
    }
}
