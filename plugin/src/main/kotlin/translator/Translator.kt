package io.genstrings.translator

import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.model.Translation

interface Translator {
    fun translate(
        string: StringResource,
        appContext: String?,
        language: Language,

        // parameter is an optional message to log
        onPreTranslate: (String?) -> Unit = {},
    ) : TranslationOutput

    fun estimateTranslationCost(
        string: StringResource,
        appContext: String?,
        language: Language,
    ): Double
}

data class TranslationOutput(
    val translatedText: String,
    val metadata: Map<String, String>,
)
