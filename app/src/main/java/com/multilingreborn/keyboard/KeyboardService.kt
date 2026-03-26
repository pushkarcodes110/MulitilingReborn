package com.multilingreborn.keyboard

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

class KeyboardService : InputMethodService(), KeyboardView.KeyboardListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var layoutManager: LayoutManager
    private lateinit var emojiPanel: EmojiPanel
    private var capsLock = false
    private var shifted = false
    private var currentMode = KeyboardMode.ALPHA

    enum class KeyboardMode { ALPHA, SYMBOLS, SYMBOLS2, EMOJI, NUMERIC }

    override fun onCreateInputView(): View {
        layoutManager = LayoutManager(this)
        keyboardView = KeyboardView(this, layoutManager)
        keyboardView.listener = this
        emojiPanel = EmojiPanel(this) { emoji ->
            currentInputConnection?.commitText(emoji, 1)
        }
        applyTheme()
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentMode = when (info.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE -> KeyboardMode.NUMERIC
            else -> KeyboardMode.ALPHA
        }
        shifted = false
        capsLock = false
        keyboardView.setMode(currentMode, shifted, capsLock)
        // Android 16: window insets handled by the framework via fitSystemWindows
    }

    // ── Key events ────────────────────────────────────────────────────────────

    override fun onKey(code: Int, label: String) {
        val ic = currentInputConnection ?: return

        when (code) {
            KeyCode.DELETE -> handleDelete(ic)
            KeyCode.ENTER  -> handleEnter(ic)
            KeyCode.SHIFT  -> handleShift()
            KeyCode.CAPS   -> handleCaps()
            KeyCode.SWITCH_SYMBOLS -> toggleSymbols()
            KeyCode.SWITCH_ALPHA   -> switchTo(KeyboardMode.ALPHA)
            KeyCode.SWITCH_EMOJI   -> switchToEmoji()
            KeyCode.SWITCH_LANG    -> layoutManager.nextLanguage().also { refreshLayout() }
            KeyCode.SPACE  -> {
                ic.commitText(" ", 1)
                autoResetShift()
            }
            else -> {
                val ch = if ((shifted || capsLock) && label.length == 1)
                    label.uppercase() else label
                ic.commitText(ch, 1)
                autoResetShift()
            }
        }
    }

    override fun onSwipeLeft() {
        handleDelete(currentInputConnection)
    }

    override fun onSwipeRight() {
        currentInputConnection?.commitText(" ", 1)
    }

    override fun onLongPress(code: Int, label: String) {
        if (code == KeyCode.DELETE) {
            // Delete word
            val ic = currentInputConnection ?: return
            val before = ic.getTextBeforeCursor(50, 0)?.toString() ?: return
            val trimmed = before.trimEnd()
            val lastSpace = trimmed.lastIndexOf(' ').coerceAtLeast(0)
            val toDelete = before.length - lastSpace
            ic.deleteSurroundingText(toDelete, 0)
        }
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private fun handleDelete(ic: InputConnection?) {
        ic ?: return
        val sel = ic.getSelectedText(0)
        if (!sel.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleEnter(ic: InputConnection) {
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
            ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    private fun handleShift() {
        if (capsLock) { capsLock = false; shifted = false }
        else shifted = !shifted
        keyboardView.setMode(currentMode, shifted, capsLock)
    }

    private fun handleCaps() {
        capsLock = !capsLock
        shifted = capsLock
        keyboardView.setMode(currentMode, shifted, capsLock)
    }

    private fun autoResetShift() {
        if (shifted && !capsLock) {
            shifted = false
            keyboardView.setMode(currentMode, shifted, capsLock)
        }
    }

    private fun toggleSymbols() {
        currentMode = if (currentMode == KeyboardMode.SYMBOLS) KeyboardMode.SYMBOLS2
                      else KeyboardMode.SYMBOLS
        keyboardView.setMode(currentMode, shifted, capsLock)
    }

    private fun switchTo(mode: KeyboardMode) {
        currentMode = mode
        if (mode == KeyboardMode.ALPHA) { shifted = false; capsLock = false }
        keyboardView.setMode(currentMode, shifted, capsLock)
    }

    private fun switchToEmoji() {
        currentMode = KeyboardMode.EMOJI
        keyboardView.showEmojiPanel(emojiPanel)
    }

    private fun refreshLayout() {
        if (currentMode == KeyboardMode.ALPHA) {
            keyboardView.setMode(currentMode, shifted, capsLock)
        }
    }

    private fun applyTheme() {
        val prefs = ThemePrefs(this)
        keyboardView.applyTheme(prefs.load())
    }
}
