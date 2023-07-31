package com.femtioprocent.omega.adm.assets

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.lesson.machine.Target
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils

/**
 * Created by lars on 2017-02-12.
 */
class TargetCombinations {
    class TCItem @JvmOverloads constructor(var fn: String, var originalExtention: String? = null) {
	@JvmField
	var exist: Boolean = false

	init {
	    exist = OmegaContext.omegaAssetsExist(fn)
	    if (fn.contains("{")) Log.getLogger().info("MORE using var")
	    if (fn.contains(",")) Log.getLogger().info("MORE using ,")
	    if (!fn.contains("/")) Log.getLogger().info("LESS using /")
	}

	override fun equals(o: Any?): Boolean {
	    if (o == null) return false
	    return if (o !is TCItem) false else o.fn == fn
	}

	override fun hashCode(): Int {
	    return fn.hashCode()
	}

	fun formatOriginalExtention(): String {
	    return if (SundryUtils.empty(originalExtention)) "·" else originalExtention!!
	}
    }

    class Builder {
	var bundle: MutableList<TargetCombinations> = ArrayList()
	var as_one_cache: TargetCombinations? = null
	fun add(tc: TargetCombinations) {
	    bundle.add(tc)
	    as_one_cache = null
	}

	fun asOne(): TargetCombinations {
	    if (as_one_cache != null) return as_one_cache as TargetCombinations
	    val as_one = TargetCombinations()
	    for (tc in bundle) {
		as_one.merge(tc)
	    }
	    as_one_cache = as_one
	    return as_one
	}

	fun srcSize(): Int {
	    return asOne().src_set.size
	}
    }

    var src_set: MutableSet<TCItem> = HashSet()
    var dep_set: MutableSet<TCItem> = HashSet()
    var tg_set: MutableSet<Target> = HashSet()
    private fun merge(tc: TargetCombinations) {
	dep_set.addAll(tc.dep_set)
	src_set.addAll(tc.src_set)
    }
}
