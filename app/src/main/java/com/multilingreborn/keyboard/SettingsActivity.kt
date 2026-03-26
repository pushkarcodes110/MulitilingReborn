package com.multilingreborn.keyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(0xFF1A1A2E.toInt())
        }
        root.addView(layout)

        // Title
        layout.addView(TextView(this).apply {
            text = "Multiling O Reborn"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, dp(16))
        })

        // ── Setup section ───────────────────────────────────────────────────
        layout.addView(sectionHeader("Setup"))

        layout.addView(actionButton("Enable Keyboard") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })

        layout.addView(actionButton("Switch to This Keyboard") {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        })

        // ── Theme section ────────────────────────────────────────────────────
        layout.addView(sectionHeader("Theme"))

        val themePrefs = ThemePrefs(this)
        val themeNames = ThemePrefs.THEMES.keys.toList()
        val themeLabels = listOf("Dark Blue","AMOLED Black","Light","Material You","Forest","Ocean")

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity,
                android.R.layout.simple_spinner_dropdown_item, themeLabels)
            val current = themePrefs.load()
            val idx = themeNames.indexOfFirst { ThemePrefs.THEMES[it] == current }.coerceAtLeast(0)
            setSelection(idx)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                    themePrefs.save(themeNames[pos])
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }
        layout.addView(labelWrap("Color Theme", spinner))

        // Text size
        layout.addView(sectionHeader("Keys"))

        layout.addView(sliderRow("Text Size", 10f, 24f, themePrefs.getTextSize()) {
            themePrefs.setTextSize(it)
        })

        layout.addView(sliderRow("Key Roundness", 0f, 20f, themePrefs.getKeyRadius()) {
            themePrefs.setKeyRadius(it)
        })

        // ── Language section ─────────────────────────────────────────────────
        layout.addView(sectionHeader("Languages"))
        layout.addView(TextView(this).apply {
            text = "Use the 🌐 globe key on the keyboard to cycle through your enabled languages.\n\n" +
                   "Supported: English (QWERTY/Dvorak/Colemak), French, German, Spanish, Portuguese, " +
                   "Italian, Russian, Arabic, Chinese (Pinyin), Japanese (Romaji), Korean, Hindi, " +
                   "Bengali, Turkish, Polish, Dutch, Greek, Hebrew, Thai, Vietnamese, Ukrainian, " +
                   "Czech, Romanian, Swedish, Finnish, Danish, Norwegian, Hungarian, Indonesian, " +
                   "Malay, Persian, Urdu, Tamil, Telugu, Malayalam, Kannada, Sinhala, Burmese, " +
                   "Khmer, Lao, Nepali, Esperanto and more."
            textSize = 13f
            setTextColor(0xFFCCCCCC.toInt())
        })

        // ── About section ─────────────────────────────────────────────────────
        layout.addView(sectionHeader("About"))
        layout.addView(TextView(this).apply {
            text = "Multiling O Reborn v1.0\n" +
                   "Open source. Android 7–16 compatible.\n" +
                   "Canvas-based rendering · Sub-1MB · No permissions required."
            textSize = 13f
            setTextColor(0xFF888888.toInt())
        })

        setContentView(root)
        title = "Multiling O Reborn Settings"
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun sectionHeader(text: String) = TextView(this).apply {
        this.text = text.uppercase()
        textSize = 11f
        setTextColor(0xFF7B7BFF.toInt())
        setPadding(0, dp(20), 0, dp(8))
        letterSpacing = 0.1f
    }

    private fun actionButton(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        setBackgroundColor(0xFF0F3460.toInt())
        setTextColor(0xFFFFFFFF.toInt())
        setPadding(dp(16), dp(12), dp(16), dp(12))
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, dp(8))
        layoutParams = lp
        setOnClickListener { onClick() }
    }

    private fun labelWrap(label: String, child: android.view.View): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 0, dp(8))
            layoutParams = lp
            addView(TextView(context).apply {
                text = label; textSize = 14f; setTextColor(0xFFCCCCCC.toInt())
            })
            addView(child)
        }
    }

    private fun sliderRow(label: String, min: Float, max: Float, value: Float, onChanged: (Float) -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 0, dp(8))
            layoutParams = lp
            val tv = TextView(context).apply {
                text = "$label: ${value.toInt()}"
                textSize = 14f; setTextColor(0xFFCCCCCC.toInt())
            }
            addView(tv)
            addView(SeekBar(context).apply {
                this.max = ((max - min) * 10).toInt()
                progress = ((value - min) * 10).toInt()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                        val v = min + p / 10f
                        tv.text = "$label: ${v.toInt()}"
                        onChanged(v)
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            })
        }
    }
}
