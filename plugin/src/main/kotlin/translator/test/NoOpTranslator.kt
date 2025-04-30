package io.genstrings.translator.test

import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.translator.Translator
import io.genstrings.translator.TranslationOutput


@Suppress("unused")
class NoOpTranslator : Translator {

    override fun translate(
        string: StringResource, language: Language, onPreTranslate: (String?) -> Unit,
    ): TranslationOutput {
        val metadata = mapOf("provider" to "no-op")
        onPreTranslate.invoke(null)
        return TranslationOutput(string.text, metadata)
    }
}
