package com.multilingreborn.keyboard

import android.content.Context
import android.graphics.Color

class ThemePrefs(private val context: Context) {

    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    companion object {
        val THEMES = mapOf(
            "dark_blue" to KeyboardTheme(
                background = Color.parseColor("#1A1A2E"),
                keyNormal  = Color.parseColor("#16213E"),
                keySpecial = Color.parseColor("#0F3460"),
                keyPressed = Color.parseColor("#E94560"),
                textNormal = Color.WHITE,
                textSpecial = Color.parseColor("#A0C4FF")
            ),
            "amoled" to KeyboardTheme(
                background = Color.BLACK,
                keyNormal  = Color.parseColor("#111111"),
                keySpecial = Color.parseColor("#1C1C1C"),
                keyPressed = Color.parseColor("#BB86FC"),
                textNormal = Color.WHITE,
                textSpecial = Color.parseColor("#BB86FC")
            ),
            "light" to KeyboardTheme(
                background = Color.parseColor("#F0F0F0"),
                keyNormal  = Color.WHITE,
                keySpecial = Color.parseColor("#D0D0D0"),
                keyPressed = Color.parseColor("#4285F4"),
                textNormal = Color.parseColor("#202020"),
                textSpecial = Color.parseColor("#4285F4"),
                subText    = Color.parseColor("#888888")
            ),
            "material_you" to KeyboardTheme(
                background = Color.parseColor("#1C1B1F"),
                keyNormal  = Color.parseColor("#2B2930"),
                keySpecial = Color.parseColor("#49454F"),
                keyPressed = Color.parseColor("#D0BCFF"),
                textNormal = Color.parseColor("#E6E1E5"),
                textSpecial = Color.parseColor("#D0BCFF")
            ),
            "forest" to KeyboardTheme(
                background = Color.parseColor("#1B2A1B"),
                keyNormal  = Color.parseColor("#243824"),
                keySpecial = Color.parseColor("#2E5A2E"),
                keyPressed = Color.parseColor("#7CFC00"),
                textNormal = Color.parseColor("#E0F0E0"),
                textSpecial = Color.parseColor("#7CFC00")
            ),
            "ocean" to KeyboardTheme(
                background = Color.parseColor("#0A1628"),
                keyNormal  = Color.parseColor("#0D2137"),
                keySpecial = Color.parseColor("#0A3054"),
                keyPressed = Color.parseColor("#00CED1"),
                textNormal = Color.WHITE,
                textSpecial = Color.parseColor("#00BFFF")
            )
        )
    }

    fun load(): KeyboardTheme {
        val name = prefs.getString("theme", "dark_blue") ?: "dark_blue"
        return THEMES[name] ?: THEMES["dark_blue"]!!
    }

    fun save(name: String) = prefs.edit().putString("theme", name).apply()

    fun getTextSize(): Float = prefs.getFloat("text_size", 15f)
    fun setTextSize(size: Float) = prefs.edit().putFloat("text_size", size).apply()

    fun getKeyRadius(): Float = prefs.getFloat("key_radius", 8f)
    fun setKeyRadius(r: Float) = prefs.edit().putFloat("key_radius", r).apply()
}
