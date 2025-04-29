package io.genstrings.sample

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        R.string.test_string
        R.string.test_string_2
    }
}
