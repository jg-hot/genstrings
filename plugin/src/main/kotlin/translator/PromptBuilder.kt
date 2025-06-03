package io.genstrings.translator

import io.genstrings.model.Language
import io.genstrings.model.StringResource

interface PromptBuilder {
    val promptBuilderId: String
    fun buildPrompt(string: StringResource, appContext: String?, language: Language): Prompt
}

class DefaultPromptBuilder : PromptBuilder {
    override val promptBuilderId = "default_v1"

    override fun buildPrompt(string: StringResource, appContext: String?, language: Language): Prompt {
        return Prompt(
            instructions = buildInstructions(language, appContext),
            message = buildMessage(string),
        )
    }

    private fun buildInstructions(language: Language, appContext: String?) = buildString {
        var i = 1
        appendLine("""
            You are an AI assistant that translates string resources for a mobile app.
            
            Target language: ${language.name}
            Locale: ${language.locale}
        """.trimIndent())

        if (appContext != null) {
            appendLine()
            appendLine("App Context:")
            appendLine(appContext)
            appendLine()
        } else {
            appendLine()
        }

        appendLine("""
            Instructions:
            ${i++}. Translate the input string into the target language and locale.
        """.trimIndent())

        if (appContext != null) {
            appendLine("""
                ${i++}. Use the app context above to guide the translation. It may define terminology, describe how the app works, or include explicit instructions.
            """.trimIndent())
            appendLine("""
                ${i++}. If string-specific context is provided, use it to guide the translation. If it conflicts with the app context, the string context takes precedence. Otherwise, consider both.
            """.trimIndent())
        } else {
            appendLine("""
                ${i++}. If string-specific context is provided, use it to guide the translation. Follow any explicit instructions given in the context.
            """.trimIndent())
        }
        appendLine("""
            ${i++}. Preserve the original string's formatting:
              - Maintain case.
              - Preserve newlines and spacing.
            ${i++}. Leave format placeholders e.g. {1} unchanged and in a grammatically correct position for the target language.
            $i. Output only the translated string. Do not include any markdown formatting, comments, or labels.
        """.trimIndent())
    }

    private fun buildMessage(string: StringResource) = buildString {
        val context = buildContext(string)
        if (context.isNotEmpty()) {
            appendLine("String Context:")
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
