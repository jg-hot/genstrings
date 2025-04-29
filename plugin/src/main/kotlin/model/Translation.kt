package io.genstrings.model

import com.charleskorn.kaml.MultiLineStringStyle
import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.YamlMultiLineStringStyle
import com.charleskorn.kaml.YamlSingleLineStringStyle
import kotlinx.serialization.Serializable

@Serializable
data class Translation(
    val name: String,

    val source: SourceKey,

    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Literal)
    val translation: String,

    // TODO: use custom KSerializer to persist date / time
    val timestamp: String,
)
