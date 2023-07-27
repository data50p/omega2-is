package com.femtioprocent.omega.anim.appl

import java.util.*

open interface EditStateListener : EventListener {
    fun dirtyChanged(is_dirty: Boolean)
}
