package com.wajidboard.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class WajidKeyboardService : InputMethodService() {
    private var bengali = false
    private var symbols = false
    private var shift = false
    private var pendingRoman = ""
    private var root: LinearLayout? = null

    private val englishRows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
    private val banglaRows = listOf("ঙযডপটচজহগড়", "োেিুূপড়কত", "মনতলসবদফ")
    private val symbolsRows = listOf("1234567890", "@#\$%&-*+()", "!\"':;/?")
    private val wordMap = mapOf(
        "ami" to "আমি", "tumi" to "তুমি", "apni" to "আপনি", "amar" to "আমার",
        "bhalo" to "ভালো", "bangla" to "বাংলা", "kemon" to "কেমন", "dhonnobad" to "ধন্যবাদ",
        "valo" to "ভালো", "tomar" to "তোমার", "wajid" to "ওয়াজিদ", "assalamu" to "আসসালামু"
    )

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(5, 8, 5, 8)
            setBackgroundColor(Color.rgb(24, 24, 24))
        }
        renderKeyboard()
        return root!!
    }
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        pendingRoman = ""
    }
    private fun renderKeyboard() {
        val panel = root ?: return
        panel.removeAllViews()
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        toolbar.addView(label("WAJID BOARD", 13f, Color.LTGRAY), LinearLayout.LayoutParams(0, 38, 1f))
        toolbar.addView(smallButton(if (bengali) "বাংলা" else "EN") { bengali = !bengali; pendingRoman = ""; renderKeyboard() })
        toolbar.addView(smallButton("⚙") { requestSettings() })
        panel.addView(toolbar)
        val rows = when { symbols -> symbolsRows; bengali -> banglaRows; else -> englishRows }
        rows.forEachIndexed { index, row ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, 3, 0, 3) }
            if (index == 2) line.addView(keyButton(if (shift) "⇧" else "↑", true) { shift = !shift; renderKeyboard() }, weight(1.15f))
            row.forEach { char ->
                val value = if (!bengali && shift && !symbols) char.uppercase() else char.toString()
                line.addView(keyButton(value, false) { typeKey(value) }, weight(1f))
            }
            if (index == 2) line.addView(keyButton("⌫", true) { backspace() }, weight(1.15f))
            panel.addView(line)
        }
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, 3, 0, 0) }
        bottom.addView(keyButton("?123", true) { symbols = !symbols; renderKeyboard() }, weight(1.25f))
        bottom.addView(keyButton("🌐", true) { bengali = !bengali; symbols = false; pendingRoman = ""; renderKeyboard() }, weight(1f))
        bottom.addView(keyButton("English / বাংলা", true) { typeKey(" ") }, weight(3.8f))
        bottom.addView(keyButton(".", true) { typeKey(".") }, weight(1f))
        bottom.addView(keyButton("↵", true) { enter() }, weight(1.25f))
        panel.addView(bottom)
    }
    private fun weight(w: Float) = LinearLayout.LayoutParams(0, 48, w).apply { setMargins(2, 0, 2, 0) }
    private fun label(text: String, size: Float, color: Int) = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color); gravity = Gravity.CENTER_VERTICAL
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private fun smallButton(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text; textSize = 12f; isAllCaps = false; minWidth = 42; minHeight = 36
        setPadding(5, 0, 5, 0); setOnClickListener { action() }
    }
    private fun keyButton(text: String, special: Boolean, action: () -> Unit) = Button(this).apply {
        this.text = text; textSize = if (text.length > 2) 12f else 18f; isAllCaps = false
        setTextColor(if (special) Color.WHITE else Color.rgb(245, 242, 238))
        setBackgroundTintList(android.content.res.ColorStateList.valueOf(if (special) Color.rgb(72, 72, 72) else Color.rgb(49, 49, 49)))
        minWidth = 0; minimumWidth = 0; minHeight = 0; minimumHeight = 0
        setPadding(0, 0, 0, 0); setOnClickListener { vibrate(); action() }
    }
    private fun typeKey(value: String) {
        val ic = currentInputConnection ?: return
        if (!bengali && !symbols && value.length == 1 && value[0].isLetter()) {
            pendingRoman += value.lowercase()
            ic.commitText(value, 1)
            return
        }
        if (value == " ") {
            if (!bengali && pendingRoman.isNotEmpty()) {
                val replacement = wordMap[pendingRoman]
                if (replacement != null) {
                    ic.deleteSurroundingText(pendingRoman.length, 0)
                    ic.commitText(replacement, 1)
                }
                pendingRoman = ""
            }
            ic.commitText(" ", 1)
        } else {
            pendingRoman = ""
            ic.commitText(value, 1)
        }
        shift = false
    }
    private fun backspace() {
        val ic = currentInputConnection ?: return
        if (pendingRoman.isNotEmpty()) pendingRoman = pendingRoman.dropLast(1)
        ic.deleteSurroundingText(1, 0)
    }
    private fun enter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) ic.performEditorAction(action)
        else ic.commitText("\n", 1)
        pendingRoman = ""
    }
    private fun requestSettings() {
        requestShowSelf(0)
        try { startActivity(android.content.Intent(this, MainActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { }
    }
    private fun vibrate() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
            else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        } catch (_: Exception) { }
    }
}