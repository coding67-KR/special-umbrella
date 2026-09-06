package com.coding67.kkeutmal

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    private val used = mutableSetOf<String>()
    private val words = mutableSetOf<String>()
    private lateinit var status: TextView
    private lateinit var turn: TextView
    private lateinit var input: EditText
    private lateinit var scoreView: TextView
    private var lastWord: String? = null
    private var score = 0
    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadDictionary()
        buildUi()
        resetGame()
    }

    private fun loadDictionary() {
        words.clear()
        runCatching {
            assets.open("txt.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
                lines.forEach { rawLine ->
                    val line = rawLine.trim()
                    if (line.isEmpty() || line.startsWith("#")) return@forEach

                    // 한 줄에 단어만 있는 형식뿐 아니라
                    // "단어\t기타정보", "단어 기타정보" 같은 사전 형식도 지원한다.
                    val candidate = line
                        .split('\t', ' ', ',', '|')
                        .firstOrNull()
                        ?.trim()
                        .orEmpty()

                    if (isValidWord(candidate)) {
                        words += candidate
                    }
                }
            }
        }
    }

    private fun isValidWord(word: String): Boolean {
        return word.length >= 2 && word.all { ch -> ch in '\uAC00'..'\uD7A3' }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(Color.rgb(247, 247, 251))
        }

        val title = TextView(this).apply {
            text = "끝말잇기"
            textSize = 30f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(31, 41, 55))
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        scoreView = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 18)
            setTextColor(Color.rgb(79, 70, 229))
        }
        root.addView(scoreView, LinearLayout.LayoutParams(-1, -2))

        turn = TextView(this).apply {
            textSize = 27f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 22, 0, 14)
            setTextColor(Color.rgb(17, 24, 39))
        }
        root.addView(turn, LinearLayout.LayoutParams(-1, -2))

        status = TextView(this).apply {
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 26)
            setTextColor(Color.rgb(75, 85, 99))
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))

        val inputRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        input = EditText(this).apply {
            hint = "단어 입력"
            inputType = 1
            imeOptions = 6
            textSize = 18f
            setSingleLine()
            setPadding(16, 12, 16, 12)
            setOnEditorActionListener { _, _, _ -> submitWord(); true }
        }
        inputRow.addView(input, LinearLayout.LayoutParams(0, -2, 1f))

        val submit = Button(this).apply {
            text = "입력"
            setOnClickListener { submitWord() }
        }
        inputRow.addView(submit, LinearLayout.LayoutParams(-2, -2).apply { setMargins(10, 0, 0, 0) })
        root.addView(inputRow, LinearLayout.LayoutParams(-1, -2))

        val restart = Button(this).apply {
            text = "새 게임"
            setOnClickListener { resetGame() }
        }
        root.addView(restart, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 14 })

        setContentView(root)
    }

    private fun resetGame() {
        used.clear()
        lastWord = null
        score = 0
        gameOver = false
        turn.text = "첫 단어를 입력하세요"
        scoreView.text = "점수 0 · 사용 0개"
        status.text = if (words.isEmpty()) "txt.txt에서 읽을 수 있는 단어가 없어요." else "사전 ${words.size}개 · 내가 먼저 시작"
        input.text.clear()
        input.isEnabled = true
    }

    private fun submitWord() {
        if (gameOver) return
        val word = input.text.toString().trim()
        if (word.isEmpty()) {
            status.text = "단어를 입력해주세요."
            return
        }
        if (!isValidWord(word)) {
            status.text = "한글 두 글자 이상의 단어만 사용할 수 있어요."
            return
        }
        if (word !in words) {
            status.text = "txt.txt 사전에 없는 단어예요."
            return
        }
        if (word in used) {
            status.text = "이미 사용한 단어예요."
            return
        }
        val required = lastWord?.lastOrNull()
        if (required != null && word.first() != required) {
            status.text = "‘$required’으로 시작하는 단어가 필요해요."
            return
        }

        used += word
        score++
        lastWord = word
        input.text.clear()
        computerTurn(word.last())
    }

    private fun computerTurn(first: Char) {
        val candidates = words.asSequence()
            .filter { it.isNotEmpty() && it.first() == first && it !in used }
            .toList()

        if (candidates.isEmpty()) {
            gameOver = true
            turn.text = "🎉 승리!"
            status.text = "컴퓨터가 이어갈 단어를 찾지 못했어요."
            scoreView.text = "최종 점수 $score · 총 사용 ${used.size}개"
            input.isEnabled = false
            return
        }

        // 너무 단순하게 '가장 긴 단어' 하나만 고르지 않고,
        // 후보 중 긴 단어들을 묶어 약간의 랜덤성을 준다.
        val maxLength = candidates.maxOf { it.length }
        val strongCandidates = candidates.filter { it.length >= maxLength - 1 }
        val next = strongCandidates.random()

        used += next
        lastWord = next
        turn.text = "컴퓨터: $next"
        status.text = "‘${next.last()}’으로 시작하는 단어를 입력하세요."
        scoreView.text = "점수 $score · 사용 ${used.size}개"
    }
}
