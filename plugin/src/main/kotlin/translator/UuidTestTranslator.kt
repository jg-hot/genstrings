package io.genstrings.translator

import io.genstrings.model.StringResource
import java.util.UUID

class UuidTestTranslator : Translator {

    override fun translate(string: StringResource): String {
        return UUID.randomUUID().toString()
    }
}
