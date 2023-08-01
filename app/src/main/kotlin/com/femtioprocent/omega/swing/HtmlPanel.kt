package com.femtioprocent.omega.swing

import com.femtioprocent.omega.OmegaContext
import java.awt.BorderLayout
import java.awt.Cursor
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener

/*
 * @(#)HtmlPanel.java	1.14 98/08/26
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.SundryUtils.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */ /*
 * @version 1.14 98/08/26
 * @author Jeff Dinkins
 * @author Tim Prinzing
 * @author Peter Korn (accessibility support)
 */
class HtmlPanel(s: String?) : JPanel(), HyperlinkListener {
    var html: JEditorPane? = null

    init {
	layout = BorderLayout()
	getAccessibleContext().accessibleName = "HTML panel"
	getAccessibleContext().accessibleDescription = "A panel for viewing HTML documents, and following their links"
	try {
	    var url: URL? = null
	    url = try {
		URL(s)
	    } catch (exc: MalformedURLException) {
		OmegaContext.sout_log.getLogger().info(
			"ERR: " + "Attempted to open example.html "
				+ "with a bad URL: " + url
		)
		null
	    }
	    if (url != null) {
		html = JEditorPane(url)
		html!!.isEditable = false
		html!!.addHyperlinkListener(this)
		val scroller = JScrollPane()
		//                scroller.setBorder(swing.loweredBorder);
		val vp = scroller.viewport
		vp.add(html)
		// not in Java 2, 1.3                vp.setBackingStoreEnabled(true);
		add(scroller, BorderLayout.CENTER)
	    }
	} catch (e: MalformedURLException) {
	    OmegaContext.sout_log.getLogger().info("ERR: Malformed URL: $e")
	} catch (e: IOException) {
	    OmegaContext.sout_log.getLogger().info("ERR: IOException: $e")
	}
    }

    /**
     * Notification of a change relative to a
     * hyperlink.
     */
    override fun hyperlinkUpdate(e: HyperlinkEvent) {
	if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
	    linkActivated(e.url)
	}
    }

    /**
     * Follows the reference in an
     * link.  The given url is the requested reference.
     * By default this calls [setPage](#setPage),
     * and if an exception is thrown the original previous
     * document is restored and a beep sounded.  If an
     * attempt was made to follow a link, but it represented
     * a malformed url, this method will be called with a
     * null argument.
     *
     * @param u the URL to follow
     */
    protected fun linkActivated(u: URL?) {
	val c = html!!.cursor
	val waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
	html!!.cursor = waitCursor
	SwingUtilities.invokeLater(PageLoader(u, c))
    }

    fun goTo(s: String?) {
	var url: URL? = null
	url = try {
	    URL(s)
	} catch (exc: MalformedURLException) {
	    OmegaContext.sout_log.getLogger().info(
		    "ERR: " + "Attempted to open example.html "
			    + "with a bad URL: " + url
	    )
	    null
	}
	if (html != null && url != null) {
	    val c = html!!.cursor
	    val waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
	    html!!.cursor = waitCursor
	    SwingUtilities.invokeLater(PageLoader(url, c))
	}
    }

    /**
     * temporary class that loads synchronously (although
     * later than the request so that a cursor change
     * can be done).
     */
    internal inner class PageLoader(var url: URL?, var cursor: Cursor) : Runnable {
	override fun run() {
	    if (url == null) {
		// restore the original cursor
		html!!.cursor = cursor

		// PENDING(prinz) remove this hack when
		// automatic validation is activated.
		val parent = html!!.parent
		parent.repaint()
	    } else {
		val doc = html!!.document
		try {
		    html!!.page = url
		} catch (ioe: IOException) {
		    html!!.document = doc
		    toolkit.beep()
		} finally {
		    // schedule the cursor to revert after
		    // the paint has happended.
		    url = null
		    SwingUtilities.invokeLater(this)
		}
	    }
	}
    }
}
