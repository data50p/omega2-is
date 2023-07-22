package com.femtioprocent.omega.lesson.actions;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.appl.AnimRuntime;
import com.femtioprocent.omega.anim.canvas.AnimCanvas;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class AnimAction implements ActionI {
    public AnimRuntime rt = new AnimRuntime();
    public HashMap<String,Object> params = new HashMap();

    public Element prefetch(String action_s) {
        Element el = null;
        try {
            rt.prefetch(action_s);
            el = getElementRoot();
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "exception " + ex);
            ex.printStackTrace();
        }
        return el;
    }

    public Element getElementRoot() {
        Element el = rt.getElementRoot();
        return el;
    }

    public void show() {
//	rt.a_ctxt.anim_canvas.repaint();
    }

    public String getPathList() {
        Element el = getElementRoot();
        if (el != null) {
            Element mtl_el = el.findElement("MTL", 0);
            if (mtl_el != null) {
                String s = "";
                for (int i = 0; i < 50; i++) {
                    Element tel = mtl_el.findElement("TimeLine", i);
                    if (tel != null) {
                        String ss = tel.findAttr("lesson_id");
                        if (ss != null) {
                            if (s.length() == 0)
                                s = ss;
                            else
                                s += "," + ss;
                        }
                    }
                }
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FIND all tl " + s);
                return s;
            }
        }
        return null;
    }

    public void clearScreen() {
        AnimCanvas aca = rt.getAC();
        if (aca != null) {
            aca.setHidden(true);
        }
        if (rt != null)
            rt.clean();
    }

    public String getActorList() {
        Element el = getElementRoot();
        if (el != null) {
            Element allact = el.findElement("AllActors", 0);
            if (allact != null) {
                String s = "";
                for (int i = 0; i < 50; i++) {
                    Element ael = allact.findElement("Actor", i);
                    if (ael != null) {
                        String ss = ael.findAttr("lesson_id");
                        if (ss != null) {
                            if (s.length() == 0)
                                s = ss;
                            else
                                s += "," + ss;
                        }
                    }
                }
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FIND all act " + s);
                return s;
            }
        }
        return null;
    }

    public JPanel getCanvas() {
        return rt.getAC();
    }

    public HashMap<String,Object> getHm() {
        return params;
    }

    public void perform(Window window,
                        String action_s,
                        String[] actA,
                        String[] pathA,
                        int ord,
                        Runnable hook) {

        rt.getAC().setHidden(false);
        rt.runAction(window, action_s, actA, pathA, params, hook);
    }

    public void clean() {
        rt.clean();
    }
}
