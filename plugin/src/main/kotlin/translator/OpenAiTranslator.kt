package io.genstrings.translator

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.genstrings.model.GenstringsOpenAiConfig
import io.genstrings.model.Language
import io.genstrings.model.StringResource
import kotlinx.coroutines.runBlocking

class OpenAiTranslator(
    private val config: GenstringsOpenAiConfig,
) : Translator {

    private val client = OpenAI(token = config.apiKey)

    override fun translate(string: StringResource, language: Language): String {
        val instructions = buildInstructions()
        val message = buildMessage(string, language)

        val request = ChatCompletionRequest(
            model = ModelId(config.model),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = instructions,
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = message,
                ),
            )
        )
        val completion = runBlocking {
            client.chatCompletion(request)
        }
        return completion.choices.firstOrNull()?.message?.content
            ?: throw Exception("OpenAI response did not include a message")
    }

    private fun buildInstructions() = """
        You are an AI assistant translating string resources for a mobile app.

        1. Translate each input string into the target language / locale.
        2. Consider the string's context, if given. Follow any explicit instructions given in the context.
        3. Preserve newlines.
        4. Preserve case.
        5. Some strings may contain format strings, e.g. {1}. These are placeholders. Do not rewrite them, but place them in the correct position in the output string (with respect to the target language's grammar).
        6. Output only the translated string.
    """.trimIndent()

    private fun buildMessage(string: StringResource, language: Language) = buildString {
        append(
            """
            Target language: ${language.name} (locale: ${language.locale})
        """.trimIndent()
        )
        appendLine()

        if (string.context != null) {
            append(
                """
                Context: ${string.context}
            """.trimIndent()
            )
            appendLine()
        }
        append(
            """
            Input string:
            ```
            ${string.text}
            ```
        """.trimIndent()
        )
    }
}
