package com.wajidboard.keyboard

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Avro phonetic rule engine adapted from OpenBangla Keyboard's AvroPhonetic.cpp.
 * Original parser: Copyright (C) 2015-2016 Muhammad Mominul Huque.
 * The original parser is dual-licensed MPL-2.0 / GPL-3.0; this adaptation is
 * distributed under GPL-3.0. See THIRD_PARTY_NOTICES.md.
 */
class AvroPhoneticEngine(context: Context) {
    private data class Pattern(val find: String, val replace: String, val rules: JSONArray)

    private val patterns = LinkedHashMap<String, Pattern>()
    private val maxPatternLength: Int
    private val vowels: String
    private val consonants: String
    private val numbers: String
    private val caseSensitive: String

    init {
        val json = context.assets.open("avrophonetic.json").bufferedReader().use { it.readText() }
        val layout = JSONObject(json).getJSONObject("layout")
        vowels = layout.optString("vowel")
        consonants = layout.optString("consonant")
        numbers = layout.optString("number")
        caseSensitive = layout.optString("casesensitive")
        val array = layout.getJSONArray("patterns")
        var longest = 1
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val find = item.getString("find")
            patterns[find] = Pattern(find, item.optString("replace", find), item.optJSONArray("rules") ?: JSONArray())
            if (find.length > longest) longest = find.length
        }
        maxPatternLength = longest
    }

    fun convert(input: String): String {
        val fixed = buildString(input.length) {
            input.forEach { c -> append(if (caseSensitive.contains(c, true)) c else c.lowercaseChar()) }
        }
        val out = StringBuilder()
        var cursor = 0
        while (cursor < fixed.length) {
            var matched = false
            val longest = minOf(maxPatternLength, fixed.length - cursor)
            for (size in longest downTo 1) {
                val pattern = patterns[fixed.substring(cursor, cursor + size)] ?: continue
                var replacement: String? = null
                for (r in 0 until pattern.rules.length()) {
                    val rule = pattern.rules.optJSONObject(r) ?: continue
                    if (ruleMatches(rule.optJSONArray("matches") ?: JSONArray(), fixed, cursor, cursor + size)) {
                        replacement = rule.optString("replace", pattern.replace)
                        break
                    }
                }
                out.append(replacement ?: pattern.replace)
                cursor += size
                matched = true
                break
            }
            if (!matched) {
                out.append(fixed[cursor])
                cursor++
            }
        }
        return out.toString()
    }

    private fun ruleMatches(matches: JSONArray, text: String, start: Int, end: Int): Boolean {
        for (i in 0 until matches.length()) {
            val m = matches.optJSONObject(i) ?: continue
            val type = m.optString("type")
            var scope = m.optString("scope")
            val negative = scope.startsWith("!")
            if (negative) scope = scope.drop(1)
            val value = m.optString("value")
            val position = if (type == "suffix") end else start - 1
            val actual = when (scope) {
                "punctuation" -> if (position !in text.indices) true else !isVowel(text[position]) && !isConsonant(text[position])
                "vowel" -> position in text.indices && isVowel(text[position])
                "consonant" -> position in text.indices && isConsonant(text[position])
                "number" -> position in text.indices && numbers.contains(text[position], true)
                "exact" -> {
                    if (type == "suffix") {
                        val finish = end + value.length
                        finish <= text.length && text.substring(end, finish) == value
                    } else {
                        val begin = start - value.length
                        begin >= 0 && text.substring(begin, start) == value
                    }
                }
                else -> false
            }
            if (if (negative) actual else !actual) return false
        }
        return true
    }

    private fun isVowel(c: Char) = vowels.contains(c, true)
    private fun isConsonant(c: Char) = consonants.contains(c, true)
}
