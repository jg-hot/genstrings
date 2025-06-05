package io.genstrings.action

import com.charleskorn.kaml.decodeFromStream
import io.genstrings.common.Serializers
import io.genstrings.common.encodeAndroidFormatArgs
import io.genstrings.common.encodeRawAndroidString
import io.genstrings.model.FormatArg
import io.genstrings.model.Language
import io.genstrings.model.StringsTemplate
import io.genstrings.model.TranslationList
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.nameWithoutExtension

private const val LANGUAGE_TAG = "genstrings_language"

class ProcessStringsYamlToXmlAction(
    private val templatePath: Path,
    private val languages: List<Language>,
    private val outputDir: Path,
) {
    private val name = "${templatePath.nameWithoutExtension}.xml"

    private val template = Files.newInputStream(templatePath).use {
        StringsTemplate.decodeAndPostProcess(it)
    }

    private val stringsByName = template.strings.associateBy { it.name }

    fun execute() {
        writeTemplateXml()
        languages.forEach {
            writeTranslationXml(it)
        }
    }

    private fun writeTemplateXml() {
        val outputPath = outputDir
            .resolve("values")
            .resolve(name)
            .apply {
                createParentDirectories()
            }

        val builder = AndroidStringsXmlBuilder()
        builder.addEntry(LANGUAGE_TAG, "default")

        template.strings.forEach {
            builder.addEntry(it.name, it.text, it.translatable, it.formatArgs)
        }
        Files.newBufferedWriter(outputPath).use {
            it.write(builder.build())
        }
    }

    private fun writeTranslationXml(
        language: Language,
    ) {
        val inputPath = templatePath.parent
            .resolve("translations")
            .resolve(
                "${templatePath.nameWithoutExtension}-${language.locale}.yaml"
            )

        val outputPath = outputDir
            .resolve("values-${language.locale}")
            .resolve(name)
            .apply {
                createParentDirectories()
            }

        val translations = Files.newInputStream(inputPath).use {
            Serializers.yaml.decodeFromStream<TranslationList>(it).translations
        }
        val builder = AndroidStringsXmlBuilder()
        builder.addEntry(LANGUAGE_TAG, language.name)

        translations.forEach {
            val string = stringsByName[it.name]!!
            builder.addEntry(string.name, it.translation, null, string.formatArgs)
        }
        Files.newBufferedWriter(outputPath).use {
            it.write(builder.build())
        }
    }

    private class AndroidStringsXmlBuilder {
        private val entries = mutableListOf<String>()

        fun addEntry(
            name: String, value: String, translatable: Boolean? = null,
            formatArgs: List<FormatArg> = listOf()
        ) {
            val translatableAttr = if (translatable == false) {
                " translatable=\"false\""
            } else ""

            entries += buildString {
                append("<string name=\"${name}\"$translatableAttr>")
                append(
                    value
                        .encodeAndroidFormatArgs(formatArgs)
                        .encodeRawAndroidString()
                )
                append("</string>")
            }
        }

        fun build() = buildString {
            appendLine("<resources>")
            entries.forEach { entry ->
                append("    ")
                appendLine(entry)
            }
            appendLine("</resources>")
        }
    }
}
