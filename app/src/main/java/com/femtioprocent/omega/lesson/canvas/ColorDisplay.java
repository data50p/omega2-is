package com.femtioprocent.omega.lesson.canvas;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.appl.LessonEditor;
import com.femtioprocent.omega.swing.ColorChooser;
import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

public class ColorDisplay extends JDialog implements ActionListener {
    int WW = 700;
    int HH = 500;
    HashMap<String, BaseCanvas.ColorColors> colors_orig;
    public HashMap<String, BaseCanvas.ColorColors> colors;
    public boolean select = false;
    String who;

    JComboBox color_file;

    static FilenameFilter fnf = new FilenameFilter() {
	public boolean accept(File dir, String fname) {
	    if (fname.endsWith(".omega_colors"))
		return true;
	    return false;
	}
    };


    class myItemListener implements ItemListener {
	public void itemStateChanged(ItemEvent ie) {
	    JComboBox cb = (JComboBox) ie.getItemSelectable();
	    if (ie.getStateChange() == ItemEvent.SELECTED)
		if (cb == color_file) {
		    String fn = (String) cb.getSelectedItem();
//log		    OmegaContext.sout_log.getLogger().info(":--: " + "FILE sel " + fn);
		    HashMap<String,Color> hm = Lesson.getColors(fn, who);
		    if (hm != null) {
			colors = new HashMap<>();
			for(Map.Entry<String,Color> ent : hm.entrySet())
			    colors.put(ent.getKey(), new BaseCanvas.ColorColors(ent.getValue()));
			repaint();
		    }
		}
	}
    }

    ;
    myItemListener myiteml = new myItemListener();

    class Canvas extends JPanel {
	Canvas() {
	}

	int getCaW() {
	    return getWidth();
	}

	int getCaH() {
	    return getHeight();
	}

	int gW(double f) {
	    return (int) (f * getCaW());
	}

	int gH(double f) {
	    return (int) (f * getCaH());
	}

	public void paintComponent(Graphics g) {
	    Graphics2D g2 = (Graphics2D) g;

	    RenderingHints rh = g2.getRenderingHints();
	    rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
	    g2.setRenderingHints(rh);

	    int hh = gW(0.3);
	    GradientPaint pa = new GradientPaint(0.0f, 0.0f, getColor("bg_t"),
		    0.0f, (float) hh, getColor("bg_m"));
	    g2.setPaint(pa);
	    g2.fill(new Rectangle(0, 0, getCaW(), hh));

	    pa = new GradientPaint(0.0f, (float) hh, getColor("bg_m"),
		    0.0f, (float) getCaH(), getColor("bg_b"));
	    g2.setPaint(pa);
	    g2.fill(new Rectangle(0, hh - 1, getCaW(), getCaH() - hh + 1));

	    if (who.equals("selector")) {
		GradientPaint pa2 = new GradientPaint(0.0f, gH(0.01), getColor("bg_b"),
			0.0f, gH(0.98), getColor("bg_t"));
		g2.setPaint(pa2);
		g2.fill(new Rectangle(gW(0.25), gH(0.1), gW(0.7), gH(0.8)));
	    }

	    if (!who.equals("words")) {
		int l1 = (int) (0.1 * getCaW());
		int w1 = (int) (0.8 * getCaW());
		int t1 = (int) (0.1 * getCaH());
		int h1 = (int) (0.2 * getCaH());
		int h1lowoffs = (int) (0.15 * getCaH());
		int h1low = (int) (0.05 * getCaH());
		int l1_1 = (int) (0.3 * getCaW());
		int w1_1 = (int) (0.25 * getCaW());
		int rad = 15;

		if (!who.equals("main")) {
		    RoundRectangle2D rr = new RoundRectangle2D.Double(l1, t1, w1, h1, rad, rad);
		    g2.setColor(getColor("bg_frbg"));
		    g2.fill(rr);

		    g2.setColor(getColor("bg_fr"));
		    BasicStroke stroke = new BasicStroke(getCaH() / 100.0f);
		    g2.setStroke(stroke);
		    g2.draw(rr);

		    g2.setColor(getColor("bg_tx"));
		    Font fo = new Font("Arial", Font.PLAIN, (int) (h1 * 0.65));
		    g2.setFont(fo);
		    g2.drawString("Message area", l1 + 10, (int) (t1 + (h1 * 0.7)));
		}
	    } else {
		if (true) {
		    int l1 = (int) (0.1 * getCaW());
		    int w1 = (int) (0.8 * getCaW());
		    int t1 = (int) (0.1 * getCaH());
		    int h1 = (int) (0.2 * getCaH());
		    int h1lowoffs = (int) (0.15 * getCaH());
		    int h1low = (int) (0.05 * getCaH());
		    int l1_1 = (int) (0.3 * getCaW());
		    int w1_1 = (int) (0.25 * getCaW());

		    Rectangle2D r = new Rectangle2D.Double(l1, t1, w1, h1);
		    g2.setColor(getColor("sn_bg"));
		    g2.fill(r);

		    r = new Rectangle2D.Double(l1_1, t1 + h1lowoffs, w1_1, h1low);
		    g2.setColor(getColor("sn_hi"));
		    g2.fill(r);

		    r = new Rectangle2D.Double(l1, t1, w1, h1);
		    g2.setColor(getColor("sn_fr"));
		    BasicStroke stroke = new BasicStroke(getCaH() / 100.0f);
		    g2.setStroke(stroke);
		    g2.draw(r);

		    g2.setColor(getColor("sn_tx"));
		    Font fo = new Font("Arial", Font.PLAIN, (int) (h1 * 0.65));
		    g2.setFont(fo);
		    g2.drawString("The fox jumps down", l1 + 10, (int) (t1 + (h1 * 0.7)));
		}

		String sa[] = {"The fox", "jumps", "down"};

		for (int i = 0; i < 3; i++) {
		    int l1 = (int) (0.1 * getCaW());
		    int w1 = (int) (0.8 * getCaW());
		    int t1 = (int) ((0.4 + i * 0.18) * getCaH());
		    int h1 = (int) (0.15 * getCaH());
		    double ra = 15;

		    RoundRectangle2D r = new RoundRectangle2D.Double(l1, t1, w1, h1, ra, ra);
		    g2.setColor(getColor(i == 1 ? "bt_hi" : i == 2 ? "bt_hs" : "bt_bg"));
		    g2.fill(r);

		    g2.setColor(getColor(i == 1 ? "bt_fr_hi" : i == 2 ? "bt_fr_hs" : "bt_fr"));
		    BasicStroke stroke = new BasicStroke(getCaH() / 100.0f);
		    g2.setStroke(stroke);
		    g2.draw(r);

		    g2.setColor(getColor(i == 1 ? "bt_tx_hi" : i == 2 ? "bt_tx_hs" : "bt_tx"));
		    Font fo = new Font("Arial", Font.PLAIN, (int) (h1 * 0.65));
		    g2.setFont(fo);
		    g2.drawString(sa[i], l1 + 10, (int) (t1 + (h1 * 0.8)));
		}

		if (true) {
		    int x = (int) (0.45 * getCaW());
		    int y = (int) (0.5 * getCaH());
		    int w = (int) (0.48 * getCaW());
		    int h = (int) (0.3 * getCaH());

		    RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 15, 15);
		    g2.setColor(getColor("bg_frbg"));
		    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
		    g2.fill(r);
		    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

		    g2.setColor(getColor("bg_fr"));
		    BasicStroke stroke = new BasicStroke(getCaH() / 100.0f);
		    g2.setStroke(stroke);
		    g2.draw(r);

		    g2.setColor(getColor("bg_tx"));
		    Font fo = new Font("Arial", Font.PLAIN, (int) (h * 0.35));
		    g2.setFont(fo);
		    g2.drawString("Message", x + 10, (int) (y + (h * 0.6)));
		}
	    }
	}
    }

    Canvas can;

    Color getColor(String id) {
	return (Color) colors.get(id).color;
    }

    public ColorDisplay(HashMap<String, BaseCanvas.ColorColors> colors, String who) {
	super(LessonEditor.TOP_JFRAME, true);
	this.who = who;
	this.colors_orig = colors;
	this.colors = (HashMap<String, BaseCanvas.ColorColors>) (colors.clone());
	build();
	setSize(WW, HH);
    }

    void ma(JMenu jm, String txt, String cmd) {
	JMenuItem mi = new JMenuItem(txt);
	mi.setActionCommand(cmd);
	mi.addActionListener(this);
	jm.add(mi);
    }

    public void actionPerformed(ActionEvent ae) {
	String cmd;
//  	cmd = (String)mi.getActionCommand();
	cmd = (String) ae.getActionCommand();
	if (cmd.equals("select")) {
	    colors_orig.putAll(colors);
	    select = true;
	    setVisible(false);
	    return;
	}
	if (cmd.equals("restore")) {
	    colors = colors_orig;
//	    color_file.setSelectedIndex(0);
	    repaint();
	    return;
	}
	if (cmd.equals("dismiss")) {
	    setVisible(false);
	    return;
	}
//	JMenuItem mi = (JMenuItem)ae.getSource();
	Color c = colors.get(cmd).color;
	Color nc = ColorChooser.select(c);
	if (nc != null)
	    colors.put(cmd, new BaseCanvas.ColorColors(nc));
	repaint();
    }

    @Deprecated
    private void fillColorFiles() {
	File dir = new File(OmegaContext.omegaAssets("."));
	String[] sa = dir.list(fnf);
	for (int i = 0; i < sa.length; i++)
	    if (color_file != null)
		color_file.addItem(sa[i]);
    }

    void build() {
	can = new Canvas();
	Container c = getContentPane();

	JPanel pan_n = new JPanel();

// 	color_file = new JComboBox();
// 	color_file.addItemListener(myiteml);
// 	color_file.addItem("");

	fillColorFiles();

// 	pan_n.add(color_file);
	JButton b;
	b = new JButton("Restore");
	b.setActionCommand("restore");
	b.addActionListener(this);
	pan_n.add(b);

	c.add(pan_n, BorderLayout.NORTH);

	c.add(can, BorderLayout.CENTER);
	JPanel pan = new JPanel();
	pan.add(b = new JButton(T.t("Select")));
	b.setActionCommand("select");
	b.addActionListener(this);

	pan.add(b = new JButton(T.t("Cancel")));
	b.setActionCommand("dismiss");
	b.addActionListener(this);

	c.add(pan, BorderLayout.SOUTH);
	JMenuBar mb = new JMenuBar();
	setJMenuBar(mb);
	JMenu jm = new JMenu(T.t("Background"));
	mb.add(jm);
	ma(jm, T.t("Top"), "bg_t");
	ma(jm, T.t("Middle"), "bg_m");
	ma(jm, T.t("Bottom"), "bg_b");

	if (!who.equals("main")) {
	    jm = new JMenu(T.t("MessageArea"));
	    mb.add(jm);
	    ma(jm, T.t("Background"), "bg_frbg");
	    ma(jm, T.t("Text"), "bg_tx");
	    ma(jm, T.t("Frame"), "bg_fr");
	}

	if (!who.equals("words")) {
	} else {
	    jm = new JMenu(T.t("Sentence"));
	    mb.add(jm);
	    ma(jm, T.t("Background"), "sn_bg");
	    ma(jm, T.t("Hilite"), "sn_hi");
	    jm.addSeparator();
	    ma(jm, T.t("Text"), "sn_tx");
	    ma(jm, T.t("Frame"), "sn_fr");

	    jm = new JMenu(T.t("Words"));
	    mb.add(jm);
	    ma(jm, T.t("Background"), "bt_bg");
	    ma(jm, T.t("Background Hilite"), "bt_hi");
	    ma(jm, T.t("Background Hilite Selected"), "bt_hs");
	    jm.addSeparator();
	    ma(jm, T.t("Text"), "bt_tx");
	    ma(jm, T.t("Text Hilite"), "bt_tx_hi");
	    ma(jm, T.t("Text Hilite Selected"), "bt_tx_hs");
	    jm.addSeparator();
	    ma(jm, T.t("Frame"), "bt_fr");
	    ma(jm, T.t("Frame Hilite"), "bt_fr_hi");
	    ma(jm, T.t("Frame Hilite Selected"), "bt_fr_hs");
	}
    }
}
