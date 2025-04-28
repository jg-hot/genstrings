package io.genstrings.model

import com.charleskorn.kaml.MultiLineStringStyle
import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.YamlMultiLineStringStyle
import com.charleskorn.kaml.YamlSingleLineStringStyle
import kotlinx.serialization.Serializable

@Serializable
data class StringResource(
    val translatable: Boolean? = null,

    val name: String,

    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Literal)
    val text: String,

    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Literal)
    val context: String? = null,

    val formatArgs: List<FormatArg> = emptyList()
)
