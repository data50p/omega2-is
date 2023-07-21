package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.util.Num;
import com.femtioprocent.omega.xml.Element;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class TimeMarker implements Serializable {
    public static final char BEGIN = '{';
    public static final char END = '}';
    public static final char START = '[';
    public static final char STOP = ']';
    public static final char TSYNC = '^';
    public static final char TRIGGER = 't';
    public static final char TIMELINE = 'T';

    public static final String[] typeString = {
            "{Begin",
            "}End",
            "[Start",
            "]Stop",
            "^TimeSync",
            "tTrigger",
            "TTimeLine"
    };

    public TimeLine tl;
    public char type;
    public int when;
    public int duration;
    public TimeLine tltl;
    public boolean selected;
    public TriggerEvent[] t_event;
    public boolean delete_candidate = false;

    public int ord;

    TimeMarker(TimeLine tl, char type, int when) {
        this.tl = tl;
        this.type = type;
        this.when = when;
        init();
    }

    TimeMarker(TimeLine tl, char type, int when, int duration) {
        this.tl = tl;
        this.type = type;
        this.when = when;
        this.duration = duration;
        init();
    }

    TimeMarker(TimeLine tl, TimeLine tltl, int when) {
        this.tl = tl;
        this.type = TIMELINE;
        this.when = when;
        this.tltl = tltl;
        init();
    }

    private void init() {
        if (type == TRIGGER || type == START || type == STOP || type == BEGIN || type == END) {
            t_event = new TriggerEvent[TriggerEventFactory.getSize()];
            String[] sa = TriggerEventFactory.getAllAsStringA();
//	    OmegaContext.sout_log.getLogger().info(":--: " + "Trigger " + SundryUtils.arrToString(sa));
            for (int i = 0; i < sa.length; i++) {
                t_event[i] = TriggerEventFactory.get(i);
            }
        }
    }

    public boolean isMoveAble() {
        return !(type == BEGIN || type == END);
    }

    public void move(int d) {
        move(d, 1);
    }

    public void move(int d, int grid) {
        when += d;
        when = Num.grid(when, grid);
        if (type == TIMELINE)
            tltl.move(d, grid);
        tl.reNumerateMarker();
    }

    public void setSelected(boolean b) {
        selected = b;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isDeleteCandidate() {
        return delete_candidate;
    }

    public void setDeleteCandidate(boolean b) {
        delete_candidate = b;
    }

    public boolean canRemove() {
        return type == TSYNC || type == TRIGGER;
    }

    public boolean relativeAdjust() {
        return type == TSYNC || type == TRIGGER;
    }

    public String typeString(char type) {
        for (int i = 0; i < typeString.length; i++)
            if (typeString[i].charAt(0) == type)
                return typeString[i].substring(1);
        return "--noname--";
    }

    void setTriggerEvent(TriggerEvent te, int ix) {
        try {
            t_event[ix] = te;
        } catch (Exception ex) {
        }
    }

    public void doAllAction(TriggerEventAction tea, boolean dry) {
        if (t_event != null)
            for (int i = 0; i < t_event.length; i++)
                if (t_event[i] != null)
                    if (t_event[i].is_on)
                        if (t_event[i].getCmd().length() > 0)
                            tea.doAction(t_event[i], this, dry);
    }

    static private Comparator comparator;

    public static Comparator getComparator() {
        if (comparator == null)
            comparator = new Comparator() {
                public int compare(Object a, Object b) {
                    TimeMarker ta = (TimeMarker) a;
                    TimeMarker tb = (TimeMarker) b;
                    int d = ta.when - tb.when;
                    return d < 0 ? -1 : d > 0 ? 1 : 0;
                }
            };
        return comparator;
    }

    public String toString() {
        return "TimeMarker{" +
                "tl=" + tl +
                ", type=" + (type == TIMELINE ? tltl.toString() : new String("" + (char) type)) +
                ", when=" + when +
                ", duration=" + duration +
                "}";
    }

    Element getElement() {
        Element el = new Element("TimeMarker");
        el.addAttr("type", "" + type);
        el.addAttr("when", "" + (when - tl.offset));
        el.addAttr("duration", "" + duration);
        if (t_event != null) {
            Element tel = new Element("T_Event");
            for (int i = 0; i < t_event.length; i++) {
                if (t_event[i] != null) {
                    Element teel = t_event[i].getElement();
                    tel.add(teel);
                }
            }
            el.add(tel);
        }
        return el;
    }

    public void fetchPlaySound(List<String> li) {
        if (t_event != null)
            for (int i = 0; i < t_event.length; i++) {
                if (t_event[i] != null) {
                    if ("PlaySound".equals(t_event[i].getCmd())) {
                        if (t_event[i].is_on) {
                            String arg = t_event[i].getArgString();
                            if (arg != null && arg.length() > 0)
                                li.add(arg);
                        }
                    }
                }
            }
    }

    public TriggerEvent findTEvent(String id) {
        if (t_event != null)
            for (int i = 0; i < t_event.length; i++) {
                if (t_event[i] != null) {
                    if (id.equals(t_event[i].getCmd())) {
                        return t_event[i];
                    }
                }
            }
        return null;
    }
}

