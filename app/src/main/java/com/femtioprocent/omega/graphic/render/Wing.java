package com.femtioprocent.omega.graphic.render;

import com.femtioprocent.omega.graphic.util.LoadImage;
import com.femtioprocent.omega.xml.Element;

import java.awt.*;

public class Wing {
    public Image im;
    public int layer;
    public Dimension dim;
    public Point pos;
    public String name;
    public int ord;
    public int mirror;
    public double scale = 1.0;
    public int width, height;

//    public int mirror;

    public Wing(Component comp, String fn, int x, int y, int layer, int ord) {
        this.name = fn;
        im = LoadImage.loadAndWaitOrNull(comp, fn, false);
        this.layer = layer;
        dim = new Dimension(im.getWidth(null), im.getHeight(null));
        pos = new Point(x, y);
        this.ord = ord;
        width = im.getWidth(null);
        height = im.getHeight(null);
    }

    public Element getElement() {
        Element el = new Element("Wing");
        el.addAttr("name", name);
        el.addAttr("layer", "" + layer);
        el.addAttr("mirror", "" + mirror);
        el.addAttr("scale", "" + scale);
        el.addAttr("position", "" + pos.getX() + ' ' + pos.getY());
        return el;
    }
}
