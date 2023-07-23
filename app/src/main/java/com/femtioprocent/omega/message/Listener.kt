package com.femtioprocent.omega.message

import java.util.*

interface Listener : EventListener {
    fun msg(msg: String?)
}
