package io.genstrings.model

import io.genstrings.common.InstantIso8601Serializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Translation(
    val name: String,

    // TODO: use custom KSerializer to persist date / time
    @Serializable(with = InstantIso8601Serializer::class)
    val timestamp: Instant,

    val source: SourceKey,

    val translation: String,
)
