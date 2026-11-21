package com.example.calculator

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var tvExpression: TextView
    private lateinit var tvOp: TextView
    private lateinit var tvDisplay: TextView

    private var acc: Double? = null
    private var op: String? = null
    private var newNumber = true
    private var error = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildLayout())
    }

    private fun buildLayout(): LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(32, 32, 32, 32)

        // 1. Экран отображения (Display)
        val display = LinearLayout(this)
        display.orientation = LinearLayout.VERTICAL
        display.gravity = Gravity.BOTTOM or Gravity.END
        display.layoutParams = LinearLayout.LayoutParams(-1, 0, 1f) // занимает 1/3 высоты

        tvExpression = TextView(this)
        tvExpression.textSize = 18f
        tvExpression.setTextColor(Color.GRAY)

        tvOp = TextView(this)
        tvOp.textSize = 24f
        tvOp.setTextColor(Color.parseColor("#3F51B5"))

        tvDisplay = TextView(this)
        tvDisplay.text = "0"
        tvDisplay.textSize = 56f
        tvDisplay.setTextColor(Color.BLACK)
        tvDisplay.setTypeface(tvDisplay.typeface, Typeface.BOLD)

        display.addView(tvExpression)
        display.addView(tvOp)
        display.addView(tvDisplay)
        root.addView(display)

        // 2. Сетка кнопок (Keyboard)
        val keyboard = LinearLayout(this)
        keyboard.orientation = LinearLayout.VERTICAL
        keyboard.layoutParams = LinearLayout.LayoutParams(-1, 0, 2f) // занимает 2/3 высоты

        val rows = listOf(
            listOf("C", "⌫", "÷", "×"),
            listOf("7", "8", "9", "-"),
            listOf("4", "5", "6", "+"),
            listOf("1", "2", "3", "."),
            listOf("0", "=")
        )

        for (row in rows) {
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)

            for (label in row) {
                val btn = Button(this)
                btn.text = label
                btn.textSize = 22f
                btn.layoutParams = LinearLayout.LayoutParams(0, -1, 1f)
                btn.setOnClickListener { onButtonClick(label) }
                rowLayout.addView(btn)
            }
            keyboard.addView(rowLayout)
        }
        root.addView(keyboard)

        return root
    }

    private fun onButtonClick(label: String) {
        if (error && label != "C") return

        when (label) {
            "C" -> clearAll()
            "⌫" -> backspace()
            "=" -> calculateResult()
            "+", "-", "×", "÷" -> setOperator(label)
            "." -> addDot()
            else -> addDigit(label)
        }
    }

    private fun clearAll() {
        acc = null
        op = null
        newNumber = true
        error = false
        tvExpression.text = ""
        tvOp.text = ""
        tvDisplay.text = "0"
    }

    private fun backspace() {
        if (newNumber) return
        val text = tvDisplay.text.toString()
        if (text.length <= 1) {
            tvDisplay.text = "0"
        } else {
            tvDisplay.text = text.substring(0, text.length - 1)
        }
    }

    private fun addDigit(digit: String) {
        if (newNumber) {
            tvDisplay.text = digit
            newNumber = false
        } else {
            val text = tvDisplay.text.toString()
            if (text == "0") {
                tvDisplay.text = digit
            } else {
                tvDisplay.text = text + digit
            }
        }
    }

    private fun addDot() {
        if (newNumber) {
            tvDisplay.text = "0."
            newNumber = false
            return
        }
        if (!tvDisplay.text.contains(".")) {
            tvDisplay.text = tvDisplay.text.toString() + "."
        }
    }

    private fun setOperator(nextOp: String) {
        val currentValue = tvDisplay.text.toString().toDoubleOrNull() ?: return

        // Логика цепочки: если уже есть операция, считаем промежуточный итог
        if (op != null && acc != null) {
            val result = calculate(acc!!, currentValue, op!!)
            if (result == null) {
                showError()
                return
            }
            acc = result
            tvDisplay.text = format(result)
        } else {
            acc = currentValue
        }

        op = nextOp
        tvOp.text = nextOp
        tvExpression.text = "${format(acc!!)} $nextOp"
        newNumber = true
    }

    private fun calculateResult() {
        if (op == null || acc == null) return

        val currentValue = tvDisplay.text.toString().toDoubleOrNull() ?: return
        val result = calculate(acc!!, currentValue, op!!)

        if (result == null) {
            showError()
            return
        }

        tvExpression.text = "${format(acc!!)} $op ${format(currentValue)} ="
        tvDisplay.text = format(result)
        tvOp.text = ""
        acc = null
        op = null
        newNumber = true
    }

    private fun calculate(a: Double, b: Double, operation: String): Double? {
        return when (operation) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b == 0.0) null else a / b
            else -> null
        }
    }

    private fun format(value: Double): String {
        // Убираем .0 в конце, если число целое
        if (value == value.toLong().toDouble()) {
            return value.toLong().toString()
        }
        // Ограничиваем до 10 знаков после запятой
        return String.format("%.10f", value).trimEnd('0').trimEnd('.')
    }

    private fun showError() {
        tvDisplay.text = "Ошибка"
        tvExpression.text = ""
        tvOp.text = ""
        acc = null
        op = null
        newNumber = true
        error = true
    }
}