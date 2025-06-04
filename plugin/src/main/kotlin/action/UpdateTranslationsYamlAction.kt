package io.genstrings.action

import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.genstrings.common.Serializers
import io.genstrings.model.Language
import io.genstrings.model.StringsTemplate
import io.genstrings.model.TranslationList
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

class UpdateTranslationsYamlAction(
    private val templatePath: Path,
    private val languages: List<Language>,
    private val outputDir: Path,
) {
    private val template = Files.newInputStream(templatePath).use {
        StringsTemplate.decodeAndPostProcess(it)
    }

    fun execute() {
        languages.forEach {
            updateTranslationsYaml(it.locale)
        }
    }

    private fun updateTranslationsYaml(locale: String) {
        val outputPath = outputDir.resolve(
            "${templatePath.nameWithoutExtension}-${locale}.yaml"
        )
        val existing = if (Files.exists(outputPath)) {
            Files.newInputStream(outputPath).use {
                Serializers.yaml.decodeFromStream<TranslationList>(it).translations
            }
        } else emptyList()

        val existingByKey = existing.associateBy { it.source }
        val output = template.buildTranslationList(existingByKey)

        Files.newOutputStream(outputPath).use {
            Serializers.yaml.encodeToStream(output, it)
        }
    }
}

