package com.femtioprocent.omega.anim.canvas;

import com.femtioprocent.omega.OmegaContext;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Key extends KeyAdapter {
    AnimCanvas anim_canvas;

    Key(AnimCanvas anim_canvas) {
        this.anim_canvas = anim_canvas;
        anim_canvas.addKeyListener(this);
    }

    public void keyTyped(KeyEvent k) {
        OmegaContext.sout_log.getLogger().info("ERR: " + "key " + k);
    }
}

