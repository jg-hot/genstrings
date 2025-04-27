package io.genstrings.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val name: String,
    val locale: String,
)
