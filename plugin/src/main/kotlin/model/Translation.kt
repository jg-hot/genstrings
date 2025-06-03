package io.genstrings.model

import io.genstrings.common.InstantIso8601Serializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Translation(
    val name: String,

    @Serializable(with = InstantIso8601Serializer::class)
    val timestamp: Instant,

    val metadata: Map<String, String> = emptyMap(),

    val promptBuilderId: String,

    val appContextHash: String?,

    val source: SourceKey,

    val translation: String,
)
