package com.femtioprocent.omega.adm.people

abstract class People {
    var name: String = "anonymous"

    override fun toString(): String {
	return "People{$name}"
    }
}
