package io.genstrings.translator

import io.genstrings.model.StringResource
import io.genstrings.model.Translation
import io.genstrings.model.toSourceKey
import java.util.UUID

class UuidTestTranslator : Translator {

    override fun translate(string: StringResource): Translation {
        return Translation(
            source = string.toSourceKey(),
            translation = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis().toString(),
        )
    }
}
