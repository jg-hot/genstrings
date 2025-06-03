package io.genstrings.model

import kotlinx.serialization.Serializable

@Serializable
data class GenstringsConfig(
    val openAi: GenstringsOpenAiConfig? = null
)

@Serializable
data class GenstringsOpenAiConfig(
    val apiKey: String,
    val model: String = "gpt-4.1",
    val temperature: Double = 0.2,
)
