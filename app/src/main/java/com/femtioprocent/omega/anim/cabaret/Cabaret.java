package com.femtioprocent.omega.anim.cabaret;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.xml.Element;

import java.util.ArrayList;

public class Cabaret {
    final int ACT_N = OmegaConfig.CABARET_ACTOR_N;
    final int EMPTY = -1;

    AnimContext a_ctxt;
    public ActA actA = new ActA();

    public class ActA {
        public class Act {
            public Actor ac;
            public int tl_nid;
            int ord;

            Act(int ord) {
                this.ord = ord;
                tl_nid = EMPTY;
            }
        }

        ;

        public Act[] arr;

        ActA() {
            arr = new Act[ACT_N];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new Act(i);
            }
        }

        public GImAE getGImAE(int ix) {
            try {
                return arr[ix].ac.gimae;
            } catch (NullPointerException ex) {
                return null;
            }
        }

        public int getTLnid(int ix) {
            try {
                return arr[ix].tl_nid;
            } catch (NullPointerException ex) {
                return -1;
            }
        }

        public int findFree() {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].tl_nid == EMPTY)
                    return i;
            }
            return -1;
        }

        public int findOrdTL(int tl_nid) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].tl_nid == tl_nid)
                    return i;
            }
            return -1;
        }
    }

    ;

    public Cabaret(AnimContext a) {
        a_ctxt = a;
    }

    public void newActA() {
        actA = new ActA();
    }

    public void setActor(int ix, Actor act) {
        actA.arr[ix].ac = act;
    }

    public Actor getActor(int ix) {
        return getAct(ix).ac;
    }

    public ActA.Act getAct(int ix) {
        return actA.arr[ix];
    }

    public int getTLnid(int ix) {
        return actA.getTLnid(ix);
    }

    public int actorNum() {
        return actA.arr.length;
    }

    public Actor createActor(final int ix, String fn, double hotspot[]) {
        GImAE gim = new GImAE(a_ctxt.anim_canvas, fn, ix);

        Actor act = new Actor(a_ctxt, gim);
        if (hotspot == null)
            act.gimae.setHotSpot(0.5, 0.5);
        else
            act.gimae.setHotSpot(hotspot[0], hotspot[1]);

        setActor(ix, act);
        return act;
    }

    public String[] getLessonId() {
        ArrayList li = new ArrayList();

        for (int i = 0; i < actA.arr.length; i++) {
            Actor act = actA.arr[i].ac;
            if (act != null) {
                String lid = act.gimae.getLessonId();
                if (lid != null && !lid.startsWith("#") && lid.length() > 0)
                    li.add(lid);
            }
        }

        return (String[]) li.toArray(new String[0]);
    }

    private String reduce(String s, int v) {
        if (v == 0)
            return s;
        int ix = s.lastIndexOf(':');
        if (ix == -1)
            return s;
        return reduce(s.substring(0, ix), --v);
    }

    private boolean match(String s1, String s2) {
        if (s1.equals(s2))
            return true;
        return false;
    }

    public GImAE findActorByLessonId(String s1) {  // reduce x:y:z to x:y while not match
        for (int r_val = 0; r_val < 5; r_val++) {
            for (int i = 0; i < actA.arr.length; i++) {
                ActA.Act act = actA.arr[i];
                if (act != null && act.ac != null) {
                    String s2 = act.ac.gimae.getLessonId();
                    String ss2 = reduce(s2, r_val);
                    if (match(s1, ss2)) {
                        return act.ac.gimae;
                    }
                }
            }
        }

        int ix;
        if ((ix = s1.lastIndexOf(':')) != -1) {
            s1 = s1.substring(0, ix);
            return findActorByLessonId(s1);
        }
        return null;
    }

    public Element getElement() {
        Element el = new Element("Cabaret");
        el.addAttr("a", "b");
        return el;
    }
}
