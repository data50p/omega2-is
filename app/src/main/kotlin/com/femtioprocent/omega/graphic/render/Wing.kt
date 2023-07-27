package com.femtioprocent.omega.graphic.render

import com.femtioprocent.omega.graphic.util.LoadImage
import com.femtioprocent.omega.xml.Element
import java.awt.Component
import java.awt.Dimension
import java.awt.Image
import java.awt.Point

class Wing(comp: Component?, @JvmField var name: String, x: Int, y: Int, @JvmField var layer: Int, ord: Int) {
    @JvmField
    var im: Image
    var dim: Dimension
    @JvmField
    var pos: Point
    @JvmField
    var ord: Int
    @JvmField
    var mirror = 0
    @JvmField
    var scale = 1.0
    @JvmField
    var width: Int
    @JvmField
    var height: Int

    //    public int mirror;
    init {
	im = LoadImage.loadAndWaitOrNull(comp, name, false)!!
	dim = Dimension(im.getWidth(null), im.getHeight(null))
	pos = Point(x, y)
	this.ord = ord
	width = im.getWidth(null)
	height = im.getHeight(null)
    }

    val element: Element
	get() {
	    val el = Element("Wing")
	    el.addAttr("name", name)
	    el.addAttr("layer", "" + layer)
	    el.addAttr("mirror", "" + mirror)
	    el.addAttr("scale", "" + scale)
	    el.addAttr("position", "" + pos.getX() + ' ' + pos.getY())
	    return el
	}
}
