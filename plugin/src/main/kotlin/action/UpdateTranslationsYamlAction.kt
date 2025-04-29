package io.genstrings.action

import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.genstrings.common.Serializers
import io.genstrings.model.StringsTemplate
import io.genstrings.model.TranslationList
import io.genstrings.model.toSourceKey
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

// TODO: make language configurable in app build.gradle.kts
class UpdateTranslationsYamlAction(
    private val templatePath: Path,
    private val locales: Set<String>,
    private val outputDir: Path,
) {
    private val template = Files.newInputStream(templatePath).use {
        Serializers.yaml.decodeFromStream<StringsTemplate>(it)
    }

    fun execute() {
        locales.forEach {
            updateTranslationsYaml(it)
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

