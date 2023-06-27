package com.femtioprocent.omega.swing;

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
 */

import com.femtioprocent.omega.OmegaContext;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/*
 * @version 1.14 98/08/26
 * @author Jeff Dinkins
 * @author Tim Prinzing
 * @author Peter Korn (accessibility support)
 */
public class HtmlPanel extends JPanel implements HyperlinkListener {
    JEditorPane html;

    public HtmlPanel(String s) {
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleName("HTML panel");
        getAccessibleContext().setAccessibleDescription("A panel for viewing HTML documents, and following their links");

        try {
            URL url = null;
            try {
                url = new URL(s);
            } catch (java.net.MalformedURLException exc) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "Attempted to open example.html "
                        + "with a bad URL: " + url);
                url = null;
            }

            if (url != null) {
                html = new JEditorPane(url);
                html.setEditable(false);
                html.addHyperlinkListener(this);
                JScrollPane scroller = new JScrollPane();
//                scroller.setBorder(swing.loweredBorder);
                JViewport vp = scroller.getViewport();
                vp.add(html);
// not in Java 2, 1.3                vp.setBackingStoreEnabled(true);
                add(scroller, BorderLayout.CENTER);
            }
        } catch (MalformedURLException e) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Malformed URL: " + e);
        } catch (IOException e) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "IOException: " + e);
        }
    }

    /**
     * Notification of a change relative to a
     * hyperlink.
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            linkActivated(e.getURL());
        }
    }

    /**
     * Follows the reference in an
     * link.  The given url is the requested reference.
     * By default this calls <a href="#setPage">setPage</a>,
     * and if an exception is thrown the original previous
     * document is restored and a beep sounded.  If an
     * attempt was made to follow a link, but it represented
     * a malformed url, this method will be called with a
     * null argument.
     *
     * @param u the URL to follow
     */
    protected void linkActivated(URL u) {
        Cursor c = html.getCursor();
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        html.setCursor(waitCursor);
        SwingUtilities.invokeLater(new PageLoader(u, c));
    }

    public void goTo(String s) {
        URL url = null;
        try {
            url = new URL(s);
        } catch (java.net.MalformedURLException exc) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Attempted to open example.html "
                    + "with a bad URL: " + url);
            url = null;
        }
        if (html != null && url != null) {
            Cursor c = html.getCursor();
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            html.setCursor(waitCursor);
            SwingUtilities.invokeLater(new PageLoader(url, c));
        }
    }

    /**
     * temporary class that loads synchronously (although
     * later than the request so that a cursor change
     * can be done).
     */
    class PageLoader implements Runnable {

        PageLoader(URL u, Cursor c) {
            url = u;
            cursor = c;
        }

        public void run() {
            if (url == null) {
                // restore the original cursor
                html.setCursor(cursor);

                // PENDING(prinz) remove this hack when
                // automatic validation is activated.
                Container parent = html.getParent();
                parent.repaint();
            } else {
                Document doc = html.getDocument();
                try {
                    html.setPage(url);
                } catch (IOException ioe) {
                    html.setDocument(doc);
                    getToolkit().beep();
                } finally {
                    // schedule the cursor to revert after
                    // the paint has happended.
                    url = null;
                    SwingUtilities.invokeLater(this);
                }
            }
        }

        URL url;
        Cursor cursor;
    }

}
