package com.wajidboard.keyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 36, 24, 36)
            setBackgroundColor(Color.rgb(18, 20, 27))
        }
        root.addView(TextView(this).apply {
            text = "WAJID BOARD  •  0.2.0"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }, match())
        root.addView(TextView(this).apply {
            text = "Avro Phonetic  •  Probhat  •  Clipboard"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }, match())
        root.addView(actionButton("1  Enable WAJID BOARD") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        root.addView(actionButton("2  Select WAJID BOARD") {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        })
        root.addView(TextView(this).apply {
            text = "HOW TO USE\nOpen any text box. Tap the top language key to cycle EN → অভ্র → প্রভাত. In Avro mode, type Bangla with English letters. In Probhat mode, use the mapped Bengali keys. Tap 📋 to view recent copied text. Clipboard history is kept only in memory while the keyboard service is running; Clear removes the visible history.\n\nPRIVACY\nNo Internet permission, analytics, cloud sync or key logging. Avro and Probhat work offline. Clipboard text is read only by the active keyboard and is not uploaded."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 28, 0, 0)
        }, match())
        setContentView(root)
    }

    private fun match() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = 12
        }
    }
}
