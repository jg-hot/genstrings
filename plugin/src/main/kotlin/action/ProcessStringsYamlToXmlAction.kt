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

// these tags appear as specified in build.gradle.kts:

// name of the language i.e. "Chinese (Traditional Hant)"
private const val KEY_LANGUAGE = "genstrings_language"

// 2 or 3 digit language code, 1st portion of parsed qualifier i.e. "zh"
private const val KEY_LANGUAGE_CODE= "genstrings_language_code"

// full Android strings.xml locale qualifier, parsed i.e. "zh-Hant"
private const val KEY_LOCALE_TAG = "genstrings_locale_tag"

// 4-letter script specified in BCP 47 tag, if any (otherwise an empty string)
private const val KEY_SCRIPT = "genstrings_script"

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

        // this would need to be changed if writing strings.yaml in a language other than English
        val builder = AndroidStringsXmlBuilder()
        builder.addEntry(KEY_LANGUAGE, "English")
        builder.addEntry(KEY_LANGUAGE_CODE, "en")
        builder.addEntry(KEY_LOCALE_TAG, "en")
        builder.addEntry(KEY_SCRIPT, "")

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
        val parsedLocale = language.parsedLocale

        val builder = AndroidStringsXmlBuilder()
        builder.addEntry(KEY_LANGUAGE, language.name)
        builder.addEntry(KEY_LANGUAGE_CODE, parsedLocale.languageCode)
        builder.addEntry(KEY_LOCALE_TAG, parsedLocale.toString())
        builder.addEntry(KEY_SCRIPT, parsedLocale.script ?: "")

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
