package io.genstrings.model

import kotlinx.serialization.Serializable

@Serializable
data class TranslationList(
    val translations: List<Translation>,
)
