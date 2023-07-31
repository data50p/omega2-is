package com.femtioprocent.omega.xml

abstract class Node {
    abstract fun render(
	sbu: StringBuffer,
	sbl: StringBuffer?
    )

    fun render(sb: StringBuffer) {
	render(sb, null)
    }
}
