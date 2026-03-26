package com.multilingreborn.keyboard

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*

class EmojiPanel(
    private val context: Context,
    private val onEmoji: (String) -> Unit
) {

    private val categories = linkedMapOf(
        "😀 Smileys"   to SMILEYS,
        "👋 People"    to PEOPLE,
        "🐶 Animals"   to ANIMALS,
        "🍕 Food"      to FOOD,
        "⚽ Activities" to ACTIVITIES,
        "✈️ Travel"    to TRAVEL,
        "💡 Objects"   to OBJECTS,
        "❤️ Symbols"   to SYMBOLS,
        "🏁 Flags"     to FLAGS,
    )

    fun buildView(context: Context): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A2E"))
        }

        // Search bar
        val search = EditText(context).apply {
            hint = "Search emoji..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#16213E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        // Category tabs
        val tabs = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
        }
        val tabRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        tabs.addView(tabRow)

        // Emoji grid container
        val gridScroll = ScrollView(context).apply {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            layoutParams = lp
        }
        val grid = FlowLayout(context)
        gridScroll.addView(grid)

        var currentCategory = categories.keys.first()

        fun showCategory(cat: String) {
            currentCategory = cat
            grid.removeAllViews()
            val emojis = categories[cat] ?: return
            for (emoji in emojis) {
                val btn = TextView(context).apply {
                    text = emoji
                    textSize = 22f
                    gravity = Gravity.CENTER
                    setPadding(dp(4), dp(4), dp(4), dp(4))
                    setOnClickListener { onEmoji(emoji) }
                }
                grid.addView(btn)
            }
        }

        for ((cat, _) in categories) {
            val firstEmoji = cat.split(" ")[0]
            val tab = TextView(context).apply {
                text = firstEmoji
                textSize = 20f
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(6), dp(8), dp(6))
                setOnClickListener { showCategory(cat) }
            }
            tabRow.addView(tab)
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim() ?: return
                if (q.isEmpty()) { showCategory(currentCategory); return }
                grid.removeAllViews()
                for ((_, emojis) in categories) {
                    for (emoji in emojis) {
                        if (emojiName(emoji).contains(q, ignoreCase = true)) {
                            val btn = TextView(context).apply {
                                text = emoji; textSize = 22f
                                gravity = Gravity.CENTER
                                setPadding(dp(4), dp(4), dp(4), dp(4))
                                setOnClickListener { onEmoji(emoji) }
                            }
                            grid.addView(btn)
                        }
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        showCategory(categories.keys.first())

        root.addView(search, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(tabs, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(gridScroll)
        return root
    }

    private fun dp(n: Int) = (n * context.resources.displayMetrics.density).toInt()

    private fun emojiName(e: String): String = EMOJI_NAMES[e] ?: e

    // Simple FlowLayout for emoji grid
    inner class FlowLayout(context: Context) : android.view.ViewGroup(context) {
        override fun onMeasure(ws: Int, hs: Int) {
            val w = MeasureSpec.getSize(ws)
            var x = 0; var y = 0; var rowH = 0
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                if (x + child.measuredWidth > w) { x = 0; y += rowH; rowH = 0 }
                x += child.measuredWidth; rowH = maxOf(rowH, child.measuredHeight)
            }
            setMeasuredDimension(w, y + rowH)
        }
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val w = r - l; var x = 0; var y = 0; var rowH = 0
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (x + child.measuredWidth > w) { x = 0; y += rowH; rowH = 0 }
                child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
                x += child.measuredWidth; rowH = maxOf(rowH, child.measuredHeight)
            }
        }
    }

    companion object {
        val SMILEYS = listOf(
            "😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃","😉","😊","😇","🥰","😍",
            "🤩","😘","😗","☺️","😚","😙","🥲","😋","😛","😜","🤪","😝","🤑","🤗","🤭",
            "🤫","🤔","🤐","🤨","😐","😑","😶","😏","😒","🙄","😬","🤥","😔","😪","🤤",
            "😴","😷","🤒","🤕","🤢","🤮","🤧","🥵","🥶","🥴","😵","🤯","🤠","🥳","🥸",
            "😎","🤓","🧐","😕","😟","🙁","☹️","😮","😯","😲","😳","🥺","😦","😧","😨",
            "😰","😥","😢","😭","😱","😖","😣","😞","😓","😩","😫","🥱","😤","😡","😠",
            "🤬","😈","👿","💀","☠️","💩","🤡","👹","👺","👻","👽","👾","🤖","😺","😸",
            "😹","😻","😼","😽","🙀","😿","😾"
        )
        val PEOPLE = listOf(
            "👋","🤚","🖐","✋","🖖","👌","🤌","🤏","✌️","🤞","🤟","🤘","🤙","👈","👉",
            "👆","🖕","👇","☝️","👍","👎","✊","👊","🤛","🤜","👏","🙌","👐","🤲","🤝",
            "🙏","✍️","💅","🤳","💪","🦾","🦿","🦵","🦶","👂","🦻","👃","🫀","🫁","🧠",
            "🦷","🦴","👀","👁","👅","👄","💋","👶","🧒","👦","👧","🧑","👱","👨","🧔",
            "👩","🧓","👴","👵","🙍","🙎","🙅","🙆","💁","🙋","🧏","🙇","🤦","🤷"
        )
        val ANIMALS = listOf(
            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐻‍❄️","🐨","🐯","🦁","🐮","🐷","🐸",
            "🐵","🙈","🙉","🙊","🐔","🐧","🐦","🐤","🦆","🦅","🦉","🦇","🐺","🐗","🐴",
            "🦄","🐝","🪱","🐛","🦋","🐌","🐞","🐜","🪲","🦟","🦗","🪳","🕷","🦂","🐢",
            "🐍","🦎","🐊","🐸","🦕","🦖","🐉","🐲","🦕","🐳","🐋","🐬","🦭","🐟","🐠",
            "🐡","🦈","🐙","🦑","🦐","🦞","🦀","🐡","🦑"
        )
        val FOOD = listOf(
            "🍎","🍊","🍋","🍇","🍓","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🥑","🫑","🥦",
            "🥬","🥒","🌽","🥕","🧄","🧅","🥔","🍠","🫘","🌰","🍞","🥐","🥖","🫓","🧀",
            "🥚","🍳","🧈","🥞","🧇","🥓","🥩","🍗","🍖","🌭","🍔","🍟","🍕","🫔","🥙",
            "🧆","🌮","🌯","🥗","🥘","🫕","🍝","🍜","🍛","🍣","🍱","🥟","🦪","🍤","🍙",
            "🍚","🍘","🍥","🥮","🍡","🧁","🍰","🎂","🍮","🍭","🍬","🍫","🍿","🍩","🍪",
            "☕","🍵","🧃","🥤","🧋","🍺","🍻","🥂","🍷","🥃","🍸","🍹","🧉","🍾"
        )
        val ACTIVITIES = listOf(
            "⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱","🏓","🏸","🥅","⛳","🎣",
            "🏹","🥊","🥋","⛸","🎿","🛷","🏋️","🤸","⛹️","🤺","🏌️","🏇","🧘","🏄","🏊",
            "🚴","🏆","🥇","🥈","🥉","🏅","🎖","🎗","🎫","🎟","🎪","🤹","🎭","🎨","🎬",
            "🎤","🎧","🎼","🎹","🪘","🥁","🪗","🎷","🎺","🎸","🪕","🎻","🎲","♟","🎯",
            "🎳","🎮","🎰","🧩"
        )
        val TRAVEL = listOf(
            "🚗","🚕","🚙","🚌","🚎","🏎","🚓","🚑","🚒","🚐","🛻","🚚","🚛","🚜","🛵",
            "🏍","🛺","🚲","🛴","🛹","🛼","🚏","🛣","🛤","⛽","🚧","⚓","🛟","⛵","🚤",
            "🛥","🛳","⛴","🚢","✈️","🛩","🛫","🛬","🛰","🚀","🛸","🚁","🛺","⛺","🏕",
            "🏗","🏘","🏚","🏠","🏡","🏢","🏣","🏤","🏥","🏦","🏨","🏩","🏪","🏫","🏬",
            "🗼","🗽","🗾","🎌","🗺","🧭","🌋","🏔","⛰","🏕","🌁","🌃","🌄","🌅","🌆"
        )
        val OBJECTS = listOf(
            "⌚","📱","💻","⌨️","🖥","🖨","🖱","🖲","💽","💾","💿","📀","📷","📸","📹",
            "🎥","📽","📞","☎️","📟","📠","📺","📻","🧭","⏱","⏲","⏰","🕰","⌛","⏳",
            "📡","🔋","🔌","💡","🔦","🕯","💰","💳","💸","💵","💴","💶","💷","🪙","💹",
            "📈","📉","📊","📋","📌","📍","📎","🖇","📏","📐","✂️","🗃","🗄","🗑","🔒",
            "🔓","🔏","🔐","🔑","🗝","🔨","🪓","⛏","⚒","🛠","🗡","⚔️","🛡","🔧","🔩","⚙️"
        )
        val SYMBOLS = listOf(
            "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣️","💕","💞","💓","💗",
            "💖","💘","💝","💟","☮️","✝️","☪️","🕉","☸️","✡️","🔯","🕎","☯️","☦️","🛐",
            "⛎","♈","♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓","🆔","⚛️","🉑",
            "☢️","☣️","📴","📳","🈶","🈚","🈸","🈺","🈷️","✴️","🆚","💮","🉐","㊙️","㊗️",
            "🈴","🈵","🈹","🈲","🅰️","🅱️","🆎","🆑","🅾️","🆘","❌","⭕","🛑","⛔","📛",
            "🚫","💯","💢","♨️","🚷","🚯","🚳","🚱","🔞","📵","🔕","🔇","🔈","🔉","🔊"
        )
        val FLAGS = listOf(
            "🏁","🚩","🎌","🏴","🏳","🏳️‍🌈","🏳️‍⚧️","🏴‍☠️","🇦🇫","🇦🇱","🇩🇿","🇦🇸","🇦🇩",
            "🇦🇴","🇦🇮","🇦🇶","🇦🇬","🇦🇷","🇦🇲","🇦🇼","🇦🇺","🇦🇹","🇦🇿","🇧🇸","🇧🇭",
            "🇧🇩","🇧🇧","🇧🇾","🇧🇪","🇧🇿","🇧🇯","🇧🇲","🇧🇹","🇧🇴","🇧🇦","🇧🇼","🇧🇷",
            "🇮🇳","🇨🇳","🇯🇵","🇰🇷","🇺🇸","🇬🇧","🇩🇪","🇫🇷","🇮🇹","🇪🇸","🇷🇺","🇧🇷",
            "🇨🇦","🇦🇺","🇿🇦","🇲🇽","🇸🇦","🇵🇰","🇳🇬","🇧🇩","🇮🇩","🇵🇭","🇹🇷","🇮🇷"
        )

        val EMOJI_NAMES = mapOf(
            "😀" to "grinning face", "😂" to "face tears joy", "❤️" to "red heart love",
            "👍" to "thumbs up", "👎" to "thumbs down", "🔥" to "fire hot",
            "⭐" to "star", "✨" to "sparkles", "🎉" to "party tada celebration",
            "😍" to "heart eyes love", "🤣" to "rolling floor laughing",
            "😊" to "smiling face", "🙏" to "folded hands please thanks pray",
            "💕" to "two hearts love", "😭" to "loudly crying face sad",
            "😘" to "face blowing kiss love", "👏" to "clapping hands applause",
            "🎂" to "birthday cake", "🍕" to "pizza", "🍔" to "hamburger burger",
            "🍺" to "beer mug drink", "☕" to "coffee hot drink",
            "🐶" to "dog puppy", "🐱" to "cat kitten", "🦁" to "lion",
            "🌍" to "earth globe world", "🌸" to "cherry blossom flower",
            "🌙" to "crescent moon night", "⚽" to "soccer football",
            "🏆" to "trophy winner", "💡" to "light bulb idea"
        )
    }
}
