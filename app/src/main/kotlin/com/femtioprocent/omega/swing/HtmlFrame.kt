package com.femtioprocent.omega.swing

import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

class HtmlFrame(url_s: String) : JFrame("Omega - " + base(url_s)) {
    var htmlp: HtmlPanel? = null

    init {
	init(url_s)
    }

    private fun init(url_s: String) {
	if (htmlp != null) return
	htmlp = HtmlPanel(url_s)
	val co = contentPane
	co.layout = BorderLayout()
	co.add(htmlp, BorderLayout.CENTER)
	val jp = JPanel()
	var jb: JButton
	jp.add(JButton("Close").also { jb = it })
	co.add(jp, BorderLayout.SOUTH)
	jb.addActionListener { isVisible = false }
	setSize(700, 500)
    }

    fun goTo(s: String) {
	title = "Omega - " + base(s)
	htmlp!!.goTo(s)
    }

    companion object {
	private fun base(s: String): String {
	    val ix = s.lastIndexOf('/')
	    val ix1 = s.lastIndexOf(':')
	    return if (ix1 != -1 || ix != -1) s.substring((if (ix > ix1) ix else ix1) + 1) else s
	}
    }
}
