package com.femtioprocent.omega.util

import femtioprocent.ansi.Color5
import femtioprocent.logging.MyLogger
import java.util.logging.Logger

object Log {
    @JvmStatic
    fun getLogger(): Logger {
	return omegaLog
    }

    val omegaLog = MyLogger.theLogger("omega.log", Color5.colorFun(Color5.ColorValue.YELLOW))
}
