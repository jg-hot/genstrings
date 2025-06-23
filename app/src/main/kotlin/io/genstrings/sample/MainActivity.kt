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

        // https://stackoverflow.com/questions/4212320/get-the-current-language-in-device
        // https://medium.com/@hectorricardomendez/how-to-get-the-current-locale-in-android-fc12d8be6242
        val deviceLocale = Resources.getSystem().configuration.locales.get(0)

        // this isn't very useful; use genstrings_language to see what was actually resolved
        val appLocale = resources.configuration.locales.get(0)

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
        val textToDisplay = buildString {
            appendLine("Device Locale:\n${deviceLocale.format()}\n")
            appendLine("App Locale:\n${appLocale.format()}\n")
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

    private fun Locale.format() = "${this.getDisplayName(Locale.ENGLISH)} (${this.toLanguageTag()})"
}
