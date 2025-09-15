package com.femtioprocent.omega.appl

open class OmegaAppl(name: String) {
    protected var name: String

    init {
	this.name = "Omega - $name"
	name.also { propHashMap["name"] = it }
	Omega_IS.initFx() // for audio played by JavaFX
    }

    companion object {
	var propHashMap = HashMap<String, String>()
	private var splash: Splash? = Splash()
	var flag : HashMap<String, String> = HashMap()
	var argl : List<String> = listOf()

	fun waitAndCloseSplash() {
	    Splash.waitForIt()
	    closeSplash()
	}

	fun closeSplash() {
	    if (splash != null) {
		splash!!.isVisible = false
		splash = null
	    }
	}
    }
}
