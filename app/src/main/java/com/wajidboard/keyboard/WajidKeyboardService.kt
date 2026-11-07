package com.wajidboard.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject

class WajidKeyboardService : InputMethodService() {
    private enum class Mode { ENGLISH, AVRO, PROBHAT }

    private var mode = Mode.AVRO
    private var symbols = false
    private var shifted = false
    private var clipboardPanel = false
    private var pendingRoman = ""
    private var root: LinearLayout? = null
    private var preview: TextView? = null
    private var probhat: JSONObject? = null
    private lateinit var avro: AvroPhoneticEngine
    private val clipboardHistory = ArrayList<String>()
    private var clipboardManager: ClipboardManager? = null
    private var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    private val alphaRows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
    private val symbolRows = listOf("1234567890", "@#$%&-*+()", "!\"':;/?")
    private val bg = Color.rgb(18, 20, 27)
    private val keyBg = Color.rgb(39, 44, 55)
    private val specialBg = Color.rgb(63, 72, 89)
    private val accent = Color.rgb(105, 160, 255)

    override fun onCreate() {
        super.onCreate()
        avro = AvroPhoneticEngine(this)
        val data = assets.open("probhat.json").bufferedReader().use { it.readText() }
        probhat = JSONObject(data).getJSONObject("layout")
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardListener = ClipboardManager.OnPrimaryClipChangedListener { captureClipboard() }
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(7, 8, 7, 8)
            setBackgroundColor(bg)
        }
        clipboardManager?.addPrimaryClipChangedListener(clipboardListener!!)
        renderKeyboard()
        return root!!
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        pendingRoman = ""
        preview?.text = ""
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        clipboardManager?.removePrimaryClipChangedListener(clipboardListener!!)
        clipboardPanel = false
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        clipboardListener?.let { clipboardManager?.removePrimaryClipChangedListener(it) }
        super.onDestroy()
    }

    private fun renderKeyboard() {
        val panel = root ?: return
        panel.removeAllViews()
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(2, 0, 2, 5)
        }
        val brand = TextView(this).apply {
            text = "WAJID BOARD"
            textSize = 12f
            letterSpacing = 0.12f
            setTextColor(Color.LTGRAY)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
        toolbar.addView(brand, LinearLayout.LayoutParams(0, 38, 1f))
        toolbar.addView(toolButton(modeLabel()) { cycleMode() }, toolParams())
        toolbar.addView(toolButton("📋") { toggleClipboard() }, toolParams())
        toolbar.addView(toolButton("⚙") { openSettings() }, toolParams())
        panel.addView(toolbar)

        preview = TextView(this).apply {
            textSize = 13f
            setTextColor(accent)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 0, 8, 2)
            text = if (pendingRoman.isNotEmpty() && mode == Mode.AVRO) avro.convert(pendingRoman) else ""
        }
        panel.addView(preview, LinearLayout.LayoutParams(-1, 22))

        if (clipboardPanel) renderClipboard(panel)

        val rows = when {
            symbols -> symbolRows
            else -> alphaRows
        }
        rows.forEachIndexed { index, row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(0, 3, 0, 3)
            }
            if (index == 2) line.addView(key("⇧", true) { shifted = !shifted; renderKeyboard() }, keyParams(1.15f))
            row.forEach { ch ->
                val shown = keyLabel(ch)
                line.addView(key(shown, false) { handleCharacter(ch) }, keyParams(1f))
            }
            if (index == 2) line.addView(key("⌫", true) { backspace() }, keyParams(1.15f))
            panel.addView(line)
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 3, 0, 0)
        }
        bottom.addView(key(if (symbols) "ABC" else "?123", true) { symbols = !symbols; renderKeyboard() }, keyParams(1.15f))
        bottom.addView(key("🌐", true) { cycleMode() }, keyParams(1f))
        bottom.addView(key("বাংলা / English", true) { handleSpace() }, keyParams(3.4f))
        bottom.addView(key(".", true) { handleCharacter('.') }, keyParams(0.8f))
        bottom.addView(key("↵", true) { enter() }, keyParams(1.05f))
        panel.addView(bottom)
    }

    private fun renderClipboard(panel: LinearLayout) {
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply {
            text = "সাম্প্রতিক কপি"
            setTextColor(Color.WHITE)
            textSize = 12f
        }, LinearLayout.LayoutParams(0, 30, 1f))
        header.addView(toolButton("Clear") { clipboardHistory.clear(); renderKeyboard() })
        header.addView(toolButton("×") { clipboardPanel = false; renderKeyboard() })
        panel.addView(header)
        if (clipboardHistory.isEmpty()) {
            panel.addView(TextView(this).apply {
                text = "এখনো কোনো কপি করা লেখা নেই"
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                textSize = 12f
            }, LinearLayout.LayoutParams(-1, 34))
        } else {
            clipboardHistory.take(5).forEach { item ->
                panel.addView(key(item.replace("\n", " ").take(54), true) {
                    currentInputConnection?.commitText(item, 1)
                    clipboardPanel = false
                    renderKeyboard()
                }, LinearLayout.LayoutParams(-1, 38).apply { setMargins(2, 2, 2, 2) })
            }
        }
    }

    private fun captureClipboard() {
        try {
            val clip = clipboardManager?.primaryClip ?: return
            if (clip.itemCount == 0) return
            val text = clip.getItemAt(0).coerceToText(this)?.toString()?.trim().orEmpty()
            if (text.isNotEmpty() && text.length <= 3000 && clipboardHistory.firstOrNull() != text) {
                clipboardHistory.remove(text)
                clipboardHistory.add(0, text)
                while (clipboardHistory.size > 10) clipboardHistory.removeAt(clipboardHistory.lastIndex)
                if (clipboardPanel) renderKeyboard()
            }
        } catch (_: Exception) { }
    }

    private fun toggleClipboard() {
        captureClipboard()
        clipboardPanel = !clipboardPanel
        renderKeyboard()
    }

    private fun modeLabel() = when (mode) {
        Mode.ENGLISH -> "EN"
        Mode.AVRO -> "অভ্র"
        Mode.PROBHAT -> "প্রভাত"
    }

    private fun cycleMode() {
        finishPending()
        mode = when (mode) {
            Mode.ENGLISH -> Mode.AVRO
            Mode.AVRO -> Mode.PROBHAT
            Mode.PROBHAT -> Mode.ENGLISH
        }
        symbols = false
        shifted = false
        clipboardPanel = false
        renderKeyboard()
    }

    private fun keyLabel(ch: Char): String {
        if (symbols) return ch.toString()
        return when (mode) {
            Mode.ENGLISH -> if (shifted) ch.uppercaseChar().toString() else ch.toString()
            Mode.AVRO -> if (shifted) ch.uppercaseChar().toString() else ch.toString()
            Mode.PROBHAT -> probhatValue(ch) ?: ch.toString()
        }
    }

    private fun probhatValue(ch: Char): String? {
        val keyName = if (shifted) ch.uppercaseChar() else ch
        val suffix = if (shifted) "AltGr" else "Normal"
        val value = probhat?.optString("Key_${keyName}_${suffix}").orEmpty()
        if (value.isNotEmpty()) return value
        if (shifted) {
            val fallback = probhat?.optString("Key_${ch.uppercaseChar()}_Normal").orEmpty()
            if (fallback.isNotEmpty()) return fallback
        }
        return null
    }

    private fun handleCharacter(ch: Char) {
        if (symbols) {
            commitLiteral(ch.toString())
            return
        }
        when (mode) {
            Mode.AVRO -> {
                if (ch.isLetter()) {
                    pendingRoman += if (shifted) ch.uppercaseChar() else ch
                    updateAvroComposition()
                } else commitLiteral(ch.toString())
            }
            Mode.PROBHAT -> {
                val value = probhatValue(ch) ?: ch.toString()
                commitLiteral(value)
            }
            Mode.ENGLISH -> commitLiteral(if (shifted) ch.uppercaseChar().toString() else ch.toString())
        }
        shifted = false
    }

    private fun updateAvroComposition() {
        val ic = currentInputConnection ?: return
        val converted = avro.convert(pendingRoman)
        ic.setComposingText(converted, 1)
        preview?.text = if (pendingRoman.isNotEmpty()) "$pendingRoman  →  $converted" else ""
    }

    private fun commitLiteral(value: String) {
        finishPending()
        currentInputConnection?.commitText(value, 1)
        preview?.text = ""
    }

    private fun handleSpace() {
        finishPending()
        currentInputConnection?.commitText(" ", 1)
        preview?.text = ""
    }

    private fun finishPending() {
        if (pendingRoman.isNotEmpty()) {
            currentInputConnection?.finishComposingText()
            pendingRoman = ""
        }
    }

    private fun backspace() {
        val ic = currentInputConnection ?: return
        if (mode == Mode.AVRO && pendingRoman.isNotEmpty()) {
            pendingRoman = pendingRoman.dropLast(1)
            if (pendingRoman.isEmpty()) {
                ic.setComposingText("", 1)
                ic.finishComposingText()
                preview?.text = ""
            } else updateAvroComposition()
        } else {
            ic.deleteSurroundingTextInCodePoints(1, 0)
        }
        vibrate()
    }

    private fun enter() {
        finishPending()
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) ic.performEditorAction(action)
        else ic.commitText("\n", 1)
        preview?.text = ""
    }

    private fun openSettings() {
        try {
            startActivity(android.content.Intent(this, MainActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) { }
    }

    private fun key(text: String, special: Boolean, action: () -> Unit): TextView {
        val shape = GradientDrawable().apply {
            setColor(if (special) specialBg else keyBg)
            cornerRadius = dp(9).toFloat()
        }
        val mask = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(9).toFloat()
        }
        return TextView(this).apply {
            this.text = text
            textSize = if (text.length > 5) 11f else if (text.length > 2) 14f else 18f
            setTextColor(Color.rgb(245, 247, 251))
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            background = RippleDrawable(ColorStateList.valueOf(Color.argb(65, 255, 255, 255)), shape, mask)
            elevation = dp(2).toFloat()
            isClickable = true
            isFocusable = true
            setPadding(1, 0, 1, 0)
            setOnClickListener {
                animate().scaleX(0.93f).scaleY(0.93f).setDuration(65).withEndAction {
                    animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                }.start()
                vibrate()
                action()
            }
        }
    }

    private fun toolButton(text: String, action: () -> Unit) = TextView(this).apply {
        this.text = text
        textSize = 12f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        setPadding(dp(8), 0, dp(8), 0)
        background = GradientDrawable().apply {
            setColor(Color.rgb(37, 43, 55))
            cornerRadius = dp(10).toFloat()
            setStroke(dp(1), Color.rgb(60, 70, 87))
        }
        isClickable = true
        setOnClickListener { action() }
    }

    private fun keyParams(weight: Float) = LinearLayout.LayoutParams(0, dp(47), weight).apply {
        setMargins(dp(2), dp(2), dp(2), dp(2))
    }
    private fun toolParams() = LinearLayout.LayoutParams(-2, dp(34)).apply { setMargins(dp(3), 0, dp(3), 0) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun vibrate() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(9, VibrationEffect.DEFAULT_AMPLITUDE))
            else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(9)
            }
        } catch (_: Exception) { }
    }
}
