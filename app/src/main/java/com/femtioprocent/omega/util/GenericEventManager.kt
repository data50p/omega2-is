package com.femtioprocent.omega.util

class GenericEventManager {
    var li: MutableList<GenericEventListener> = ArrayList()
    fun fireGenericEvent(ge: GenericEvent?, a: Any?) {
	val it: Iterator<GenericEventListener> = li.iterator()
	while (it.hasNext()) {
	    if (true /*gel.grp.equals(ge.grp) */) it.next().genericEvent(ge, a)
	}
    }

    fun addGenericEventListener(gel: GenericEventListener) {
	li.add(gel)
    }
}
