package io.genstrings.model

import com.charleskorn.kaml.MultiLineStringStyle
import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.YamlMultiLineStringStyle
import com.charleskorn.kaml.YamlSingleLineStringStyle
import kotlinx.serialization.Serializable

@Serializable
data class FormatArg(
    val position: Int,

    val type: String = "s",

    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Literal)
    val context: String? = null,
) {
    fun copyPostProcessed() = copy(
        context = context?.trimEnd('\n')
    )
}
