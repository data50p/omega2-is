package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.util.ExtensionFileFilter
import darrylbu.util.SwingUtils
import java.awt.Container
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import javax.swing.*
import javax.swing.filechooser.FileSystemView
import javax.swing.filechooser.FileView

class ChooseOmegaAssetsDir : JFileChooser(File(".")) {
    init {
	val jTextField = SwingUtils.getDescendantOfType(
	    JTextField::class.java, this, "Text", ""
	)
	jTextField.isEditable = false
	val list2 = SwingUtils.getDescendantOfType(JList::class.java, this, "Enabled", true)
	if (list2 != null) {
	    val mouseListener = list2.mouseListeners[2]
	    list2.removeMouseListener(mouseListener)
	    list2.addMouseListener(object : MouseAdapter() {
		override fun mouseClicked(e: MouseEvent) {
		    if (e.clickCount == 2) {
			mouseListener.mouseClicked(e)
		    }
		}
	    })
	}
	val fi = ExtensionFileFilter()
	fi.addExtension(ext)
	fileFilter = fi
	isMultiSelectionEnabled = false
	disableNav(this)
	fileSelectionMode = DIRECTORIES_ONLY
	fileView = object : FileView() {
	    override fun isTraversable(f: File): Boolean {
		return f.isDirectory && f.name.endsWith("." + ext)
	    }
	}
	fileSystemView = object : FileSystemView() {
	    @Throws(IOException::class)
	    override fun createNewFolder(containingDir: File): File? {
		return null
	    }
	}
    }

    private fun disableNav(c: Container) {
	var jbCnt = 0
	for (x in c.components) if (x is JComboBox<*>) x.setEnabled(false) else if (x is JButton) {
	    val text = x.text
	    if (text == null || text.isEmpty()) x.isEnabled = false
	    if (text != null && text.length > 0) {
		jbCnt++
		if (jbCnt == 1) x.isEnabled = false
	    }
	} else if (x is Container) disableNav(x)
    }

    companion object {
	var ext = "omega_assets"
    }
}
