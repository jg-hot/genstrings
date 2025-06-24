package io.genstrings.sample

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.os.LocaleList
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceLocales = getDeviceLocales()

        // below code is unreliable; use injected genstrings_* from strings.xml to see what was actually resolved
        // val appLocale = resources.configuration.locales.get(0)

        val strings = mapOf(
            "genstrings_language" to R.string.genstrings_language,
            "hello" to R.string.hello,
            "submit_button" to R.string.submit_button,
            "brand_name" to R.string.brand_name,
            "electric_charge" to R.string.electric_charge,
            "billing_charge" to R.string.billing_charge,
        ).mapValues { (_, resId) ->
            getString(resId)
        }
        val deviceLocalesToDisplay = buildString {
            appendLine("Device Locales:")
            deviceLocales.take(3).forEachIndexed { idx, locale ->
                appendLine("${idx + 1}. ${locale.format()}")
            }
            if (deviceLocales.size > 3) {
                val n = deviceLocales.size - 3
                appendLine("+ $n more...")
            }
            appendLine("DEFAULT IS: ${Locale.getDefault().format()}")
        }
        val textToDisplay = buildString {
            appendLine(deviceLocalesToDisplay)
            strings.forEach {
                appendLine("${it.key}:\n${it.value}\n")
            }
        }
        val textView = TextView(this).apply {
            text = textToDisplay
            textSize = 18f
            setPadding(24, 24, 24, 24)
        }
        setContentView(textView)
    }

    private fun getDeviceLocales(): List<Locale> {
        // https://stackoverflow.com/questions/4212320/get-the-current-language-in-device
        // https://medium.com/@hectorricardomendez/how-to-get-the-current-locale-in-android-fc12d8be6242

        // our goal is to get the exact list of languages specified in the system settings app
        // to detect what the user's top language preference is

        // several answers suggest using:
        // Resources.getSystem().configuration.locales.toList()

        // however this actually provides the language *displayed by the system*, not the user's
        // top preference. for example Cantonese is available as a setting but can't be displayed
        // by the system in API 26-36 (tested manually).

        // in this case if the preferences looked like 1. Cantonese 2. English
        // the method above would actually return 1. English 2. Cantonese

        // and the system settings will typically show:
        // * May not be available in some apps (API 26-33)
        // * Not available as system language (API 34+)

        // the following is more reliable (again via manual testing). however use with caution if
        // there's any chance you (or a library) calls LocaleList.setDefault()
        return LocaleList.getDefault().toList()
    }

    private fun Locale.format() = "${this.getDisplayName(Locale.ENGLISH)} (${this.toLanguageTag()})"

    private fun LocaleList.toList(): List<Locale> {
        val result = mutableListOf<Locale>()
        for (i in 0..<this.size()) {
            result += this.get(i)
        }
        return result
    }
}
