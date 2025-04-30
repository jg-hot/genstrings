package io.genstrings.translator

import io.genstrings.model.Language
import io.genstrings.model.StringResource
import java.util.UUID

class UuidTestTranslator : Translator {

    override fun translate(string: StringResource, language: Language): String {
        return UUID.randomUUID().toString()
    }
}
