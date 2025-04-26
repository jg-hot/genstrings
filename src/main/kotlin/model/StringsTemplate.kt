package io.genstrings.model

import kotlinx.serialization.Serializable

@Serializable
data class StringsTemplate(
    val strings: List<StringResource>,
)
