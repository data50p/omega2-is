package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.xml.Element
import java.lang.reflect.InvocationTargetException

object TriggerEventFactory {
    fun getTriggerEvent(ix: Int): Class<*>? {
	when (ix) {
	    0 -> return TriggerEventPlaySound::class.java
	    1 -> return TriggerEventRotate::class.java
	    2 -> return TriggerEventScale::class.java
	    3 -> return TriggerEventImageAttrib::class.java
	    4 -> return TriggerEventSetLayer::class.java
	    5 -> return TriggerEventSetVisibility::class.java
	    6 -> return TriggerEventSetAnimSpeed::class.java
	    7 -> return TriggerEventSetMirror::class.java
	    8 -> return TriggerEventDinner::class.java
	    9 -> return TriggerEventOption::class.java
	    10 -> return TriggerEventResetSequence::class.java
	}
	return null
    }

    fun getSlot(name: String): Int {
	if ("PlaySound" == name) return 0
	if ("Rotate" == name) return 1
	if ("Scale" == name) return 2
	if ("ImageAttrib" == name) return 3
	if ("SetLayer" == name) return 4
	if ("SetVisibility" == name) return 5
	if ("SetAnimSpeed" == name) return 6
	if ("SetMirror" == name) return 7
	if ("Dinner" == name) return 8
	if ("Option" == name) return 9
	return if ("ResetSequence" == name) 10 else -1
    }

    val size: Int
	get() = allAsStringA.size

    fun createTriggerEvent(name: String): TriggerEvent? {
	try {
	    val cl = Class.forName("com.femtioprocent.omega.anim.tool.timeline.TriggerEvent$name")
	    val te = cl.getDeclaredConstructor().newInstance() as TriggerEvent
	    te.name = name
	    return te
	} catch (ex: IllegalAccessException) {
	    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
	} catch (ex: InstantiationException) {
	    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
	} catch (ex: ClassNotFoundException) {
	    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
	} catch (ex: InvocationTargetException) {
	    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
	} catch (e: NoSuchMethodException) {
	    throw RuntimeException(e)
	}
	return null
    }

    fun createTriggerEvent(e: Element): TriggerEvent? {
	val cmd = e.findAttr("cmd")
	val arg = e.findAttr("arg")
	val is_on = e.findAttr("isOn")
	val te = createTriggerEvent(cmd!!)
	if (te != null) {
	    if (te.hasSelections()) {
		(te as TriggerEventSelections).setArgFromCmd(arg)
	    } else te.setArg_(arg)
	    te.setOn(is_on == "true")
	}
	return te
    }

    val allAsStringA: Array<String?>
	get() {
	    val l: MutableList<String?> = ArrayList()
	    for (i in 0..99) {
		val cl = getTriggerEvent(i) ?: break
		try {
		    val te = cl.getDeclaredConstructor().newInstance() as TriggerEvent
		    val s = te.cmdLabel
		    te.name = s
		    l.add(s)
		} catch (ex: IllegalAccessException) {
		    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
		} catch (ex: InstantiationException) {
		    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
		} catch (ex: InvocationTargetException) {
		    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
		} catch (e: NoSuchMethodException) {
		    throw RuntimeException(e)
		}
	    }
	    return l.toTypedArray<String?>()
	}

    operator fun get(ix: Int): TriggerEvent? {
	val cl = getTriggerEvent(ix)
	if (cl != null) try {
	    return cl.newInstance() as TriggerEvent
	} catch (ex: IllegalAccessException) {
	    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
	} catch (ex: InstantiationException) {
	    Log.getLogger().info("ERR: TriggerEventFactory: $ex")
	}
	return null
    }
}
