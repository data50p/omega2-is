package com.femtioprocent.omega.anim.appl

import com.femtioprocent.omega.help.HelpSystem

class Anim_HelpSystem : HelpSystem() {
    fun showManualL() {
	show(mkFileName("lesson_manual"), 800, 600)
    }

    fun showManualAE() {
	show(mkFileName("editor_manual"), 800, 600)
    }
}
