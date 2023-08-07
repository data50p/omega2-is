package com.femtioprocent.omega

import com.femtioprocent.omega.util.DelimitedStringBuilder
import com.femtioprocent.omega.util.Files
import org.junit.Assert
import org.junit.Test

class VariousTest {
    @Test
    fun t1() {
	val a = 1
	val b = 1
	Assert.assertEquals("Expected value 1", a, b)
    }

    @Test
    fun sbTest() {
	val sb = DelimitedStringBuilder(",")
	sb.append("aa")
	sb.append("bb")
	Assert.assertEquals(sb.toString(), "aa,bb")
    }

    @Test
    fun slash() {
	val f = Files
	val fn = f.mkRelativeCWD("abc")
	println("" + fn)
    }
}