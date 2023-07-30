package com.femtioprocent.omega.adm.people

abstract class People {
    @kotlin.jvm.JvmField
    public var name: String = "anonymous"

    override fun toString(): String {
	return "People{$name}"
    }
}
