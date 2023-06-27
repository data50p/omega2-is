package com.femtioprocent.omega.lesson;

import com.femtioprocent.omega.media.video.MpgPlayer;

import javax.swing.*;

//import omega.lesson.test.*;

abstract class FeedBack {
    MpgPlayer mp;
    JPanel canvas, my_own;
    JComponent comp;

    int w, h;
    int vw, vh;

    FeedBack() {
    }


    int getW() {
        return vw;
    }

    int getH() {
        return vh;
    }

    abstract JPanel prepare(String rsrs, JPanel jpan);

    abstract void perform();

    abstract void waitEnd();

    abstract void dispose();
}
