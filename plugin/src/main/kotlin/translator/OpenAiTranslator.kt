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
        string: StringResource,
        appContext: String?,
        language: Language,
        onPreTranslate: (String?) -> Unit,
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

    // https://openai.com/api/pricing/
    //
    // TODO: let user provide up-to-date pricing info in config.yaml
    override fun estimateTranslationCost(
        string: StringResource, appContext: String?, language: Language
    ): Double {
        val (inputCostPerToken, outputCostPerToken) = when (config.model) {
            "gpt-4.1" ->
                (2.00 / 1E6) to (8.00 / 1E6)

            else ->
                throw Exception("Pricing unavailable for model: ${config.model}")
        }

        val prompt = promptBuilder.buildPrompt(string, appContext, language)
        val inputTokenCount = estimateTokenCount(prompt.instructions + prompt.message)

        // assuming the output will be similar in length to the input in English
        val outputTokenCount = estimateTokenCount(string.text)

        return (inputCostPerToken * inputTokenCount) + (outputCostPerToken * outputTokenCount)
    }

    // https://help.openai.com/en/articles/4936856-what-are-tokens-and-how-to-count-them
    //
    // this can be improved in the future by using tiktoken
    private fun estimateTokenCount(text: String): Double {
        return text.length / 4.0
    }
}
