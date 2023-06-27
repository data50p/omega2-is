package com.femtioprocent.omega.anim.tool.path;

import java.awt.geom.Point2D;

public class Probe {
    public Segment_Q seg;
    public Path pa;
    public Point2D p;
    public int sel;
    public double dist;

    public Probe() {
        dist = 999999999.99;
    }

    public String toString() {
        return "Probe" +
                "{seg=" + seg +
                ", p=" + p +
                ", pa=" + pa +
                ", sel=" + sel +
                ", dist=" + dist +
                "}";
    }
}
