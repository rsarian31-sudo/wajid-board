package com.wajidboard.keyboard

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
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
            setBackgroundColor(Color.rgb(18, 18, 18))
        }
        val title = TextView(this).apply {
            text = "WAJID BOARD  •  0.1.0"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Minimal • Offline-first • Personal keyboard"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }
        root.addView(title, match())
        root.addView(subtitle, match())
        root.addView(actionButton("1  Enable WAJID BOARD") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) })
        root.addView(actionButton("2  Select WAJID BOARD") {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        })
        val privacy = TextView(this).apply {
            text = "PRIVACY\nThis prototype requests no Internet permission. Typed text is not collected or transmitted. Clipboard history, cloud sync and analytics are not included."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 28, 0, 0)
        }
        root.addView(privacy, match())
        setContentView(root)
    }
    private fun match() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
    }
}