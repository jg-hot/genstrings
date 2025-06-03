package io.genstrings.translator

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import io.genstrings.model.GenstringsOpenAiConfig
import io.genstrings.model.Language
import io.genstrings.model.StringResource
import kotlinx.coroutines.runBlocking

class OpenAiTranslator(
    private val config: GenstringsOpenAiConfig,
    private val promptBuilder: PromptBuilder,
) : Translator {

    private val client = OpenAI(
        token = config.apiKey,
        logging = LoggingConfig(LogLevel.None),
    )

    override fun translate(
        string: StringResource, appContext: String?, language: Language, onPreTranslate: (String?) -> Unit,
    ): TranslationOutput {
        val prompt = promptBuilder.buildPrompt(string, appContext, language)

        val request = ChatCompletionRequest(
            model = ModelId(config.model),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = prompt.instructions,
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt.message,
                ),
            ),
            temperature = config.temperature,
        )
        onPreTranslate.invoke(prompt.message)

        val completion = runBlocking {
            client.chatCompletion(request)
        }
        val text = completion.choices.firstOrNull()?.message?.content
            ?: throw Exception("OpenAI response did not include a message")
        val metadata = mapOf(
            "provider" to "openai",
            "model" to config.model,
            "temperature" to config.temperature.toString(),
        )
        return TranslationOutput(text, metadata)
    }
}
