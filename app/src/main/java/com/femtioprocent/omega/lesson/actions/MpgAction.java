package com.femtioprocent.omega.lesson.actions;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.canvas.MsgItem;
import com.femtioprocent.omega.media.video.MpgPlayer;
import com.femtioprocent.omega.swing.ScaledImageIcon;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;


public class MpgAction implements ActionI {
    public MpgPlayer mpg_player;
    MyPanel jpan;
    HashMap args = new HashMap();
    private Font item_fo = null;
    int parent_w = 100;
    int parent_h = 100;
    public String sentence;
    public boolean show_sentence = true;

    private boolean again_play = false;
    private boolean again_play2 = false;
    private boolean again_audio = false;
    private boolean again_audio2 = false;

    MsgDialog msg_dlg = new MsgDialog();

    class MyPanel extends JPanel {
        Mouse m;

        MyPanel() {
            m = new Mouse();
            addMouseListener(m);
            addMouseMotionListener(m);
        }

        class Mouse extends MouseInputAdapter {
            public void mousePressed(MouseEvent e) {
                hideMsg();
            }
        }

        public void paintComponent(Graphics g) {
            g.setColor(Lesson.omega_settings_dialog.action_movie_background.color);
            g.fillRect(0, 0, 2000, 2000);
            if (show_msg)
                msg_dlg.draw((Graphics2D) g);
            super.paintComponent(g);
        }
    }

    public boolean ownKeyCode(int kc, boolean is_shift) {
        if (kc == ' ' || kc == '\n' || kc == '\r') {
            hideMsg();
            return false;
        }
        OmegaContext.sout_log.getLogger().info("ERR: " + "own kk " + kc);
        if (kc == 'l') {
            again_play = true;
        }
        if (kc == 'u') {
            again_audio = true;
        }

        return true;
    }

    public MpgAction() {
        if (jpan == null)
            jpan = new MyPanel();
        jpan.setLayout(new BorderLayout());
    }

    public Element prefetch(String action_s) {
        return prefetch(action_s, 0, 0);
    }

    public Element prefetch(String action_s, int winW, int winH) {
        Element el = null;
        mpg_player = MpgPlayer.createMpgPlayer(action_s, jpan, winW, winH);
        return el;
    }

    public void setParentWH(int parent_w, int parent_h) {
        this.parent_w = parent_w;
        this.parent_h = parent_h;
    }

    public Element getElementRoot() {
        return null;
    }

    public void show() {
        jpan.repaint();
    }

    public String getPathList() {
        return null;
    }

    public String getActorList() {
        return null;
    }

    public JPanel getCanvas() {
        return jpan;
    }

    public void dispose() {
        if (mpg_player != null)
            mpg_player.dispose(jpan);
        OmegaContext.sout_log.getLogger().info("ERR: " + "mpg disposed");
        jpan.setVisible(false);
        mpg_player = null;
    }

    public void reset() {
        if (mpg_player != null)
            mpg_player.reset();
    }

    public int getW() {
        int w = -1;
        if (mpg_player != null)
            w = mpg_player.fxp.mediaW;
        Log.getLogger().info("Movie width: " + w);
        return w;
    }

    public int getH() {
        int h = -1;
        if (mpg_player != null)
            h = mpg_player.fxp.mediaH;
        Log.getLogger().info("Movie width: " + h);
        return h;
    }

    public int getPW() {
        return parent_w;
    }

    public int getPH() {
        return parent_h;
    }

    int gX(double f) {
        return (int) (f * getPW());
    }

    int gY(double f) {
        int ret = (int) (f * getPH());
        return ret;
    }

    public void setSize(int w, int h) {
        mpg_player.setSize(w, h);
    }

    public void setLocation(int x, int y) {
        mpg_player.setLocation(x, y);
    }

    public HashMap getHm() {
        return args;
    }

    boolean show_msg = false;

    int getStringWidth(Graphics2D g2, Font fo, String s) {
        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = fo.getStringBounds(s, frc);
        return (int) r.getWidth();
    }

    class MsgDialog {
        MsgItem msg_item;
        String cont_image_fn = OmegaContext.media() + "default/continue.png";

        void show(MsgItem msg) {
            set(msg);
        }

        void set(MsgItem msg) {
            if (msg == null) {
                show_msg = false;
            } else {
                show_msg = true;
            }

            msg_item = msg;
            jpan.repaint();
        }

        void draw(Graphics2D g2) {
            if (true)
                return;   // JavaFX
            String text = msg_item != null ? msg_item.text : "";

            if (item_fo == null) {
                int fH = gY(0.04);
                if (fH < 8)
                    fH = 8;
                item_fo = new Font("Arial", Font.PLAIN, fH);
            }

            int sw = getStringWidth(g2, item_fo, text);

            if (sw > gX(0.9)) {
                int fH = gY(0.025);
                item_fo = new Font("Arial", Font.PLAIN, fH);
                sw = getStringWidth(g2, item_fo, text);
            }

            int w = sw + 60;
            int h = gY(0.06);
            int th = gY(0.026);
            int x = gX(0.5) - w / 2;
            int y = gY(0.92);
            int r = gX(0.02);
            Color col = OmegaContext.COLOR_WARP;
            RoundRectangle2D fr = new RoundRectangle2D.Double(x, y, w, h, r, r);

            OmegaContext.sout_log.getLogger().info("ERR: " + "MPG draw: " + w + ' ' + sw + ' ' + text);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.setColor(col);
            g2.fill(fr);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            if (false) {
                // titlebar
                g2.setColor(new Color(88, 88, 88));
                g2.setClip(fr);
                g2.fill(new Rectangle2D.Double(x, y, w, th));
            }

            BasicStroke stroke = new BasicStroke(getPH() / 200f);
            g2.setStroke(stroke);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(new Color(15, 15, 15));
            g2.setClip(0, 0, 10000, 10000);
            g2.draw(fr);


            g2.setClip(0, 0, 10000, 10000);//	    g2.setClip(fr);
            g2.setColor(OmegaContext.COLOR_TEXT_WARP);

            g2.setFont(item_fo);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            OmegaContext.sout_log.getLogger().info("ERR: " + "MPG text: " + x + ' ' + y + ' ' + h + ' ' + item_fo + ' ' + text);
            g2.drawString(text,
                    x + 5,
                    y + 2 * h / 3);

            g2.setColor(col);
// 	    g2.setFont(item_fo);
// 	    g2.drawString(msg_item.title, x + 1 * w / 10, (int)(y + gY(0.03)));

            if (msg_item != null && msg_item.image != null) {
                int hh = (int) (h * 0.7);
                int ww = (4 * hh) / 3;
                try {
                    Image img = ScaledImageIcon.createImageIcon(jpan,
                            msg_item.image,
                            ww,
                            hh).getImage();
                    g2.drawImage(img, x, y + th + 2, null);
                } catch (Exception ex) {
                }
            }
            if (cont_image_fn != null) {
                int hh = (int) (h * 0.25);
                int ww = hh * 4;
                try {
                    Image img = ScaledImageIcon.createImageIcon(jpan,
                            cont_image_fn,
                            ww,
                            hh).getImage();
                    int imw = img.getWidth(null);
                    g2.drawImage(img, x + w - imw - 3, y + h - hh - 3, null);
                } catch (Exception ex) {
                }
            }

            Area a = new Area();
            a.add(new Area(new Rectangle2D.Double(0, 0, 10000, 10000)));
            a.subtract(new Area(fr));
            g2.setClip(a);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
            g2.setColor(new Color(15, 15, 15));

            for (int i = 0; i < 7; i++) {
                RoundRectangle2D frs = new RoundRectangle2D.Double(x + 10 - i, y + 10 - i, w, h, r, r);
                g2.fill(frs);
            }
        }
    }

    public void showMsg(MsgItem mi) {
        msg_dlg.show(mi);
    }

    public boolean isMsg() {
        return show_msg;
    }

    public void hideMsg() {
        msg_dlg.set(null);
    }

    public void perform(Window window,
                        String action_s,
                        String[] actA,
                        String[] pathA,
                        int ord,
                        Runnable hook) {
        if (mpg_player == null)
            mpg_player = MpgPlayer.createMpgPlayer(action_s, jpan, window.getWidth(), window.getHeight());
        else
            Log.getLogger().info("already created MpgPayer ... ");
        OmegaContext.sout_log.getLogger().info("ERR: " + "mpg created " + mpg_player.getOrigW() + ' ' + mpg_player.getOrigH());
        mpg_player.fxp.waitReady();
        again_play2 = true;
        again_audio2 = true;
        //mpg_player.setSize(mpg_player.getOrigW(), mpg_player.getOrigH());
        int ww = (getW() - mpg_player.getOrigW()) / 2;
        int hh = (getH() - mpg_player.getOrigH()) / 2;
//	mpg_player.setLocation(ww, hh);
//	mpg_player.setSize(200, 200);
//	mpg_player.visual.setVisible(true);

        mpg_player.start();
        mpg_player.wait4();
        OmegaContext.sout_log.getLogger().info("ERR: " + "mp_waited");
        if (ord == 0) {
//	    dispose();
            if (show_sentence) {
                showMsgFx(new MsgItem("", sentence));
                while (show_msg && mpg_player.fxp.messageShown) {
                    SundryUtils.m_sleep(200);
                    if (again_audio && again_audio2) {
                        hook.run();
                        again_audio2 = false;
                    }
                    if (again_play && again_play2) {
                        hideMsg();
                        mpg_player.reset();
                        mpg_player.start();
                        mpg_player.wait4();
                        again_play2 = false;
                        again_play = false;
                        showMsg(new MsgItem("", sentence));
                    }
                }
            }
        }
// may play twice	mpg_player.stop();
// 	mpg_player.dispose(jpan);
// 	mpg_player = null;
        OmegaContext.sout_log.getLogger().info("ERR: " + "mp_shown");
    }

    private void showMsgFx(MsgItem mi) {
        mpg_player.fxp.showMsg(mi);
        mpg_player.visual.repaint();
        mpg_player.fxp.messageShown = true;
        show_msg = true;
    }

    public void clearScreen() {
    }

    public void clean() {
    }
}
