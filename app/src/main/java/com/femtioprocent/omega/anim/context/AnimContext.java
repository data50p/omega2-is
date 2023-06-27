package com.femtioprocent.omega.anim.context;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.appl.AnimEditor;
import com.femtioprocent.omega.anim.appl.AnimRuntime;
import com.femtioprocent.omega.anim.canvas.AnimCanvas;
import com.femtioprocent.omega.anim.tool.timeline.MasterTimeLine;
import com.femtioprocent.omega.anim.tool.timeline.TimeLinePlayer;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;

//import omega.anim.config.*;

public class AnimContext extends OmegaContext {
    public AnimCanvas anim_canvas;
    public TimeLinePlayer tl_player;
    public MasterTimeLine mtl;
    static public AnimEditor ae;
    public AnimRuntime arun;
    public static JFrame top_frame;

    public double anim_speed = 1.0;

    public AnimContext(AnimEditor ae) {
        this.ae = ae;
        this.arun = null;
    }

    public AnimContext(AnimRuntime arun) {
        this.ae = null;
        this.arun = arun;
    }

    public void fillElement(Element el) {
        anim_canvas.fillElement(el);
        mtl.fillElement(el);
    }

//      public void save(XML_PW xmlpw) {
//  	anim_canvas.save(xmlpw);
//  	mtl.save(xmlpw);
//      }
}
