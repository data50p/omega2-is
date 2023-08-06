package com.femtioprocent.omega.value

class Values {
    var hm: HashMap<String, Value>

    init {
	hm = HashMap()
    }

    fun getValue(id: String): Value {
	var v = hm[id]
	if (v == null) {
	    v = Value(id)
	    hm[id] = v
	}
	return v
    }

    fun getInt(id: String): Int {
	val v = getValue(id)
	return v.int
    }

    fun setInt(id: String, a: Int) {
	val v = getValue(id)
	v.int = a
    }

    fun getStr(id: String): String? {
	val v = getValue(id)
	return v.str
    }

    fun setStr(id: String, s: String?) {
	val v = getValue(id)
	v.str = s
    }

    fun getObj(id: String): Any? {
	val v = getValue(id)
	return v.obj
    }

    fun setObj(id: String, o: Any?) {
	val v = getValue(id)
	v.obj = o
    }

    override fun toString(): String {
	return "Value{$hm}"
    }
}
