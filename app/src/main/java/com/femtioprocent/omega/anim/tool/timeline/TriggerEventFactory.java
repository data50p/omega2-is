package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.xml.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class TriggerEventFactory {
    public static Class getTriggerEvent(int ix) {
        switch (ix) {
            case 0:
                return TriggerEventPlaySound.class;
            case 1:
                return TriggerEventRotate.class;
            case 2:
                return TriggerEventScale.class;
            case 3:
                return TriggerEventImageAttrib.class;
            case 4:
                return TriggerEventSetLayer.class;
            case 5:
                return TriggerEventSetVisibility.class;
            case 6:
                return TriggerEventSetAnimSpeed.class;
            case 7:
                return TriggerEventSetMirror.class;
            case 8:
                return TriggerEventDinner.class;
            case 9:
                return TriggerEventOption.class;
            case 10:
                return TriggerEventResetSequence.class;
        }
        return null;
    }

    public static int getSlot(String name) {
        if ("PlaySound".equals(name))
            return 0;
        if ("Rotate".equals(name))
            return 1;
        if ("Scale".equals(name))
            return 2;
        if ("ImageAttrib".equals(name))
            return 3;
        if ("SetLayer".equals(name))
            return 4;
        if ("SetVisibility".equals(name))
            return 5;
        if ("SetAnimSpeed".equals(name))
            return 6;
        if ("SetMirror".equals(name))
            return 7;
        if ("Dinner".equals(name))
            return 8;
        if ("Option".equals(name))
            return 9;
        if ("ResetSequence".equals(name))
            return 10;
        return -1;
    }

    public static int getSize() {
        return getAllAsStringA().length;
    }

    public static TriggerEvent createTriggerEvent(String name) {
        try {
            Class cl = Class.forName("com.femtioprocent.omega.anim.tool.timeline." + "TriggerEvent" + name);
            TriggerEvent te = (TriggerEvent) cl.getDeclaredConstructor().newInstance();
            te.name = name;
            return te;
        } catch (IllegalAccessException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
        } catch (InstantiationException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
        } catch (ClassNotFoundException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
        } catch (InvocationTargetException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static TriggerEvent createTriggerEvent(Element e) {
        String cmd = e.findAttr("cmd");
        String arg = e.findAttr("arg");
        String is_on = e.findAttr("isOn");
        TriggerEvent te = createTriggerEvent(cmd);
        if (te != null) {
            if (te.hasSelections()) {
                ((TriggerEventSelections) te).setArgFromCmd(arg);
            } else
                te.setArg(arg);
            te.setOn(is_on.equals("true"));
        }
        return te;
    }

    static public String[] getAllAsStringA() {
        List<String> l = new ArrayList();

        for (int i = 0; i < 100; i++) {
            Class cl = getTriggerEvent(i);
            if (cl == null)
                break;
            try {
                TriggerEvent te = (TriggerEvent) cl.getDeclaredConstructor().newInstance();
                String s = te.getCmdLabel();
                te.name = s;
                l.add(s);
            } catch (IllegalAccessException ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
            } catch (InstantiationException ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
            } catch (InvocationTargetException ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return l.toArray(new String[0]);
    }

    static public TriggerEvent get(int ix) {
        Class cl = getTriggerEvent(ix);
        if (cl != null)
            try {
                TriggerEvent te = (TriggerEvent) cl.newInstance();
                return te;
            } catch (IllegalAccessException ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
            } catch (InstantiationException ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "TriggerEventFactory: " + ex);
            }
        return null;
    }
}
