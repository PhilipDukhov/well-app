package com.well.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.aakira.napier.Napier
import com.well.shared.Greeting

fun greet(): String {
    return Greeting().greeting()
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        Napier.v("Hello napier android" + Greeting().greeting())
//        throw RuntimeException("Test Crash")
    }
}
