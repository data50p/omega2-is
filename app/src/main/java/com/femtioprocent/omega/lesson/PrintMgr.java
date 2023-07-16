package com.femtioprocent.omega.lesson;

import com.femtioprocent.omega.OmegaContext;

import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

public class PrintMgr implements Printable {
    class PrintableDoc implements Doc {
        private Printable printable;

        public PrintableDoc(Printable printable) {
            this.printable = printable;
        }

        public DocFlavor getDocFlavor() {
            return DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        }

        public DocAttributeSet getAttributes() {
            return null;
        }

        public Object getPrintData() throws IOException {
            return printable;
        }

        public Reader getReaderForText() throws IOException {
            return null;
        }

        public InputStream getStreamForBytes() throws IOException {
            return null;
        }
    }

    RenderedImage image2;
    ArrayList sentences;
    String lesson_name;
    Font item_fo;

    Font getItemFont() {
        if (item_fo == null)
            item_fo = new Font("Arial", Font.PLAIN, 50);
        return item_fo;
    }

    void setItemFont(Font fo) {
        item_fo = fo;
    }

    public PrinterJob getPrintJob() throws PrinterException {
	PrinterJob job = PrinterJob.getPrinterJob();
	boolean doPrint = job.printDialog();
	if (doPrint) {
	    job.setPrintable(this);
	    return job;
	} else {
	    return null;
	}
    }

    public void doThePrint(PrinterJob job) {
	try {
	    job.print();
	} catch (PrinterException e) {
	    e.printStackTrace();
	}
    }


    void print(PrintService print_service,
	       String title,
	       ArrayList sentences,
	       String lesson_name) throws Exception {
	this.sentences = sentences;
	this.lesson_name = lesson_name;

	try {
	    PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
	    aset.add(OrientationRequested.PORTRAIT);
	    aset.add(new JobName("Omega sentences", null));
	    DocPrintJob pj = print_service.createPrintJob();

	    try {
		Doc doc = new PrintableDoc(this);
		pj.print(doc, aset);
	    } catch (PrintException ex) {
		throw new Exception("warning" + ex);
	    }
	} finally {
	    System.gc();
	}
    }

    void prepare(String title,
	       ArrayList sentences,
	       String lesson_name) throws Exception {
	this.sentences = sentences;
	this.lesson_name = lesson_name;
    }

    int getStringWidth(Graphics2D g2, Font fo, String s) {
        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = fo.getStringBounds(s, frc);
        return (int) r.getWidth();
    }

    int getStringHeight(Graphics2D g2, Font fo, String s) {
        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = fo.getStringBounds(s, frc);
        return (int) r.getHeight();
    }

    int gap = 4;

    int[] getBounding(Graphics2D g2, ArrayList li) {
        int WW = 0;
        int HH = 0;
        if (li == null)
            return new int[]{500, 350};

        Iterator it = li.iterator();
        while (it.hasNext()) {
            String sent = (String) it.next();
            int sh = getStringHeight(g2, getItemFont(), sent);
            int sw = getStringWidth(g2, getItemFont(), sent);
            HH += sh + gap;
            WW = sw > WW ? sw : WW;
        }
        OmegaContext.sout_log.getLogger().info(":--: " + "bounding is " + WW + ' ' + HH);
        return new int[]{WW, HH};
    }

    public int print(Graphics g, PageFormat pf, int pageIndex) {
        ArrayList sentences = this.sentences;

        try {
            double pfW = pf.getImageableWidth();
            double pfH = pf.getImageableHeight();
            double pfX = pf.getImageableX();
            double pfY = pf.getImageableY();

// 	    double scaleX = pfW / imW;
// 	    double scaleY = pfH / imH;

            OmegaContext.sout_log.getLogger().info(":--: " + "print..." + pageIndex);
            OmegaContext.sout_log.getLogger().info(":--: " + "size " + /* imW + ' ' + imH + ' ' + */ pfW + ' ' + pfH);
            OmegaContext.sout_log.getLogger().info(":--: " + "po " + pfX + ' ' + pfY);
//	    OmegaContext.sout_log.getLogger().info(":--: " + "scale " + scaleX + ' ' + scaleY);

            if (pageIndex == 0) {
                Graphics2D g2 = (Graphics2D) g;
                //AffineTransform at = new AffineTransform();
                AffineTransform at = g2.getTransform();
                OmegaContext.sout_log.getLogger().info(":--: " + "at " + at);
                at.translate(pfX, pfY);
                g2.setTransform(at);

// 		g2.drawRect(0, 0, 10, 10);
// 		g2.drawRect(10, 10, 90, 90);
// 		g2.drawRect((int)(pfW-10-1), (int)(pfH-10-1), 10, 10);

                setItemFont(new Font("Arial", Font.PLAIN, 20));
                int[] bounding = getBounding(g2, sentences);

                while (bounding[0] > pfW || bounding[1] > pfH) {
                    int size = getItemFont().getSize();
                    setItemFont(new Font("Arial", Font.PLAIN, (int) (size * 0.9)));
                    bounding = getBounding(g2, sentences);
                    OmegaContext.sout_log.getLogger().info(":--: " + "font size is " + getItemFont().getSize());

                }

                g2.setFont(getItemFont());

                int x = 0;
                int y = 0;
                Iterator it = sentences.iterator();
                int cnt = 0;
                int sh = getStringHeight(g2, getItemFont(), "Aj");
                y += sh + 5;

                g2.drawString(lesson_name, x, y);

                y += sh + sh + gap * 2;
                while (it.hasNext()) {
                    String sent = (String) it.next();
                    g2.drawString(sent, x, y);
                    y += sh + gap;
                }

                return Printable.PAGE_EXISTS;
            } else {
                return Printable.NO_SUCH_PAGE;
            }
        } finally {
            System.gc();
        }
    }
}
