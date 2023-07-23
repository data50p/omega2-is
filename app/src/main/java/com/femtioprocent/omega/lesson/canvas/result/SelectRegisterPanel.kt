package com.femtioprocent.omega.lesson.canvas.resultimport

import com.femtioprocent.omega.adm.register.data.SelectEntry
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class SelectRegisterPanel internal constructor() : RegisterPanel() {
    var type: JLabel? = null
    var lwhen: JLabel? = null
    var extra: JTextField? = null
    var word: JTextField? = null
    var `when`: JTextField? = null

    init {
	background = Color(200, 200, 240)
    }

    override fun populate() {
	val pan: JPanel = this
	type = JLabel("")
	lwhen = JLabel("Time: ")
	`when` = JTextField("0.0")
	extra = JTextField(30)
	word = JTextField(30)
	var X = 0
	var Y = 0
	pan.add(JLabel("Test:"), gbcf.createL(X, Y, 1))
	pan.add(type, gbcf.createL(++X, Y, 1))
	Y++
	X = 0
	pan.add(JLabel("Pupil answer:"), gbcf.createL(X, Y, 1))
	pan.add(extra, gbcf.createL(++X, Y, 1))
	Y++
	X = 0
	pan.add(JLabel("Correct answer:"), gbcf.createL(X, Y, 1))
	pan.add(word, gbcf.createL(++X, Y, 1))
	pan.add(lwhen, gbcf.createL(++X, Y, 1))
	pan.add(`when`, gbcf.createL(++X, Y, 1))
    }

    fun set(se: SelectEntry) {
	extra!!.text = se.extra
	word!!.text = se.word
	`when`!!.text = "" + se.`when` / 1000.0
    }
}
