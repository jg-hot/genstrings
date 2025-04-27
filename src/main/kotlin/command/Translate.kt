package io.genstrings.command

import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.genstrings.common.Serializers
import io.genstrings.common.resolveTranslationListPath
import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.model.StringsTemplate
import io.genstrings.model.Translation
import io.genstrings.model.TranslationList
import io.genstrings.model.toSourceKey
import io.genstrings.translator.NoOpTranslator
import io.genstrings.translator.Translator
import java.nio.file.Files
import java.nio.file.Path

fun translate(templatePath: Path) {
    val template = Files.newInputStream(templatePath).use {
        Serializers.yaml.decodeFromStream<StringsTemplate>(it)
    }
    val commands = template.targetLanguages.map {
        buildTranslationCommands(templatePath, template, it)
    }
    commands.forEach {
        translateImpl(templatePath, it)
    }
}

fun translateImpl(
    templatePath: Path,
    command: TranslationCommand,
) {
    val outputPath = resolveTranslationListPath(templatePath, command.language)
    val outputTranslations = command.directives.map {
        when (it) {
            is TranslationDirective.UseExisting -> it.translation
            is TranslationDirective.Translate -> it.outOfDateTranslation
        }
    }.toMutableList()

    // write once to sync strings which were removed, re-ordered or have changed metadata
    writeTranslations(outputPath, outputTranslations)

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
                outputTranslations[idx] = translator.translate(directive.source)

                // checkpoint
                writeTranslations(outputPath, outputTranslations)

            } catch (ex: Exception) {
                println("Failed to translate: $ex")
            }
        }
    }
}

fun writeTranslations(
    outputPath: Path,
    translations: List<Translation?>,
) {
    val data = TranslationList(
        translations.filterNotNull()
    )
    Files.newOutputStream(outputPath).use {
        Serializers.yaml.encodeToStream(data, it)
    }
}

fun buildTranslationCommands(
    templatePath: Path,
    template: StringsTemplate,
    language: Language,
) : TranslationCommand {
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

fun buildTranslationDirectives(
    strings: List<StringResource>,
    existing: List<Translation>,
) : List<TranslationDirective> {
    val existingByKey = existing.associateBy { it.source }
    return strings.map { string ->
        val translation = existingByKey[string.toSourceKey()]
        when {
            translation != null -> TranslationDirective.UseExisting(translation)
            else -> TranslationDirective.Translate(
                source = string,
                outOfDateTranslation = translation,
            )
        }
    }
}

data class TranslationCommand(
    val language: Language,
    val outputPath: Path,
    val directives: List<TranslationDirective>,
)

sealed interface TranslationDirective {
    data class UseExisting(val translation: Translation) : TranslationDirective
    data class Translate(
        val source: StringResource,

        // this is added to support incremental updates to output file as well as updating the translation
        // output file without performing any actual translations (i.e. updating metadata)
        val outOfDateTranslation: Translation?,
    ) : TranslationDirective
}

fun translationPreRun() {
    // TODO: build prompts, count tokens, estimate cost, and confirm
}
