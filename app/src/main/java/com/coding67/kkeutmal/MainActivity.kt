package com.coding67.kkeutmal

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*

class MainActivity : Activity() {
    private val used = mutableSetOf<String>()
    private val words = mutableSetOf<String>()
    private lateinit var message: TextView
    private lateinit var computerWord: TextView
    private lateinit var input: EditText
    private lateinit var scoreText: TextView
    private var lastWord: String? = null
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadDictionary()
        buildUi()
        resetGame()
    }

    private fun loadDictionary() {
        runCatching {
            assets.open("txt.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotEmpty() && it.all { ch -> ch in '\uAC00'..'\uD7A3' } }
                    .forEach { words += it }
            }
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 40, 32, 32)
            setBackgroundColor(Color.rgb(247, 247, 251))
        }

        val title = TextView(this).apply {
            text = "끝말잇기"
            textSize = 30f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(31, 41, 55))
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        scoreText = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.rgb(79, 70, 229))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
        }
        root.addView(scoreText, LinearLayout.LayoutParams(-1, -2))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }
        val cardParams = LinearLayout.LayoutParams(-1, 0, 1f)
        cardParams.setMargins(0, 8, 0, 16)
        root.addView(card, cardParams)

        computerWord = TextView(this).apply {
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(17, 24, 39))
            setPadding(0, 20, 0, 16)
        }
        card.addView(computerWord, LinearLayout.LayoutParams(-1, -2))

        message = TextView(this).apply {
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(75, 85, 99))
            setPadding(0, 8, 0, 24)
        }
        card.addView(message, LinearLayout.LayoutParams(-1, -2))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        card.addView(row, LinearLayout.LayoutParams(-1, -2))

        input = EditText(this).apply {
            hint = "단어 입력"
            singleLine = true
            textSize = 18f
            setPadding(18, 12, 18, 12)
        }
        row.addView(input, LinearLayout.LayoutParams(0, -2, 1f))

        val submit = Button(this).apply {
            text = "입력"
            setOnClickListener { submitWord() }
        }
        val buttonParams = LinearLayout.LayoutParams(-2, -2)
        buttonParams.setMargins(12, 0, 0, 0)
        row.addView(submit, buttonParams)

        val restart = Button(this).apply {
            text = "새 게임"
            setOnClickListener { resetGame() }
        }
        root.addView(restart, LinearLayout.LayoutParams(-1, -2))

        setContentView(root)
    }

    private fun resetGame() {
        used.clear()
        score = 0
        lastWord = null
        computerWord.text = "첫 단어를 입력하세요"
        message.text = if (words.isEmpty()) "txt.txt 사전을 넣으면 바로 시작할 수 있어요." else "사전 ${words.size}개 단어 준비 완료"
        scoreText.text = "점수 0 · 연속 0"
        input.text.clear()
    }

    private fun submitWord() {
        val word = input.text.toString().trim()
        if (word.isEmpty()) return
        if (word.length < 2) {
            message.text = "두 글자 이상 입력해주세요."
            return
        }
        if (words.isNotEmpty() && word !in words) {
            message.text = "사전에 없는 단어예요."
            return
        }
        if (word in used) {
            message.text = "이미 사용한 단어예요."
            return
        }
        val expected = lastWord?.lastOrNull()
        if (expected != null && word.first() != expected) {
            message.text = "‘$expected’으로 시작하는 단어가 필요해요."
            return
        }

        used += word
        score++
        lastWord = word
        computerTurn(word.last())
        input.text.clear()
        input.requestFocus()
    }

    private fun computerTurn(firstChar: Char) {
        val candidates = words.asSequence()
            .filter { it.isNotEmpty() && it.first() == firstChar && it !in used }
            .toList()

        if (candidates.isEmpty()) {
            computerWord.text = "🎉 $lastWord"
            message.text = "컴퓨터가 이어갈 단어를 못 찾았어요! 승리!"
            scoreText.text = "점수 $score · 승리"
            return
        }

        val next = candidates.random()
        used += next
        lastWord = next
        computerWord.text = "컴퓨터: $next"
        message.text = "‘${next.last()}’으로 이어주세요."
        scoreText.text = "점수 $score · 사용 ${used.size}개"
    }
}
