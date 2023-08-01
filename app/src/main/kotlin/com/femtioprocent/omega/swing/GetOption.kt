package com.femtioprocent.omega.swing

import javax.swing.JOptionPane

object GetOption {
    fun getOption(msg: String?, options: Array<String>): Int {
	return JOptionPane.showOptionDialog(
		null,
		msg,
		"Omega - Option",
		JOptionPane.OK_CANCEL_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		options,
		options[0]
	)
    }

    fun showMsg(msg: String?) {
	JOptionPane.showMessageDialog(
		null,
		msg,
		"Omega - Message",
		JOptionPane.WARNING_MESSAGE
	)
    }
}
