package com.femtioprocent.omega.util

class GenericEventManager {
    var li: MutableList<GenericEventListener> = ArrayList()
    fun fireGenericEvent(ge: GenericEvent?, a: Any?) {
	li.forEach {if (true /*gel.grp.equals(ge.grp) */) it.genericEvent(ge, a) }
    }

    fun addGenericEventListener(gel: GenericEventListener) {
	li.add(gel)
    }
}
