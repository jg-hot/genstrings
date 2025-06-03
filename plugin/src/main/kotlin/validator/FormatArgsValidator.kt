package io.genstrings.validator

import io.genstrings.model.StringResource

// verifies that all format args in the input appear in the output
class FormatArgsValidator : Validator {

    private val formatArgsRegex = "\\{(\\d+)}".toRegex()

    override fun validate(string: StringResource, translatedText: String): ValidationResult {
        val inputFormatArgs = findSortedFormatArgs(string.text)
        val outputFormatArgs = findSortedFormatArgs(translatedText)

        return if (inputFormatArgs == outputFormatArgs) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("""
                Format arg mismatch:
                Expected: $inputFormatArgs
                Found in translation: $outputFormatArgs
            """.trimIndent())
        }
    }

    private fun findSortedFormatArgs(string: String): List<Int> {
        return formatArgsRegex.findAll(string)
            .map {
                it.groupValues[1].toInt()
            }
            .sorted()
            .toList()
    }
}
