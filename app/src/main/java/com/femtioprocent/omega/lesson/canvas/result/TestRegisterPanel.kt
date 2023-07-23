package com.femtioprocent.omega.lesson.canvas.resultimport

import com.femtioprocent.omega.adm.register.data.TestEntry
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class TestRegisterPanel internal constructor() : RegisterPanel() {
    var type: JLabel? = null
    var lduration: JLabel? = null
    var answer: JTextField? = null
    var sentence: JTextField? = null
    var duration: JTextField? = null

    init {
	background = Color(240, 240, 200)
    }

    override fun populate() {
	val pan: JPanel = this
	type = JLabel("")
	lduration = JLabel("Time: ")
	duration = JTextField("0.0")
	answer = JTextField(30)
	sentence = JTextField(30)
	var X = 0
	var Y = 0
	pan.add(JLabel("Test:"), gbcf.createL(X, Y, 1))
	pan.add(type, gbcf.createL(++X, Y, 1))
	Y++
	X = 0
	pan.add(JLabel("Pupil answer:"), gbcf.createL(X, Y, 1))
	pan.add(answer, gbcf.createL(++X, Y, 1))
	Y++
	X = 0
	pan.add(JLabel("Correct answer:"), gbcf.createL(X, Y, 1))
	pan.add(sentence, gbcf.createL(++X, Y, 1))
	pan.add(lduration, gbcf.createL(++X, Y, 1))
	pan.add(duration, gbcf.createL(++X, Y, 1))
    }

    fun set(te: TestEntry) {
	answer!!.text = te.answer
	sentence!!.text = te.sentence
	duration!!.text = "" + te.duration / 1000.0
    }
}
