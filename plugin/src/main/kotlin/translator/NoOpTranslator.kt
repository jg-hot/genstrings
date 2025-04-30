package io.genstrings.translator

import io.genstrings.model.Language
import io.genstrings.model.StringResource

class NoOpTranslator : Translator {

    override fun translate(string: StringResource, language: Language): String {
        return string.text
    }
}
