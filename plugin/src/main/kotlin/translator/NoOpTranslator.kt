package io.genstrings.translator

import io.genstrings.model.StringResource

class NoOpTranslator : Translator {

    override fun translate(string: StringResource): String {
        return string.text
    }
}
