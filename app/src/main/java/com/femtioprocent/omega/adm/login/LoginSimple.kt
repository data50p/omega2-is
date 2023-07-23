package com.femtioprocent.omega.adm.login

import com.femtioprocent.omega.util.Log
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*
import kotlin.concurrent.withLock

class LoginSimple : Login() {
    var comp: JComponent

    internal inner class Comp : JComponent() {
	var login: JButton
	var cancel: JButton
	val tf: JTextField

	init {
	    layout = BorderLayout()
	    val p = JPanel()
	    val p2 = JPanel()
	    p.layout = BorderLayout()
	    p2.layout = FlowLayout()
	    login = JButton("Login")
	    cancel = JButton("As Guest")
	    tf = JTextField(20)
	    val text = JTextArea("""
	Ange ditt namn
	Tryck sedan på 'Login'
	""".trimIndent())
	    text.isEditable = false
	    text.background = cancel.background
	    p2.add(login)
	    p2.add(cancel)
	    p.add(tf, BorderLayout.NORTH)
	    p.add(p2, BorderLayout.SOUTH)
	    add(text, BorderLayout.NORTH)
	    add(p, BorderLayout.CENTER)
	    login.addActionListener {
		lock.withLock {
		    if (tf.text.length > 0) {
			Log.getLogger().info(":--: " + "action " + tf.text)
			this@LoginSimple.setName(tf.text)
			ready = true
			condition.signal()
		    }
		}
	    }
	    cancel.addActionListener {
		lock.withLock {
		    tf.text = ""
		    name = null
		    ready = true
		    condition.signal()
		}
	    }
	}
    }

    init {
	comp = Comp()
    }

    override fun getComp_(): JComponent {
	return comp
    }
}
