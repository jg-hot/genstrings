package io.genstrings.command

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

fun templateImpl(sourcePath: Path) {
    val xml = Files.newInputStream(sourcePath).use {
        XmlMapper().readTree(it).get("string")
    }
    val elements = when {
        xml == null -> emptyList()
        xml.isArray -> xml.elements().asSequence().toList()
        else -> listOf(xml)
    }
    if (elements.isEmpty()) {
        println("Source file doesn't contain any Android string resources")
        return
    }
    val template = elements.map { node ->
        val name = node.get("name")?.asText()
        val rawText = node.get("")?.asText()
        val translatable = node.get("translatable")?.asBoolean()
        require(name != null && rawText != null) {
            "Invalid string in source file: $node"
        }
        val (text, _) = rawText
            .decodeRawAndroidString()
            .decodeAndroidFormatArgs()
        StringResource(
            name = name,
            text = text,
            translatable = translatable,
        )
    }.let {
        StringsTemplate(it)
    }
    val templatePath = resolveTemplatePath(sourcePath)
    println("Writing template file to: $templatePath")

    templatePath.createParentDirectories()
    Files.newOutputStream(templatePath).use {
        Serializers.yaml.encodeToStream(template, it)
    }
}
