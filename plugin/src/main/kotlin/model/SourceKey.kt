package io.genstrings.model

import kotlinx.serialization.Serializable

// includes only fields relevant to translation (original text and context)
// excludes metadata including string name and format arg specifiers
@Serializable
data class SourceKey(
    val text: String,
    val context: String? = null,
    val formatArgsContext: Map<Int, String> = mapOf(),
)

fun StringResource.toSourceKey() = SourceKey(
    text = text,
    context = context,
    formatArgsContext = formatArgs.filter {
        it.context != null
    }.associate {
        it.position to it.context!!
    }
)
