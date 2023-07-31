package com.femtioprocent.omega.appl

open class OmegaAppl(name: String) {
    @JvmField
    protected var name: String

    init {
	this.name = "Omega - $name"
	name.also { propHashMap.set("name", it) }
	Omega_IS.initFx() // for audio played by JavaFX
    }

    companion object {
	var propHashMap = HashMap<String, String>()
	var splash: Splash? = Splash()

	@JvmStatic
	fun closeSplash() {
	    if (splash == null) return
	    splash!!.isVisible = false
	    splash = null
	}
    }
}
