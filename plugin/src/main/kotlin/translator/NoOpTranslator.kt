package io.genstrings.translator

import io.genstrings.model.StringResource
import io.genstrings.model.Translation
import io.genstrings.model.toSourceKey

class NoOpTranslator : Translator {

    override fun translate(string: StringResource): Translation {
        return Translation(
            source = string.toSourceKey(),
            translation = string.text,
            timestamp = System.currentTimeMillis().toString(),
        )
    }
}
