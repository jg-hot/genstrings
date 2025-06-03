package io.genstrings.validator

import io.genstrings.model.StringResource

interface Validator {
    fun validate(string: StringResource, translatedText: String): ValidationResult
}

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(
        val reason: String,
    ) : ValidationResult
}
