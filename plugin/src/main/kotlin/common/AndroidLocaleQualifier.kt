package io.genstrings.common

// https://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources
sealed interface AndroidLocaleQualifier {

    val languageCode: String
    val script: String?
        get() = null

    data class Default(
        override val languageCode: String,
        val region: String?,
    ) : AndroidLocaleQualifier {

        override fun toString() = listOfNotNull(
            languageCode, region
        ).joinToString("-")
    }

    data class Bcp47(
        override val languageCode: String,
        val subtags: List<String>,
    ) : AndroidLocaleQualifier {

        // https://www.rfc-editor.org/rfc/rfc5646.html#section-2.2.3
        // simplified implementation check's length but doesn't verify position
        override val script = subtags.firstOrNull { it.length == 4 }

        override fun toString() = buildList {
            add(languageCode)
            addAll(subtags)
        }.joinToString("-")
    }

    data class Invalid(
        val rawLocale: String
    ) : AndroidLocaleQualifier {

        override val languageCode = rawLocale

        override fun toString() = rawLocale
    }

    companion object {
        fun parse(value: String) : AndroidLocaleQualifier {
            return try {
                if (value.startsWith("b+")) {
                    tryParseBcp47Qualifier(value)
                } else {
                    tryParseDefaultQualifier(value)
                }
            } catch (ex: Exception) {
                Invalid(rawLocale = value)
            }
        }

        private fun tryParseBcp47Qualifier(value: String): Bcp47 {
            val components = value.substringAfter("b+").split("+")
            return Bcp47(
                languageCode = components[0],
                subtags = components.drop(1),
            )
        }

        private fun tryParseDefaultQualifier(value: String): Default {
            val components = value.split("-r")
            return Default(
                languageCode = components[0],
                region = components.getOrNull(1),
            )
        }
    }
}
