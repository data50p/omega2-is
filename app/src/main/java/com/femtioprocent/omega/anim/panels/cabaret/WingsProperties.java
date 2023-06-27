package com.femtioprocent.omega.anim.panels.cabaret;

import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventSetLayer;
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventSetMirror;
import com.femtioprocent.omega.graphic.render.Wing;
import com.femtioprocent.omega.swing.GBC_Factory;
import com.femtioprocent.omega.swing.properties.OmegaProperties;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;

public class WingsProperties extends OmegaProperties implements ActionListener {
    final static int IM_W = 300;
    final static int IM_H = 100;

    GBC_Factory gbcf = new GBC_Factory();

    Wing bound_wing = null;
    int bound_wing_ixx = -1;
    WingsPanel wings_pan;

    JTextField image_name;
    JComboBox layer;
    JComboBox mirror;
    JTextField scale;
    JTextField position;
    JButton delete;
    JButton set_pos;

    String selections[] = TriggerEventSetLayer.st_selections_cmd;
    String mirror_selections[] = TriggerEventSetMirror.st_selections_human;

    private boolean skipDirty = false;

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
            if (cb == layer) {
                if (bound_wing != null) {
                    int l = layer.getSelectedIndex();
                    if (l >= 0 && l <= 5) {
                        bound_wing.layer = l;
                        wings_pan.ae.a_ctxt.anim_canvas.updWings();
                        setDirty();
                    } else {
                    }
                }
            }
            if (cb == mirror) {
                if (bound_wing != null) {
                    int l = mirror.getSelectedIndex();
                    if (l >= 0 && l < 4) {
                        bound_wing.mirror = l;
                        wings_pan.ae.a_ctxt.anim_canvas.updWings();
                        wings_pan.ae.a_ctxt.anim_canvas.resetBackground();
                        setDirty();
                    } else {
                    }
                }
            }
        }
    }

    ;
    myItemListener myiteml = new myItemListener();

    javax.swing.Timer timer;

    Wing getBoundWing() {
        return bound_wing;
    }

    void updDoc(Document doc) {
        if (doc == scale.getDocument()) {
            if (timer == null) {
                timer = new javax.swing.Timer(1000,
                        new ActionListener() {
                            public void actionPerformed(ActionEvent ae) {
                                Wing fbound_wing = getBoundWing();
                                if (fbound_wing != null) {
                                    fbound_wing.scale = SundryUtils.tD(scale.getText());
                                    if (fbound_wing.scale == 0) {
                                        fbound_wing.scale = 1.0;
                                        scale.setForeground(Color.red);
                                    } else {
                                        scale.setForeground(Color.black);
                                    }
                                    wings_pan.ae.a_ctxt.anim_canvas.updWings();
                                    wings_pan.ae.a_ctxt.anim_canvas.resetBackground();
                                    timer.stop();
                                }
                            }
                        });
                timer.setCoalesce(true);
            }
            timer.start();
        }
        setDirty();
        repaint();
    }


    public WingsProperties(JFrame owner, WingsPanel wings_pan) {
        super(owner);
        skipDirty = true;
        this.wings_pan = wings_pan;
        setTitle(T.t("Wings") + " - " + T.t("Properties"));
        setSize(300, 200);
        buildProperties();
        pack();
        skipDirty = false;
    }


    private void setDirty() {
        if (skipDirty == false)
            AnimContext.ae.setDirty(true);
    }

    public void updPos(Wing w, int dx, int dy) {
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "updPOS " + w + ' ' + bound_wing);
        if (w == bound_wing)
            position.setText("" + (bound_wing.pos.getX() + dx) + ' ' + (bound_wing.pos.getY() + dy));
    }

    public void setTarget(Wing wing, int ixx) {
        skipDirty = true;
        bound_wing = wing;
        bound_wing_ixx = ixx;
        if (wing != null) {
            image_name.setText(wing.name);
            position.setText("" + wing.pos.getX() + ' ' + wing.pos.getY());
            layer.setSelectedItem(selections[wing.layer]);
            scale.setText("" + wing.scale);
            mirror.setSelectedItem(mirror_selections[wing.mirror]);
        } else {
            image_name.setText("");
            position.setText("");
            layer.setSelectedIndex(2);
            scale.setText("");
            mirror.setSelectedIndex(0);
        }
        pack();
        repaint();
        skipDirty = false;
    }

    void drawImage(Graphics g) {
        if (bound_wing != null) {
            Image im = bound_wing.im;
            AffineTransform at = new AffineTransform();
            double iw = im.getWidth(null);
            double ih = im.getHeight(null);
            double fx = IM_W / iw;
            double fy = IM_H / ih;
            double f = fx < fy ? fx : fy;
            at.scale(f, f);

            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.black);
            g2.fillRect(0, 0, (int) (iw * f), (int) (ih * f));

            ((Graphics2D) g).drawImage(im, at, null);
        }
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

        JPanel pan = new JPanel();
        pan.setLayout(new GridBagLayout());

        JButton jb;
        int Y = 0;

        pan.add(new JLabel(T.t("Image name")), gbcf.createL(0, Y, 1));
        pan.add(image_name = new JTextField("            ", 20), gbcf.create(1, Y));
        image_name.setEditable(false);
        pan.add(jb = new JButton(T.t("Add")), gbcf.create(2, Y));
        jb.setActionCommand("addImName");
        jb.addActionListener(this);

        pan.add(jb = new JButton(T.t("Delete")), gbcf.createR(3, Y));
        jb.setForeground(new Color(140, 0, 0));
        jb.setActionCommand("delete");
        jb.addActionListener(this);
        delete = jb;
        Y++;

        pan.add(new JLabel(T.t("Layer")), gbcf.createL(0, Y, 1));
        pan.add(layer = new JComboBox(selections), gbcf.create(1, Y));
        layer.setSelectedIndex(2);
        layer.setEditable(false);
        layer.addItemListener(myiteml);
        Y++;

        pan.add(new JLabel(T.t("Mirror")), gbcf.createL(0, Y, 1));
        pan.add(mirror = new JComboBox(mirror_selections), gbcf.create(1, Y));
        mirror.setSelectedIndex(0);
        mirror.setEditable(false);
        mirror.addItemListener(myiteml);
        Y++;

        pan.add(new JLabel(T.t("Scale")), gbcf.createL(0, Y, 1));
        pan.add(scale = new JTextField("1.0"), gbcf.create(1, Y));
        scale.setEditable(true);
        if (true) {
            Document doc = scale.getDocument();
            doc.addDocumentListener(mydocl);
        }
        Y++;

        pan.add(new JLabel(T.t("Position")), gbcf.createL(0, Y, 1));
        pan.add(position = new JTextField(20), gbcf.create(1, Y));

        pan.add(jb = new JButton(T.t("Set position")), gbcf.createR(2, Y));
        position.setEditable(false);
        jb.setActionCommand("setPos");
        jb.addActionListener(this);
        set_pos = jb;
        Y++;

        con.add(pan, BorderLayout.CENTER);

        JPanel pan2 = new JPanel();
        pan2.setLayout(new FlowLayout());

        jb = new JButton(T.t("Close"));
        jb.setActionCommand("Close");
        jb.addActionListener(this);
        pan2.add(jb);

        con.add(pan2, BorderLayout.SOUTH);
    }

    void cancelPos() {
        set_pos.setText(T.t("Set position"));
        set_pos.setActionCommand("setPos");
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("addImName")) {
            wings_pan.replaceWing(bound_wing_ixx);
            wings_pan.on(bound_wing_ixx);
            setDirty();
            return;
        }
        if (ev.getActionCommand().equals("setPos")) {
            wings_pan.on(bound_wing_ixx);
            setDirty();
            return;
        }
        if (ev.getActionCommand().equals("delete")) {
            int rsp = JOptionPane.showConfirmDialog(this,
                    T.t("Are you sure to delete the Wing?"),
                    "Omega",
                    JOptionPane.YES_NO_OPTION);
//log	    OmegaContext.sout_log.getLogger().info("ERR: " + "*******) " + rsp);
            if (rsp == 0) {
                wings_pan.removeWing(bound_wing_ixx);
                setDirty();
            }
            return;
        }
        if (ev.getActionCommand().equals("Close")) {
            setVisible(false);
            return;
        }
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    void enableDelete(boolean b) {
        delete.setEnabled(b);
    }
}
