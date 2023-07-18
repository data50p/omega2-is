package com.femtioprocent.omega.adm.people

abstract class People {
    @kotlin.jvm.JvmField
    public var jname: String = "anonymous"

    override fun toString(): String {
	return "People{$jname}"
    }
}
