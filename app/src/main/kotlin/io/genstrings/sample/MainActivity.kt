package io.genstrings.sample

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        listOf(
            R.string.hello, R.string.no_worries, R.string.submit_button,
            R.string.pro_brand_name, R.string.format_args, R.string.contextual_format_args
        ).forEach {
            Log.i("Genstrings", getString(it))
        }
    }
}
