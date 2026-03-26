package com.multilingreborn.keyboard

import android.content.Context
import android.content.SharedPreferences

class LayoutManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("layout_prefs", Context.MODE_PRIVATE)
    private val langs = buildLanguageList()
    private var langIndex = prefs.getInt("lang_index", 0)

    fun nextLanguage() {
        langIndex = (langIndex + 1) % langs.size
        prefs.edit().putInt("lang_index", langIndex).apply()
    }

    fun currentLanguageTag(): String = langs[langIndex].tag

    fun getRowCount(mode: KeyboardService.KeyboardMode): Int =
        getRowDefs(mode, false).size

    fun getKeys(mode: KeyboardService.KeyboardMode, shifted: Boolean): List<Key> =
        getRowDefs(mode, shifted).flatten()

    fun getRowDefs(mode: KeyboardService.KeyboardMode, shifted: Boolean): List<List<Key>> =
        when (mode) {
            KeyboardService.KeyboardMode.ALPHA    -> alphaLayout(shifted)
            KeyboardService.KeyboardMode.SYMBOLS  -> symbolsLayout()
            KeyboardService.KeyboardMode.SYMBOLS2 -> symbols2Layout()
            KeyboardService.KeyboardMode.NUMERIC  -> numericLayout()
            KeyboardService.KeyboardMode.EMOJI    -> emptyList()
        }

    // ── Alpha layout (language-aware) ─────────────────────────────────────────

    private fun alphaLayout(shifted: Boolean): List<List<Key>> {
        val lang = langs[langIndex]
        val rows = lang.rows
        val result = mutableListOf<List<Key>>()

        for ((i, row) in rows.withIndex()) {
            result.add(row.map { ch ->
                Key(ch.code, ch.lower, ch.upper.takeIf { it != ch.lower } ?: "", isSpecial = false)
            })
        }

        // Bottom row: special keys
        result.add(listOf(
            Key(KeyCode.SWITCH_SYMBOLS, "123",  isSpecial = true, weight = 1.5f),
            Key(KeyCode.SWITCH_LANG,    "🌐",   isSpecial = true),
            Key(KeyCode.SPACE,          " ",    isSpecial = false, weight = 4f),
            Key(KeyCode.SWITCH_EMOJI,   "☺",    isSpecial = true),
            Key(KeyCode.ENTER,          "⏎",    isSpecial = true, weight = 1.5f),
        ))

        // Insert shift + delete in last alpha row
        val lastAlpha = result[result.size - 2].toMutableList()
        lastAlpha.add(0, Key(KeyCode.SHIFT, "⇧", isSpecial = true, weight = 1.5f))
        lastAlpha.add(Key(KeyCode.DELETE, "⌫", isSpecial = true, weight = 1.5f))
        result[result.size - 2] = lastAlpha

        return result
    }

    // ── Symbols layout ────────────────────────────────────────────────────────

    private fun symbolsLayout(): List<List<Key>> = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0").mapIndexed { i, s ->
            Key(s[0].code, s, subLabel = SYMBOL_SUB[i])
        },
        listOf("@","#","€","_","&","-","+","(",")","/").map { Key(it[0].code, it) },
        listOf(
            Key(KeyCode.SWITCH_SYMBOLS2, "#+=", isSpecial = true),
            *listOf("*","\"","'",":",";","!","?","\\").map { Key(it[0].code, it) }.toTypedArray(),
            Key(KeyCode.DELETE, "⌫", isSpecial = true)
        ),
        listOf(
            Key(KeyCode.SWITCH_ALPHA, "ABC", isSpecial = true, weight = 1.5f),
            Key(KeyCode.SWITCH_LANG,  "🌐",  isSpecial = true),
            Key(KeyCode.SPACE, " ", weight = 4f),
            Key(KeyCode.SWITCH_EMOJI, "☺", isSpecial = true),
            Key(KeyCode.ENTER, "⏎", isSpecial = true, weight = 1.5f),
        )
    )

    private fun symbols2Layout(): List<List<Key>> = listOf(
        listOf("~","`","|","•","√","π","÷","×","¶","∆").map { Key(it[0].code, it) },
        listOf("£","¢","€","¥","^","°","=","{","}","\\").map { Key(it[0].code, it) },
        listOf(
            Key(KeyCode.SWITCH_SYMBOLS, "123", isSpecial = true),
            *listOf("%","©","®","™","✓","[","]").map { Key(it[0].code, it) }.toTypedArray(),
            Key(KeyCode.DELETE, "⌫", isSpecial = true)
        ),
        listOf(
            Key(KeyCode.SWITCH_ALPHA, "ABC", isSpecial = true, weight = 1.5f),
            Key(KeyCode.SWITCH_LANG,  "🌐",  isSpecial = true),
            Key(KeyCode.SPACE, " ", weight = 4f),
            Key(KeyCode.SWITCH_EMOJI, "☺", isSpecial = true),
            Key(KeyCode.ENTER, "⏎", isSpecial = true, weight = 1.5f),
        )
    )

    private fun numericLayout(): List<List<Key>> = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0").map { Key(it[0].code, it) },
        listOf(".","@","#","_","-","(",")","+","/","*").map { Key(it[0].code, it) },
        listOf(
            Key(KeyCode.SWITCH_SYMBOLS, "#+=", isSpecial = true),
            *listOf(",","!","?",";",":","$","€").map { Key(it[0].code, it) }.toTypedArray(),
            Key(KeyCode.DELETE, "⌫", isSpecial = true)
        ),
        listOf(
            Key(KeyCode.SWITCH_ALPHA, "ABC", isSpecial = true, weight = 2f),
            Key(KeyCode.SPACE, " ", weight = 4f),
            Key(KeyCode.ENTER, "⏎", isSpecial = true, weight = 2f),
        )
    )

    companion object {
        private val SYMBOL_SUB = arrayOf("!","@","#","\$","%","^","&","*","(",")")
    }

    // ── Language definitions ──────────────────────────────────────────────────

    data class CharDef(val lower: String, val upper: String = "") {
        val code get() = lower.firstOrNull()?.code ?: 0
    }
    data class LangDef(val tag: String, val name: String, val rows: List<List<CharDef>>)

    private fun row(vararg chars: String): List<CharDef> = chars.map { ch ->
        if (ch.length == 2) CharDef(ch[0].toString(), ch[1].toString())
        else CharDef(ch)
    }

    private fun buildLanguageList(): List<LangDef> = listOf(

        LangDef("en", "English", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("en_dvorak", "English (Dvorak)", listOf(
            row("'",",.","p","y","f","g","c","r","l","?/"),
            row("a","o","e","u","i","d","h","t","n","s"),
            row(";","q","j","k","x","b","m","w","v","z")
        )),
        LangDef("en_colemak", "English (Colemak)", listOf(
            row("q","w","f","p","g","j","l","u","y",";"),
            row("a","r","s","t","d","h","n","e","i","o"),
            row("z","x","c","v","b","k","m")
        )),
        LangDef("fr", "Français (AZERTY)", listOf(
            row("a","z","e","r","t","y","u","i","o","p"),
            row("q","s","d","f","g","h","j","k","l","m"),
            row("w","x","c","v","b","n")
        )),
        LangDef("de", "Deutsch", listOf(
            row("q","w","e","r","t","z","u","i","o","p","ü"),
            row("a","s","d","f","g","h","j","k","l","ö","ä"),
            row("y","x","c","v","b","n","m")
        )),
        LangDef("es", "Español", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l","ñ"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("pt", "Português", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l","ç"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("it", "Italiano", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("ru", "Русский", listOf(
            row("й","ц","у","к","е","н","г","ш","щ","з","х"),
            row("ф","ы","в","а","п","р","о","л","д","ж","э"),
            row("я","ч","с","м","и","т","ь","б","ю")
        )),
        LangDef("ar", "العربية", listOf(
            row("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج","د"),
            row("ش","س","ي","ب","ل","ا","ت","ن","م","ك","ط"),
            row("ئ","ء","ؤ","ر","لا","ى","ة","و","ز","ظ")
        )),
        LangDef("zh", "中文 (Pinyin)", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("ja", "日本語 (Romaji)", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("ko", "한국어", listOf(
            row("ㅂ","ㅈ","ㄷ","ㄱ","ㅅ","ㅛ","ㅕ","ㅑ","ㅐ","ㅔ"),
            row("ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ"),
            row("ㅋ","ㅌ","ㅊ","ㅍ","ㅠ","ㅜ","ㅡ")
        )),
        LangDef("hi", "हिन्दी", listOf(
            row("ौ","ै","ा","ी","ू","ब","ह","ग","द","ज"),
            row("ो","े","्","ि","ु","प","र","क","त","च"),
            row("ं","म","न","व","ल","स","य")
        )),
        LangDef("bn", "বাংলা", listOf(
            row("ো","ৌ","ে","ৈ","ু","ূ","ি","ী","",""),
            row("া","ব","ক","ম","ন","জ","দ","গ","ত","র"),
            row("স","প","ল","হ","য","শ","ড")
        )),
        LangDef("tr", "Türkçe", listOf(
            row("q","w","e","r","t","y","u","ı","o","p","ğ"),
            row("a","s","d","f","g","h","j","k","l","ş","i"),
            row("z","x","c","v","b","n","m","ö","ü","ç")
        )),
        LangDef("pl", "Polski", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("nl", "Nederlands", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("el", "Ελληνικά", listOf(
            row("ς","ε","ρ","τ","υ","θ","ι","ο","π"),
            row("α","σ","δ","φ","γ","η","ξ","κ","λ"),
            row("ζ","χ","ψ","ω","β","ν","μ")
        )),
        LangDef("he", "עברית", listOf(
            row("ק","ר","א","ט","ו","ן","מ","פ"),
            row("ש","ד","ג","כ","ע","י","ח","ל","ך","ף"),
            row("ז","ס","ב","ה","נ","ת","ץ")
        )),
        LangDef("th", "ไทย", listOf(
            row("ๆ","ไ","ำ","พ","ะ","ั","ี","ร","น","ย","บ","ล"),
            row("ฟ","ห","ก","ด","เ","้","่","า","ส","ว","ง"),
            row("ผ","ป","แ","อ","ิ","ื","ท","ม","ใ","ฝ")
        )),
        LangDef("vi", "Tiếng Việt", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("uk", "Українська", listOf(
            row("й","ц","у","к","е","н","г","ш","щ","з","х","ї"),
            row("ф","і","в","а","п","р","о","л","д","ж","є"),
            row("я","ч","с","м","и","т","ь","б","ю")
        )),
        LangDef("cs", "Čeština", listOf(
            row("q","w","e","r","t","z","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("y","x","c","v","b","n","m")
        )),
        LangDef("ro", "Română", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("sv", "Svenska", listOf(
            row("q","w","e","r","t","y","u","i","o","p","å"),
            row("a","s","d","f","g","h","j","k","l","ö","ä"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("fi", "Suomi", listOf(
            row("q","w","e","r","t","y","u","i","o","p","å"),
            row("a","s","d","f","g","h","j","k","l","ö","ä"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("da", "Dansk", listOf(
            row("q","w","e","r","t","y","u","i","o","p","å"),
            row("a","s","d","f","g","h","j","k","l","æ","ø"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("nb", "Norsk", listOf(
            row("q","w","e","r","t","y","u","i","o","p","å"),
            row("a","s","d","f","g","h","j","k","l","æ","ø"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("hu", "Magyar", listOf(
            row("q","w","e","r","t","z","u","i","o","p","ő"),
            row("a","s","d","f","g","h","j","k","l","é","á"),
            row("y","x","c","v","b","n","m","í","ü","ó","ö","ú")
        )),
        LangDef("id", "Bahasa Indonesia", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("ms", "Bahasa Melayu", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("fa", "فارسی", listOf(
            row("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج","چ"),
            row("ش","س","ی","ب","ل","ا","ت","ن","م","ک","گ"),
            row("ظ","ط","ز","ر","ذ","د","پ","و")
        )),
        LangDef("ur", "اردو", listOf(
            row("ق","و","ع","ر","ت","ے","ئ","ح","پ"),
            row("ا","س","د","ف","گ","ہ","ج","ک","ل"),
            row("ظ","ط","ز","ش","ب","ن","م")
        )),
        LangDef("ta", "தமிழ்", listOf(
            row("ஔ","ஓ","ஒ","ஏ","ஈ","எ","ஆ","அ"),
            row("க","ங","ச","ஞ","ட","ண","த","ந","ப","ம"),
            row("ய","ர","ல","வ","ழ","ள","ற","ன")
        )),
        LangDef("te", "తెలుగు", listOf(
            row("ఔ","ఓ","ఒ","ఏ","ఈ","ఎ","ఆ","అ"),
            row("క","గ","చ","జ","ట","డ","త","ద","న","బ","మ"),
            row("య","ర","ల","వ","శ","ష","స","హ","ళ","క్ష")
        )),
        LangDef("ml", "മലയാളം", listOf(
            row("ഔ","ഓ","ഒ","ഏ","ഈ","എ","ആ","അ"),
            row("ക","ഗ","ച","ജ","ട","ഡ","ത","ദ","ന","പ","ബ"),
            row("യ","ര","ല","വ","ശ","ഷ","സ","ഹ","ള","ഴ","റ")
        )),
        LangDef("kn", "ಕನ್ನಡ", listOf(
            row("ಔ","ಓ","ಒ","ಏ","ಈ","ಎ","ಆ","ಅ"),
            row("ಕ","ಗ","ಚ","ಜ","ಟ","ಡ","ತ","ದ","ನ","ಪ","ಬ"),
            row("ಯ","ರ","ಲ","ವ","ಶ","ಷ","ಸ","ಹ","ಳ","ಕ್ಷ")
        )),
        LangDef("si", "සිංහල", listOf(
            row("ැ","ෑ","ි","ී","ු","ූ","ෙ","ේ","ෛ","ො"),
            row("ක","ව","ල","ස","ත","ර","ජ","ද","ම","න"),
            row("ශ","ෂ","ය","ෞ","ූ","බ","ග","ච","හ","ප")
        )),
        LangDef("my", "မြန်မာ", listOf(
            row("ဆ","ခ","ဂ","ဃ","င","ဆ","ဇ","ဈ","ည"),
            row("တ","ထ","ဒ","ဓ","နပ","ဖ","ဗ","ဘ","မ"),
            row("ယ","ရ","လ","ဝ","သ","ဟ","ဠ","အ")
        )),
        LangDef("km", "ខ្មែរ", listOf(
            row("ឆ","ឋ","ឌ","ឍ","ណ","ត","ថ","ទ","ធ","ន"),
            row("ក","ខ","គ","ឃ","ង","ច","ជ","ឈ","ញ","ប"),
            row("ផ","ព","ភ","ម","យ","រ","ល","វ","ស","ហ")
        )),
        LangDef("lo", "ລາວ", listOf(
            row("ຂ","ຄ","ງ","ສ","ຊ","ຍ","ບ","ປ","ຜ","ຝ"),
            row("ຟ","ຫ","ກ","ດ","ເ","ຣ","ນ","ທ","ລ","ພ"),
            row("ອ","ວ","ຈ","ມ","ແ","ໃ","ໄ","ໂ","ຈ")
        )),
        LangDef("ne", "नेपाली", listOf(
            row("ट","ठ","ड","ढ","ण","त","थ","द","ध","न"),
            row("क","ख","ग","घ","ङ","च","छ","ज","झ","ञ"),
            row("प","फ","ब","भ","म","य","र","ल","व","स")
        )),
        LangDef("si_phonetic", "Sinhala (Phonetic)", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","j","k","l"),
            row("z","x","c","v","b","n","m")
        )),
        LangDef("eo", "Esperanto", listOf(
            row("q","w","e","r","t","y","u","i","o","p"),
            row("a","s","d","f","g","h","ĥ","j","ĵ","k","l"),
            row("z","ĝ","c","ĉ","v","b","n","m","ŝ","ŭ")
        ))
    )
}
