package com.femtioprocent.omega.anim.canvas

import com.femtioprocent.omega.util.Log
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class Key internal constructor(var anim_canvas: AnimCanvas) : KeyAdapter() {
    init {
	anim_canvas.addKeyListener(this)
    }

    override fun keyTyped(k: KeyEvent) {
	Log.getLogger().info(":--: key $k")
    }
}
