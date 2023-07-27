package com.femtioprocent.omega.message

import javax.swing.event.EventListenerList

class Manager {
    var li = EventListenerList()
    fun addListener(l: Listener) {
	li.add(Listener::class.java, l)
    }

    fun removeListener(l: Listener) {
	li.remove(Listener::class.java, l)
    }

    fun fire(msg: String?) {
	val lia = li.listenerList
	var i = 0
	while (i < lia.size) {
	    (lia[i + 1] as Listener).msg(msg)
	    i += 2
	}
    }

    override fun toString(): String {
	val lia = li.listenerList
	return "Manager{" + lia.size + "}"
    }
}
