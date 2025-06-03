package io.genstrings.action

import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.genstrings.common.Hasher
import io.genstrings.common.Serializers
import io.genstrings.model.GenstringsConfig
import io.genstrings.model.Language
import io.genstrings.model.StringResource
import io.genstrings.model.StringsTemplate
import io.genstrings.model.Translation
import io.genstrings.model.TranslationList
import io.genstrings.model.toSourceKey
import io.genstrings.translator.DefaultPromptBuilder
import io.genstrings.translator.OpenAiTranslator
import io.genstrings.translator.Translator
import io.genstrings.util.indent
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.nameWithoutExtension

// TODO: add format args validation (each format arg should appear the same number of times in the response) or fail translation
class TranslateAction(
    private val configFile: Path,
    private val templateFiles: List<Path>,
    private val languages: List<Language>,
    private val stringNames: Set<String>?,
    private val retranslate: Boolean,
) {
    private val promptBuilder = DefaultPromptBuilder()
    private val translator = buildTranslator()

    fun execute() {
        val directives = templateFiles.flatMap { templateFile ->
            languages.flatMap { language ->
                buildTranslationDirectives(templateFile, language)
            }
        }
        println("${directives.size} strings to translate")
        readln()

        translate(directives)
    }

    private fun buildTranslator(): Translator {
        val config = try {
            Files.newInputStream(configFile).use {
                Serializers.yaml.decodeFromStream<GenstringsConfig>(it)
            }
        } catch (ex: Exception) {
            println("Failed to parse config.yaml: $configFile")
            throw ex
        }
        require(config.openAi != null) {
            "config.yaml file doesn't include an open_ai configurations"
        }
        return OpenAiTranslator(config.openAi, promptBuilder)
    }

    private fun buildTranslationDirectives(
        templateFile: Path,
        language: Language,
    ): List<TranslationDirective> {
        val template = Files.newInputStream(templateFile).use {
            StringsTemplate.decodeAndPostProcess(it)
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
            val output = template.buildTranslationList(outputTranslations)

            Files.newOutputStream(translationFile).use {
                Serializers.yaml.encodeToStream(output, it)
            }
        }

        val untranslatedStrings = template.translatableStrings.asSequence()
            .filter { string ->
                stringNames == null || string.name in stringNames
            }
            .filter { string ->
                if (!retranslate) {
                    !existingTranslations.containsKey(string.toSourceKey())
                } else true
            }
        return untranslatedStrings.map {
            TranslationDirective(
                string = it,
                appContext = template.appContext,
                language = language,
                onComplete = onComplete,
            )
        }.toList()
    }

    private fun translate(directives: List<TranslationDirective>) {
        directives.forEach { directive ->
            try {
                println("Translate string: ${directive.string.name} -> ${directive.language.name} (${directive.language.locale})")
                println()

                val output = translator.translate(
                    string = directive.string,
                    appContext= directive.appContext,
                    language = directive.language,
                    onPreTranslate = { inputLog ->
                        val message = (inputLog ?: directive.string.text).indent()
                        println("Input:")
                        println(message)
                        println()
                    },
                )
                println("Output:")
                println(output.translatedText.indent())
                println()

                val translation = Translation(
                    name = directive.string.name,
                    timestamp = Instant.now(),
                    promptBuilderId = promptBuilder.promptBuilderId,
                    appContextHash = directive.appContext?.let {
                        Hasher.sha256Sum(it)
                    },
                    metadata = output.metadata,
                    source = directive.string.toSourceKey(),
                    translation = output.translatedText,
                )
                directive.onComplete(translation)
            } catch (ex: Exception) {
                println("Output:")
                println("Failed to translate: ${ex.message}".indent())
            }
        }
    }
}

private data class TranslationDirective(
    val string: StringResource,
    val appContext: String?,
    val language: Language,
    val onComplete: (Translation) -> Unit,
)
