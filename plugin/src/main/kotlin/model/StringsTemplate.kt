package io.genstrings.model

import com.charleskorn.kaml.MultiLineStringStyle
import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.YamlMultiLineStringStyle
import com.charleskorn.kaml.YamlSingleLineStringStyle
import com.charleskorn.kaml.decodeFromStream
import io.genstrings.common.Serializers
import kotlinx.serialization.Serializable
import java.io.InputStream

@Serializable
data class StringsTemplate(
    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Literal)
    val appContext: String? = null,

    val strings: List<StringResource>,
) {
    val translatableStrings: List<StringResource>
        get() = strings.filter {
            it.translatable != false
        }

    fun copyPostProcessed() = copy(
        appContext = appContext?.trimEnd('\n'),
        strings = strings.map { it.copyPostProcessed() }
    )

    fun buildTranslationList(existingTranslations: Map<SourceKey, Translation>): TranslationList {
        val outTranslations = this.translatableStrings.mapNotNull { string ->
            val translation = existingTranslations[string.toSourceKey()]
            translation?.copy(
                name = string.name
            )
        }
        return TranslationList(outTranslations)
    }

    companion object {
        fun decodeAndPostProcess(input: InputStream): StringsTemplate {
            return Serializers.yaml.decodeFromStream<StringsTemplate>(input).copyPostProcessed()
        }
    }
}
