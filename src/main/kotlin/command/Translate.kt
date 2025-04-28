package io.genstrings.command

import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.genstrings.common.Serializers
import io.genstrings.common.encodeAndroidFormatArgs
import io.genstrings.common.encodeRawAndroidString
import io.genstrings.common.resolveStringsXmlPath
import io.genstrings.common.resolveTranslationListPath
import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.model.StringsTemplate
import io.genstrings.model.Translation
import io.genstrings.model.TranslationList
import io.genstrings.model.toSourceKey
import io.genstrings.translator.NoOpTranslator
import io.genstrings.translator.Translator
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories

// TODO: post translation should verify that all format specifiers still exist in the output string
fun translate(
    templatePath: Path,
    updateSource: Boolean = true,
) {
    val template = Files.newInputStream(templatePath).use {
        Serializers.yaml.decodeFromStream<StringsTemplate>(it)
    }
    if (updateSource) {
        writeAndroidStringsXml(
            templatePath = templatePath,
            language = null,
            items = template.strings,
            provideStringResource = { it },
            provideValue = { it.text },
        )
    }
    val commands = template.targetLanguages.map {
        buildTranslationCommands(templatePath, template, it)
    }
    commands.forEach {
        translateImpl(templatePath, it)
    }
}

private fun translateImpl(
    templatePath: Path,
    command: TranslationCommand,
) {
    val outputTranslations = command.directives.map {
        val translation = when (it) {
            is TranslationDirective.UseExisting -> it.translation
            is TranslationDirective.Translate -> it.outOfDateTranslation
        }
        TranslationOutput(
            string = it.source,
            translation = translation,
        )
    }.toMutableList()

    val writeTranslationsImpl: () -> Unit = {
        writeTranslationList(
            templatePath = templatePath,
            language = command.language,
            output = outputTranslations
        )
        writeAndroidStringsXml(
            templatePath = templatePath,
            language = command.language,
            items = outputTranslations,
            provideStringResource = { it.string },
            provideValue = { it.translation?.translation }
        )
    }
    // write once to sync strings which were removed, re-ordered or have changed metadata
    writeTranslationsImpl()

    if (!command.directives.any { it is TranslationDirective.Translate }) {
        println("All translations are up to date: ${command.language}")
        return
    }
    val translator: Translator = NoOpTranslator()

    // write again after every successfully translation
    command.directives.forEachIndexed { idx, directive ->
        if (directive is TranslationDirective.Translate) {
            try {
                println("Translate: ${directive.source.text}")
                outputTranslations[idx] = outputTranslations[idx].copy(
                    translation = translator.translate(directive.source)
                )
                // checkpoint
                writeTranslationsImpl()

            } catch (ex: Exception) {
                println("Failed to translate: $ex")
            }
        }
    }
}

private fun writeTranslationList(
    templatePath: Path,
    language: Language,
    output: List<TranslationOutput>,
) {
    val translationListPath = resolveTranslationListPath(templatePath, language)
    val data = TranslationList(
        output.mapNotNull { it.translation }
    )
    Files.newOutputStream(translationListPath).use {
        Serializers.yaml.encodeToStream(data, it)
    }
}

// TODO: add `translatable` flag in original file only
private fun <T> writeAndroidStringsXml(
    templatePath: Path,
    language: Language?,
    items: List<T>,
    provideStringResource: (T) -> StringResource,
    provideValue: (T) -> String?,
) {
    val xmlPath = resolveStringsXmlPath(templatePath, language)
    xmlPath.createParentDirectories()

    OutputStreamWriter(Files.newOutputStream(xmlPath)).use {
        it.append("<resources>")
        it.appendLine()
        for (item in items) {
            val string = provideStringResource(item)
            val value = provideValue(item)
            if (value == null) {
                continue
            }
            it.append("    ")
            it.append(buildAndroidStringXmlEntry(string, value))
            it.appendLine()
        }
        it.append("</resources>")
        it.appendLine()
    }
}

private fun buildTranslationCommands(
    templatePath: Path,
    template: StringsTemplate,
    language: Language,
): TranslationCommand {
    val outputPath = resolveTranslationListPath(templatePath, language)

    val existing = if (Files.exists(outputPath)) {
        Files.newInputStream(outputPath).use {
            Serializers.yaml.decodeFromStream<TranslationList>(it).translations
        }
    } else emptyList()

    val directives = buildTranslationDirectives(
        strings = template.strings.filter { it.translatable != false },
        existing = existing,
    )
    return TranslationCommand(
        language = language,
        outputPath = outputPath,
        directives = directives,
    )
}

private fun buildTranslationDirectives(
    strings: List<StringResource>,
    existing: List<Translation>,
): List<TranslationDirective> {
    val existingByKey = existing.associateBy { it.source }
    return strings.map { string ->
        val translation = existingByKey[string.toSourceKey()]
        when {
            translation != null -> TranslationDirective.UseExisting(string, translation)
            else -> TranslationDirective.Translate(
                source = string,
                outOfDateTranslation = translation,
            )
        }
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

data class TranslationCommand(
    val language: Language,
    val outputPath: Path,
    val directives: List<TranslationDirective>,
)

sealed interface TranslationDirective {
    val source: StringResource

    data class UseExisting(
        override val source: StringResource,
        val translation: Translation,
    ) : TranslationDirective

    data class Translate(
        override val source: StringResource,

        // this is added to support incremental updates to output file as well as updating the translation
        // output file without performing any actual translations (i.e. updating metadata)
        val outOfDateTranslation: Translation?,
    ) : TranslationDirective
}

data class TranslationOutput(
    val string: StringResource,
    val translation: Translation?,
)

fun translationPreRun() {
    // TODO: build prompts, count tokens, estimate cost, and confirm
}
