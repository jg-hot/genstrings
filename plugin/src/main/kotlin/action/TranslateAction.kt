package io.genstrings.action

import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.genstrings.common.Serializers
import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.model.StringsTemplate
import io.genstrings.model.Translation
import io.genstrings.model.TranslationList
import io.genstrings.model.toSourceKey
import io.genstrings.translator.UuidTestTranslator
import io.genstrings.translator.Translator
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

// TODO: store prompt hash alongside translation

// TODO: store string name in translation strings.yaml?

// TODO: serialize timestamp properly

// TODO: remove target languages from StringsTemplate

// TODO: ensure newlines in .yaml multline strings are handled properly
class TranslateAction(
    private val configFile: Path,
    private val templateFiles: List<Path>,
    private val languages: List<Language>,
) {

    fun execute() {
        val directives = templateFiles.flatMap { templateFile ->
            languages.flatMap { language ->
                buildTranslationDirectives(templateFile, language)
            }
        }
        directives.forEach {
            println(it)
        }
        println("${directives.size} strings to translate")
        readln()

        translate(directives)
    }

    private fun buildTranslationDirectives(
        templateFile: Path,
        language: Language,
    ) : List<TranslationDirective> {
        val template = Files.newInputStream(templateFile).use {
            Serializers.yaml.decodeFromStream<StringsTemplate>(it)
        }

        val translationFile = templateFile
            .parent
            .resolve("translations")
            .resolve("${templateFile.nameWithoutExtension}-${language.locale}.yaml")

        val existingTranslations = Files.newInputStream(translationFile).use {
            Serializers.yaml.decodeFromStream<TranslationList>(it).translations
        }.associateBy { it.source }

        val outputTranslations = existingTranslations.toMutableMap()

        // checkpoint: write the entire translation list, respecting original template order
        val onComplete: (Translation) -> Unit = { translation ->
            outputTranslations[translation.source] = translation

            val output = TranslationList(
                template.translatableStrings.mapNotNull { string ->
                    outputTranslations[string.toSourceKey()]
                }
            )
            Files.newOutputStream(translationFile).use {
                Serializers.yaml.encodeToStream(output, it)
            }
        }

        val untranslatedStrings = template.translatableStrings.filter {
            !existingTranslations.containsKey(it.toSourceKey())
        }
        return untranslatedStrings.map {
            TranslationDirective(
                source = it,
                language = language,
                translationFile = translationFile,
                onComplete = onComplete,
            )
        }
    }

    private fun translate(directives: List<TranslationDirective>) {
        val translator: Translator = UuidTestTranslator()
        directives.forEach { directive ->
            try {
                println("Translate: ${directive.source.text}")
                val translation = translator.translate(directive.source)
                directive.onComplete(translation)
                println("Done")
                readln()
            } catch (ex: Exception) {
                println("Failed to translate: $ex")
            }
        }
    }
}

private data class TranslationDirective(
    val source: StringResource,
    val language: Language,
    val translationFile: Path,
    val onComplete: (Translation) -> Unit,
)
