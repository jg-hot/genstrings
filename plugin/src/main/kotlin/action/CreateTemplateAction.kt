package io.genstrings.action

import com.charleskorn.kaml.encodeToStream
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.genstrings.common.Serializers
import io.genstrings.common.decodeAndroidFormatArgs
import io.genstrings.common.decodeRawAndroidString
import io.genstrings.common.resolveTemplatePath
import io.genstrings.model.StringResource
import io.genstrings.model.StringsTemplate
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories

class CreateTemplateAction(
    private val sourcePath: Path,
) {
    fun run() {
        val xml = Files.newInputStream(sourcePath).use {
            XmlMapper().readTree(it)
        }
        val strings = xml.get("string")
        require(strings != null) {
            "Source file doesn't contain any string resources"
        }
        val elementCount = xml.elements().asSequence().count()
        require(elementCount == 1) {
            "Source file contains other tags besides <string> (not supported)"
        }
        val elements = when {
            strings.isArray -> strings.elements().asSequence().toList()
            else -> listOf(strings)
        }
        val template = elements.map { node ->
            val name = node.get("name")?.asText()
            val rawText = node.get("")?.asText()
            val translatable = node.get("translatable")?.asBoolean()
            require(name != null && rawText != null) {
                "Invalid string in source file: $node"
            }
            val (text, formatArgs) = rawText
                .decodeRawAndroidString()
                .decodeAndroidFormatArgs()
            StringResource(
                name = name,
                text = text,
                translatable = translatable,
                formatArgs = formatArgs,
            )
        }.let {
            StringsTemplate(
                strings = it,
                targetLanguages = emptyList(),
            )
        }
        val templatePath = resolveTemplatePath(sourcePath)
        println("Writing template file to: $templatePath")

        templatePath.createParentDirectories()
        Files.newOutputStream(templatePath).use {
            Serializers.yaml.encodeToStream(template, it)
        }
    }
}
