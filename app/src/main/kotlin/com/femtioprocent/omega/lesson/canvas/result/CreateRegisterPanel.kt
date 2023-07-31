package com.femtioprocent.omega.lesson.canvas.result

import com.femtioprocent.omega.adm.register.data.CreateEntry
import com.femtioprocent.omega.lesson.canvas.resultimport.RegisterPanel
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class CreateRegisterPanel internal constructor() : RegisterPanel() {
    var type: JLabel? = null
    var lduration: JLabel? = null
    var sentence: JTextField? = null
    var duration: JTextField? = null

    init {
	background = Color(240, 200, 240)
    }

    override fun populate() {
	val pan: JPanel = this
	type = JLabel("")
	lduration = JLabel("Time: ")
	duration = JTextField("0.0")
	sentence = JTextField(30)
	var X = 0
	var Y = 0
	pan.add(JLabel("Test:"), gbcf.createL(X, Y, 1))
	pan.add(type, gbcf.createL(++X, Y, 1))
	Y++
	X = 0
	pan.add(JLabel("Sentence:"), gbcf.createL(X, Y, 1))
	pan.add(sentence, gbcf.createL(++X, Y, 1))
	pan.add(lduration, gbcf.createL(++X, Y, 1))
	pan.add(duration, gbcf.createL(++X, Y, 1))
    }

    fun set(te: CreateEntry) {
	sentence!!.text = te.sentence
	duration!!.text = "" + te.duration / 1000.0
    }
}
