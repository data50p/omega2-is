package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import java.io.File
import javax.swing.JFileChooser

class ChooseGenericFile internal constructor(runtime: Boolean) : JFileChooser(File(omegaAssets(if (runtime) ".." else "."))) {
    constructor() : this(false)
}
