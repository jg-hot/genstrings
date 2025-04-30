package io.genstrings.translator

import io.genstrings.model.Language
import io.genstrings.model.StringResource

interface PromptBuilder {
    fun buildPrompt(string: StringResource, language: Language): Prompt
}

class DefaultPromptBuilder : PromptBuilder {
    override fun buildPrompt(string: StringResource, language: Language): Prompt {
        return Prompt(
            instructions = buildInstructions(language),
            message = buildMessage(string),
        )
    }

    private fun buildInstructions(language: Language) = """
        You are an AI assistant that translates string resources for a mobile app.
        
        Target language: ${language.name}
        Locale: ${language.locale}
        
        Instructions:
        1. Translate the input string into the target language and locale.
        2. If context is provided, use it to guide the translation. Follow any explicit instructions given in the context.
        3. Preserve the original string's formatting:
          - Maintain case.
          - Preserve newlines and spacing.
        4. Leave format placeholders e.g. {1} unchanged and in a grammatically correct position for the target language.
        5. Output only the translated string. Do not include any markdown formatting, comments, or labels.
    """.trimIndent()

    private fun buildMessage(string: StringResource) = buildString {
        val context = buildContext(string)
        if (context.isNotEmpty()) {
            appendLine("Context:")
            context.forEach {
                appendLine(it)
            }
            appendLine()
        }

        appendLine("Input string:")
        appendLine("```")
        appendLine(string.text)
        appendLine("```")
    }

    private fun buildContext(string: StringResource) = buildList {
        if (string.context != null) {
            add(string.context)
        }
        string.formatArgs.forEach { arg ->
            if (arg.context != null) {
                add("{${arg.position}}: ${arg.context}")
            }
        }
    }
}
