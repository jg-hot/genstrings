import android.os.LocaleList
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

import io.genstrings.languagechecker.R

private const val TAG = "LanguageChecker"
private const val DEFAULT_LANGUAGE = "default"

@RunWith(AndroidJUnit4::class)
class LanguageChecker {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun logResolvedLanguages() {
        val (locales, languages) = resolveLanguagesAndLocales()

        Log.i(TAG, "# of Locales: ${locales.size}")
        Log.i(TAG, "# of Language Families: ${languages.size}")

        languages.forEach { language ->
            Log.i(TAG, language.toString())
            if (language.resolvedLanguages.size == 1) {
                val resolved = language.resolvedLanguages.first()
                val level = logLevelFor(language.languageCode, resolved)
                Log.println(level, TAG, "    -> $resolved")
            } else {
                language.locales.forEach { locale ->
                    val resolved = locale.resolvedLanguage
                    val level = logLevelFor(locale.languageCode, resolved)
                    Log.println(level, TAG, "    * $locale -> ${locale.resolvedLanguage}")
                }
            }
        }
    }

    private fun isWarning(languageCode: String, resolvedLanguage: String) : Boolean {
        return languageCode != "en" && resolvedLanguage == DEFAULT_LANGUAGE
    }

    private fun logLevelFor(languageCode: String, resolvedLanguage: String) : Int {
        return if (isWarning(languageCode, resolvedLanguage)) {
            Log.ERROR
        } else {
            Log.VERBOSE
        }
    }

    @Test
    fun printLanguageDirectives() {
        val (_, languages) = resolveLanguagesAndLocales()

        Log.i(TAG, "//---------- BEGIN LANGUAGE DIRECTIVES DUMP ----------")
        languages.forEach { language ->
            val directives = if (language.scripts.isEmpty()) {
                // covers most cases. we'll use the legacy-style code with the language's name
                listOf(
                    buildLanguageDirective(language)
                )
            } else if (language.scripts.size == 1) {
                // 1 script. use the legacy code, but add the script name to the description
                listOf(
                    buildLanguageDirective(language, language.scripts.first())
                )
            } else {
                language.scripts.map { script ->
                    buildLanguageDirective(language, script, useBcp47Tag = true)
                }
            }
            directives.forEach {
                Log.i(TAG, it)
            }
        }
        Log.i(TAG, "//---------- END LANGUAGE DIRECTIVES DUMP ----------")
    }

    private fun buildLanguageDirective(
        language: LanguageInfo,
        script: ScriptInfo? = null,
        useBcp47Tag: Boolean = false,
    ) : String {
        val tag = if (useBcp47Tag) {
            buildString {
                append("b+")
                append(language.languageCode)
                if (script != null) {
                    append("+")
                    append(script.code)
                }
            }
        } else {
            language.languageCode
        }
        val description = if (script != null) {
            "${language.languageName} (${script.name})"
        } else {
            language.languageName
        }
        return """
            create("$tag") { description = "$description" }
        """.trim()
    }

    private fun resolveLanguagesAndLocales(): Pair<List<LocaleInfo>, List<LanguageInfo>> {
        val locales = Locale.getAvailableLocales().map { locale ->
            val config = context.resources.configuration
            config.setLocales(LocaleList(locale))

            val overrideContext = context.createConfigurationContext(config)
            val resolvedLanguage = overrideContext.getString(R.string.genstrings_language)

            locale.toInfo(resolvedLanguage)
        }

        val languages = locales.groupBy { it.languageCode }.map { (languageCode, locales) ->
            val languageName = locales
                .map { it.languageName }
                .toSet()
                .joinToString(", ")

            val scripts = locales
                .mapNotNull { it.script }
                .toSet()

            val resolvedLanguages = locales
                .map { it.resolvedLanguage }
                .toSet()

            LanguageInfo(
                languageCode = languageCode,
                languageName = languageName,
                scripts = scripts,
                locales = locales,
                resolvedLanguages = resolvedLanguages,
            )
        }
        return locales to languages
    }
}

private data class LocaleInfo(
    val languageCode: String,
    val script: ScriptInfo?,
    val region: String?,
    val variant: String?,

    val languageName: String,
    val displayName: String,
    val languageTag: String,

    val resolvedLanguage: String,
) {
    override fun toString() = "$languageTag: $displayName"
}

private data class ScriptInfo(
    val code: String,
    val name: String,
)

private data class LanguageInfo(
    val languageCode: String,
    val languageName: String,
    val scripts: Set<ScriptInfo>,
    val resolvedLanguages: Set<String>,
    val locales: List<LocaleInfo>,
) {
    override fun toString() = buildString {
        append("$languageCode: $languageName")
        if (scripts.isNotEmpty()) {
            append(" ")
            append(scripts)
        }
    }
}

private fun Locale.toInfo(resolvedLanguage: String) = LocaleInfo(
    languageCode = this.language,
    script = if (!this.script.isNullOrBlank()) {
        ScriptInfo(this.script, this.getDisplayScript(Locale.ENGLISH) )
    } else null,
    region = this.country.takeIf { !it.isNullOrBlank() },
    variant = this.variant.takeIf { !it.isNullOrBlank() },

    languageName = this.getDisplayLanguage(Locale.ENGLISH),
    displayName = this.getDisplayName(Locale.ENGLISH),
    languageTag = this.toLanguageTag(),

    resolvedLanguage = resolvedLanguage,
)
