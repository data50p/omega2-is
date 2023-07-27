package com.femtioprocent.omega.lesson

import com.femtioprocent.omega.OmegaContext
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.RenderedImage
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterException
import java.awt.print.PrinterJob
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import javax.print.Doc
import javax.print.DocFlavor
import javax.print.PrintException
import javax.print.PrintService
import javax.print.attribute.DocAttributeSet
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.PrintRequestAttributeSet
import javax.print.attribute.standard.JobName
import javax.print.attribute.standard.OrientationRequested

class PrintMgr : Printable {
    internal inner class PrintableDoc(private val printable: Printable) : Doc {
	override fun getDocFlavor(): DocFlavor {
	    return DocFlavor.SERVICE_FORMATTED.PRINTABLE
	}

	override fun getAttributes(): DocAttributeSet? {
	    return null
	}

	@Throws(IOException::class)
	override fun getPrintData(): Any {
	    return printable
	}

	@Throws(IOException::class)
	override fun getReaderForText(): Reader? {
	    return null
	}

	@Throws(IOException::class)
	override fun getStreamForBytes(): InputStream? {
	    return null
	}
    }

    var image2: RenderedImage? = null
    var sentences: ArrayList<String?>? = null
    var lesson_name: String? = null
    var item_fo: Font? = null
    var itemFont: Font?
	get() {
	    if (item_fo == null) item_fo = Font("Arial", Font.PLAIN, 50)
	    return item_fo
	}
	set(fo) {
	    item_fo = fo
	}

    @get:Throws(PrinterException::class)
    val printJob: PrinterJob?
	get() {
	    val job = PrinterJob.getPrinterJob()
	    val doPrint = job.printDialog()
	    return if (doPrint) {
		job.setPrintable(this)
		job
	    } else {
		null
	    }
	}

    fun doThePrint(job: PrinterJob) {
	try {
	    job.print()
	} catch (e: PrinterException) {
	    e.printStackTrace()
	}
    }

    @Throws(Exception::class)
    fun print(
	    print_service: PrintService?,
	    title: String?,
	    sentences: java.util.ArrayList<String?>,
	    lesson_name: String?
    ) {
	this.sentences = sentences
	this.lesson_name = lesson_name
	try {
	    val aset: PrintRequestAttributeSet = HashPrintRequestAttributeSet()
	    aset.add(OrientationRequested.PORTRAIT)
	    aset.add(JobName("Omega sentences", null))
	    val pj = print_service!!.createPrintJob()
	    try {
		val doc: Doc = PrintableDoc(this)
		pj.print(doc, aset)
	    } catch (ex: PrintException) {
		throw Exception("warning$ex")
	    }
	} finally {
	    System.gc()
	}
    }

    @Throws(Exception::class)
    fun prepare(
	    title: String?,
	    sentences: java.util.ArrayList<String?>?,
	    lesson_name: String?
    ) {
	this.sentences = sentences
	this.lesson_name = lesson_name
    }

    fun getStringWidth(g2: Graphics2D, fo: Font, s: String?): Int {
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	val frc = g2.fontRenderContext
	val r = fo.getStringBounds(s, frc)
	return r.width.toInt()
    }

    fun getStringHeight(g2: Graphics2D, fo: Font, s: String?): Int {
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	val frc = g2.fontRenderContext
	val r = fo.getStringBounds(s, frc)
	return r.height.toInt()
    }

    var gap = 4
    fun getBounding(g2: Graphics2D, li: ArrayList<String?>?): IntArray {
	var WW = 0
	var HH = 0
	if (li == null) return intArrayOf(500, 350)
	val it: MutableIterator<String?> = li.iterator()
	while (it.hasNext()) {
	    val sent = it.next()
	    val sh = getStringHeight(g2, itemFont!!, sent)
	    val sw = getStringWidth(g2, itemFont!!, sent)
	    HH += sh + gap
	    WW = if (sw > WW) sw else WW
	}
	OmegaContext.sout_log.getLogger().info(":--: bounding is $WW $HH")
	return intArrayOf(WW, HH)
    }

    override fun print(g: Graphics, pf: PageFormat, pageIndex: Int): Int {
	val sentences = sentences
	return try {
	    val pfW = pf.imageableWidth
	    val pfH = pf.imageableHeight
	    val pfX = pf.imageableX
	    val pfY = pf.imageableY

// 	    double scaleX = pfW / imW;
// 	    double scaleY = pfH / imH;
	    OmegaContext.sout_log.getLogger().info(":--: print...$pageIndex")
	    OmegaContext.sout_log.getLogger().info(":--: size $pfW $pfH")
	    OmegaContext.sout_log.getLogger().info(":--: po $pfX $pfY")
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "scale " + scaleX + ' ' + scaleY);
	    if (pageIndex == 0) {
		val g2 = g as Graphics2D
		//AffineTransform at = new AffineTransform();
		val at = g2.transform
		OmegaContext.sout_log.getLogger().info(":--: at $at")
		at.translate(pfX, pfY)
		g2.transform = at

// 		g2.drawRect(0, 0, 10, 10);
// 		g2.drawRect(10, 10, 90, 90);
// 		g2.drawRect((int)(pfW-10-1), (int)(pfH-10-1), 10, 10);
		itemFont = Font("Arial", Font.PLAIN, 20)
		var bounding = getBounding(g2, sentences)
		while (bounding[0] > pfW || bounding[1] > pfH) {
		    val size = itemFont!!.size
		    itemFont = Font("Arial", Font.PLAIN, (size * 0.9).toInt())
		    bounding = getBounding(g2, sentences)
		    OmegaContext.sout_log.getLogger().info(":--: " + "font size is " + itemFont!!.size)
		}
		g2.font = itemFont
		val x = 0
		var y = 0
		val it: Iterator<*> = sentences!!.iterator()
		val cnt = 0
		val sh = getStringHeight(g2, itemFont!!, "Aj")
		y += sh + 5
		g2.drawString(lesson_name, x, y)
		y += sh + sh + gap * 2
		while (it.hasNext()) {
		    val sent = it.next() as String
		    g2.drawString(sent, x, y)
		    y += sh + gap
		}
		Printable.PAGE_EXISTS
	    } else {
		Printable.NO_SUCH_PAGE
	    }
	} finally {
	    System.gc()
	}
    }
}
