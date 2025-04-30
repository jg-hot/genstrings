package io.genstrings.translator

import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.model.Translation

interface Translator {
    fun translate(string: StringResource, language: Language) : String
}
