package io.genstrings.action

import com.charleskorn.kaml.decodeFromStream
import io.genstrings.common.Serializers
import io.genstrings.common.encodeAndroidFormatArgs
import io.genstrings.common.encodeRawAndroidString
import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.model.StringsTemplate
import io.genstrings.model.TranslationList
import io.genstrings.model.toSourceKey
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.nameWithoutExtension

class ProcessStringsYamlToXmlAction(
    private val templatePath: Path,
    private val locales: Set<String>,
    private val outputDir: Path,
) {
    private val name = "${templatePath.nameWithoutExtension}.xml"

    private val template = Files.newInputStream(templatePath).use {
        Serializers.yaml.decodeFromStream<StringsTemplate>(it)
    }

    private val stringsByKey = template.strings.associateBy { it.toSourceKey() }

    fun execute() {
        writeTemplateXml()
        locales.forEach {
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

        OutputStreamWriter(Files.newOutputStream(outputPath)).use {
            writeAndroidStringsXml(
                outputPath = outputPath,
                items = template.strings,
                provideStringResource = { it },
                provideValue = { it.text },
            )
        }
    }

    private fun writeTranslationXml(
        locale: String,
    ) {
        val inputPath = templatePath.parent
            .resolve("translations")
            .resolve(
                "${templatePath.nameWithoutExtension}-${locale}.yaml"
            )

        val outputPath = outputDir
            .resolve("values-${locale}")
            .resolve(name)
            .apply {
                createParentDirectories()
            }

        val translations = Files.newInputStream(inputPath).use {
            Serializers.yaml.decodeFromStream<TranslationList>(it).translations
        }

        OutputStreamWriter(Files.newOutputStream(outputPath)).use {
            writeAndroidStringsXml(
                outputPath = outputPath,
                items = translations,
                provideStringResource = { stringsByKey[it.source]!! },
                provideValue = { it.translation + it }
            )
        }
    }

    private fun <T> writeAndroidStringsXml(
        outputPath: Path,
        items: List<T>,
        provideStringResource: (T) -> StringResource,
        provideValue: (T) -> String,
    ) {
        OutputStreamWriter(Files.newOutputStream(outputPath)).use {
            it.append("<resources>")
            it.appendLine()
            for (item in items) {
                val string = provideStringResource(item)
                val value = provideValue(item)
                it.append("    ")
                it.append(buildAndroidStringXmlEntry(string, value))
                it.appendLine()
            }
            it.append("</resources>")
            it.appendLine()
        }
    }

    private fun buildAndroidStringXmlEntry(string: StringResource, value: String) = buildString {
        append("<string name=\"${string.name}\">")
        append(
            value
                .encodeAndroidFormatArgs(string.formatArgs)
                .encodeRawAndroidString()
        )
        append("</string>")
    }
}
