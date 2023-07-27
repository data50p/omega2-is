package com.femtioprocent.omega.value

import java.util.*

interface ValuesListener : EventListener {
    fun changed(v: Value?)
}
