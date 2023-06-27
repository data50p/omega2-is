package com.femtioprocent.omega.anim.panels.cabaret;

import com.femtioprocent.omega.anim.cabaret.Actor;
import com.femtioprocent.omega.anim.cabaret.GImAE;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.swing.GBC_Factory;
import com.femtioprocent.omega.swing.properties.OmegaProperties;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class CabaretProperties extends OmegaProperties implements ActionListener {
    //ListSelectionListener {
    static final int IM_W = 300;
    static final int IM_H = 100;

    GBC_Factory gbcf = new GBC_Factory();

    Actor bound_act = null;
    int bound_act_ixx = -1;
    CabaretPanel cabp;

    JTextField lesson_id;
    JTextField var1, var2, var3;
    JTextField image_name;
    JTextField image_petasknid;
    JTextField prim_scale;
    JComboBox prim_mirror;
    JTextField hotspot;
    JTextField rhotspot;
    JButton delete;

    JPanel im_pan;
    private Mouse m;

    private int hotspot_click = -1;
    private int hotspot_near = -1;

    private int hotx;
    private int hoty;
    private int hotx1;
    private int hoty1;
    private int hotx2;
    private int hoty2;

    private boolean skipDirty = false;

    public CabaretProperties(JFrame owner, CabaretPanel cabp) {
        super(owner);
        skipDirty = true;
        this.cabp = cabp;
        setTitle("Omega - " + T.t("Actor Properties"));
        setSize(300, 200);
        buildProperties();
        m = new Mouse(im_pan);
        skipDirty = false;
    }

    private int hitNearest(Point2D p, double lim, int cnt) {
        int r = -1;
        int rr = -1;
        double d = 9999999.9;
        double a;
        if ((a = p.distance(hotx, hoty)) <= d && a <= lim) {
            d = a + 1;
            if (cnt == 0)
                rr = 0;
            r = 0;
        }
        if ((a = p.distance(hotx1, hoty1)) <= d && a <= lim) {
            d = a + 1;
            if (cnt == 1)
                rr = 1;
            r = 1;
        }
        if ((a = p.distance(hotx2, hoty2)) <= d && a <= lim) {
            d = a + 1;
            if (cnt == 2)
                rr = 2;
            r = 2;
        }
        return rr != -1 ? rr : r;
    }

    class Mouse extends MouseInputAdapter {
        Point2D mpress_p;
        private int cnt;

        Mouse(JPanel p) {
            p.addMouseListener(this);
            p.addMouseMotionListener(this);
        }

        public void mousePressed(MouseEvent e) {
            mpress_p = new Point2D.Double(e.getX(), e.getY());

            hotspot_click = hitNearest(mpress_p, 10, cnt);

            repaint();
        }

        public void mouseMoved(MouseEvent e) {
            mpress_p = new Point2D.Double(e.getX(), e.getY());

            int lhsn = hotspot_near;
            hotspot_near = hitNearest(mpress_p, 10, cnt);
            if (hotspot_near == -1 && lhsn >= 0) {
                cnt++;
                cnt %= 3;
            }

            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            mpress_p = new Point2D.Double(e.getX(), e.getY());

            ImageScale imsc = new ImageScale();

            double mx = e.getX();
            double my = e.getY();

            AffineTransform at = new AffineTransform();
            double iwf = imsc.iw * imsc.f;
            double ihf = imsc.ih * imsc.f;
            double hsx = mx / iwf;
            double hsy = my / ihf;
            imsc.gimae.setHotSpotIx(hotspot_click, hsx, hsy);
            hotspot.setText("" + imsc.gimae.getHotSpotAsString(0));
            rhotspot.setText("" + imsc.gimae.getHotSpotAsString(1));
            AnimContext.ae.setDirty(true);
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            hotspot_click = -1;
            hotspot_near = -1;
            repaint();
        }
    }

    private void setDirty() {
        if (skipDirty == false)
            AnimContext.ae.setDirty(true);
    }

    public void setLessonId(String id) {
        lesson_id.setText(id);
        pack();
    }

    public void setTarget(Actor act, int ixx) {
        skipDirty = true;
        bound_act = act;
        bound_act_ixx = ixx;
        if (act != null) {
            image_name.setText(act.gimae.getFNBase());
            image_petasknid.setText(act.gimae.getPeTaskNid());
            lesson_id.setText(act.gimae.getLessonId());
            var1.setText(act.gimae.getVariable(1));
            var2.setText(act.gimae.getVariable(2));
            var3.setText(act.gimae.getVariable(3));
            prim_scale.setText("" + act.gimae.getPrimScale());
            prim_mirror.setSelectedIndex(act.gimae.getPrimMirror());
            hotspot.setText("" + act.gimae.getHotSpotAsString(0));
            rhotspot.setText("" + act.gimae.getHotSpotAsString(1));
        } else {
            image_name.setText("");
            image_petasknid.setText("");
            lesson_id.setText("");
            var1.setText("");
            var2.setText("");
            var3.setText("");
            prim_scale.setText("");
            prim_mirror.setSelectedIndex(0);
            hotspot.setText("");
            hotspot.setText("");
        }
        pack();
        repaint();
        skipDirty = false;
    }

    class myDocumentListener implements DocumentListener {
        public void changedUpdate(DocumentEvent de) {
            Document doc = de.getDocument();
            updDoc(doc);
        }

        public void insertUpdate(DocumentEvent de) {
            Document doc = de.getDocument();
            updDoc(doc);
        }

        public void removeUpdate(DocumentEvent de) {
            Document doc = de.getDocument();
            updDoc(doc);
        }
    }

    myDocumentListener mydocl = new myDocumentListener();

    class myItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent ie) {
            JComboBox cb = (JComboBox) ie.getItemSelectable();
            if (cb == prim_mirror) {
                int a = prim_mirror.getSelectedIndex();
                if (bound_act != null) {
                    bound_act.gimae.setPrimMirror(a);
                    setDirty();
                }
            }
        }
    }

    ;
    myItemListener myiteml = new myItemListener();

//      class OnOffItemEvent implements ItemListener {
//          public void itemStateChanged(ItemEvent ie) {
//  	    JCheckBox cb = (JCheckBox)ie.getItemSelectable();
//  	    OmegaContext.sout_log.getLogger().info("ERR: " + "+++++++++++++ toggle is " + cb.isSelected());
//  	}
//      };
//      OnOffItemEvent onoff_listener = new OnOffItemEvent();

    class ImageScale {
        GImAE gimae;
        Image im;
        double iw;
        double ih;
        double fx;
        double fy;
        double f = 1.0;

        ImageScale() {
            if (bound_act != null) {
                gimae = bound_act.gimae;
                im = gimae.getBaseImage();
                iw = im.getWidth(null);
                ih = im.getHeight(null);
                fx = IM_W / iw;
                fy = IM_H / ih;
                f = fx < fy ? fx : fy;
            }
        }
    }

    ;

    void drawImage(Graphics g) {
        if (bound_act != null) {
            ImageScale imsc = new ImageScale();
            AffineTransform at = new AffineTransform();
            at.scale(imsc.f, imsc.f);

            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.black);
            g2.fillRect(0, 0, (int) (imsc.iw * imsc.f), (int) (imsc.ih * imsc.f));

            ((Graphics2D) g).drawImage(imsc.im, at, null);

            hotx = (int) (imsc.iw * imsc.f * imsc.gimae.hotspot.getX(0));
            hoty = (int) (imsc.ih * imsc.f * imsc.gimae.hotspot.getY(0));
            hotx1 = (int) (imsc.iw * imsc.f * imsc.gimae.hotspot.getX(1));
            hoty1 = (int) (imsc.ih * imsc.f * imsc.gimae.hotspot.getY(1));
            hotx2 = (int) (imsc.iw * imsc.f * imsc.gimae.hotspot.getX(2));
            hoty2 = (int) (imsc.ih * imsc.f * imsc.gimae.hotspot.getY(2));

            drawHS(0, g2, hotx, hoty);
            drawHS(1, g2, hotx1, hoty1);
            drawHS(2, g2, hotx2, hoty2);
            drawL(g2, hotx1, hoty1, hotx2, hoty2);
        }
    }

    private void drawHS(int ix, Graphics2D g2, int hx, int hy) {
        drawHS(ix, g2, hx + 1, hy + (ix == 2 ? 0 : 1), Color.black);
        drawHS(ix, g2, hx, hy, hotspot_click == ix ? Color.red :
                hotspot_near == ix ? Color.yellow : Color.green);
    }

    private void drawHS(int ix, Graphics2D g2, int hx, int hy, Color col) {
        g2.setColor(col);
        switch (ix) {
            case 0: // rot
                g2.drawArc(hx - 5, hy - 5, 10, 10, 0, 360);
                break;
            case 1: // from
                g2.drawLine(hx - 10, hy, hx + 10, hy);
                g2.drawLine(hx, hy - 10, hx, hy + 10);
                break;
            case 2:  // to
                g2.drawLine(hx - 6, hy - 6, hx + 6, hy + 6);
                g2.drawLine(hx - 6, hy + 6, hx + 6, hy - 6);
                break;
        }
    }

    private void drawL(Graphics2D g2,
                       int hx, int hy,
                       int hx2, int hy2) {
        g2.drawLine(hx, hy, hx2, hy2);
    }

    private void buildProperties() {
        Container con = getContentPane();

        con.setLayout(new BorderLayout());

        JPanel impan = new JPanel() {
            public void paintComponent(Graphics g) {
                g.setColor(Color.white);
                g.fillRect(0, 0, 1000, 1000);
                drawImage(g);
            }
        };
        impan.setPreferredSize(new Dimension(IM_W, IM_H));
        con.add(impan, BorderLayout.NORTH);

        im_pan = impan;

        JPanel pan = new JPanel();
        pan.setLayout(new GridBagLayout());

        JButton jb;
        int Y = 0;

        pan.add(new JLabel(T.t("Actor ID")), gbcf.createL(0, Y, 1));
        pan.add(lesson_id = new JTextField("            ", 20), gbcf.create(1, Y));
        if (true) {
            Document doc2 = lesson_id.getDocument();
            doc2.addDocumentListener(mydocl);
        }
        Y++;

        pan.add(new JLabel(T.t("Variables")), gbcf.createL(0, Y, 1));
        pan.add(var1 = new JTextField("            ", 15), gbcf.create(1, Y));
        pan.add(var2 = new JTextField("            ", 15), gbcf.create(2, Y));
        pan.add(var3 = new JTextField("            ", 15), gbcf.create(3, Y));
        if (true) {
            Document doc2 = var1.getDocument();
            doc2.addDocumentListener(mydocl);
            doc2 = var2.getDocument();
            doc2.addDocumentListener(mydocl);
            doc2 = var3.getDocument();
            doc2.addDocumentListener(mydocl);
        }
        Y++;

        pan.add(new JLabel(T.t("Image name")), gbcf.createL(0, Y, 1));
        pan.add(image_name = new JTextField("            ", 20), gbcf.create(1, Y));
        pan.add(jb = new JButton(T.t("Set")), gbcf.create(2, Y));
        //image_name.setEditable(false);
        jb.setActionCommand("setImName");
        jb.addActionListener(this);

        pan.add(jb = new JButton(T.t("Delete")), gbcf.createR(3, Y));
        jb.setForeground(new Color(140, 0, 0));
        jb.setActionCommand("delete");
        jb.addActionListener(this);
        delete = jb;
        Y++;


        pan.add(new JLabel(T.t("PeTask Nid")), gbcf.create(0, Y));
        pan.add(image_petasknid = new JTextField("", 2), gbcf.create(1, Y));
        if (true) {
            Document doc2 = image_petasknid.getDocument();
            doc2.addDocumentListener(mydocl);
        }
        Y++;


        pan.add(new JLabel(T.t("Primary scale")), gbcf.createL(0, Y, 1));
        pan.add(prim_scale = new JTextField(20), gbcf.create(1, Y));
        if (true) {
            Document doc2 = prim_scale.getDocument();
            doc2.addDocumentListener(mydocl);
        }
        Y++;

        pan.add(new JLabel(T.t("Primary mirror")), gbcf.createL(0, Y, 1));
        pan.add(prim_mirror = new JComboBox(), gbcf.create(1, Y));
        prim_mirror.addItem(T.t("no mirror"));
        prim_mirror.addItem(T.t("mirror X"));
        prim_mirror.addItem(T.t("mirror Y"));
        prim_mirror.addItem(T.t("mirror X and Y"));
        prim_mirror.setSelectedIndex(0);
        prim_mirror.addItemListener(myiteml);
        Y++;

        pan.add(new JLabel(T.t("Rotation spot")), gbcf.createL(0, Y, 1));
        pan.add(hotspot = new JTextField(20), gbcf.create(1, Y));
        Y++;

        pan.add(new JLabel(T.t("Path spot")), gbcf.createL(0, Y, 1));
        pan.add(rhotspot = new JTextField(20), gbcf.create(1, Y));
        Y++;

        hotspot.setEditable(false);
        rhotspot.setEditable(false);

        con.add(pan, BorderLayout.CENTER);

        JPanel pan2 = new JPanel();
        pan2.setLayout(new FlowLayout());

        jb = new JButton(T.t("Close"));
        jb.setActionCommand("Close");
        jb.addActionListener(this);
        pan2.add(jb);

        con.add(pan2, BorderLayout.SOUTH);
    }

    void updDoc(Document doc) {
        setDirty();
        if (doc == prim_scale.getDocument()) {
            try {
                double d = SundryUtils.tD(prim_scale.getText());
                if (d == 0)
                    prim_scale.setForeground(Color.red);
                else {
                    if (bound_act != null)
                        bound_act.gimae.setPrimScale(d);
                    prim_scale.setForeground(Color.black);
                }
            } catch (Exception ex) {
                prim_scale.setForeground(Color.red);
            }
            repaint();
        }
        if (doc == lesson_id.getDocument()) {
            String lid = lesson_id.getText();
            if (bound_act != null)
                bound_act.gimae.setLessonId(lid);
        }
        if (doc == var1.getDocument()) {
            String ss = var1.getText();
            if (bound_act != null)
                bound_act.gimae.setVariable(1, ss);
        }
        if (doc == var2.getDocument()) {
            String ss = var2.getText();
            if (bound_act != null)
                bound_act.gimae.setVariable(2, ss);
        }
        if (doc == var3.getDocument()) {
            String ss = var3.getText();
            if (bound_act != null)
                bound_act.gimae.setVariable(3, ss);
        }
        if (doc == image_petasknid.getDocument()) {
            String petnid = image_petasknid.getText();
            if (bound_act != null)
                bound_act.gimae.xim.setPeTaskNid(petnid);
        }
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("setImName")) {
            cabp.replaceActor(bound_act_ixx);
            repaint();
            return;
        }
        if (ev.getActionCommand().equals("delete")) {
            int rsp = JOptionPane.showConfirmDialog(this,
                    T.t("Are you sure to delete the Actor?"),
                    "Omega",
                    JOptionPane.YES_NO_OPTION);
//log	    OmegaContext.sout_log.getLogger().info("ERR: " + "*******) " + rsp);
            if (rsp == 0)
                cabp.deleteActor(bound_act_ixx);
            repaint();
            return;
        }
        if (ev.getActionCommand().equals("Close")) {
            setVisible(false);
            return;
        }
    }

//      public void valueChanged(ListSelectionEvent ev) {
//  	JList jl = (JList)ev.getSource();
//  	int ix = jl.getSelectedIndex();
//  	hotspot_selected_ix = ix;
//  	repaint();
//      }

    void enableDelete(boolean b) {
        delete.setEnabled(b);
    }
}
