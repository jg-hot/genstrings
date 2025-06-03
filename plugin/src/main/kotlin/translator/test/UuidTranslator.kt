package io.genstrings.translator.test

import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.translator.Translator
import io.genstrings.translator.TranslationOutput
import java.util.UUID

@Suppress("unused")
class UuidTranslator : Translator {

    override fun translate(
        string: StringResource, appContext: String?, language: Language, onPreTranslate: (String?) -> Unit,
    ): TranslationOutput {
        val metadata = mapOf("provider" to "uuid")
        onPreTranslate.invoke(null)
        return TranslationOutput(UUID.randomUUID().toString(), metadata)
    }

    override fun estimateTranslationCost(
        string: StringResource, appContext: String?, language: Language
    ): Double {
        return 0.0
    }
}
