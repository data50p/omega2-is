package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;

public class TriggerEventRotate extends TriggerEvent {
    public String getCmd() {
        return "Rotate";
    }

    public String getCmdLabel() {
        return T.t("Rotate object");
    }

    public String getHelp() {
        return T.t("<angle change / sec> {end angle=<none>}");
    }

    public TriggerEventRotate() {
        super("0.0");
    }

    public TriggerEventRotate(String arg) {
        super(arg);
    }

    public double getArgDouble() {
        String s = getArgString();
        String sa[] = SundryUtils.split(s, " ");
        if (sa.length == 1) {
            double d = SundryUtils.tD(s);
            return d;
        }
        double d = SundryUtils.tD(sa[0]);
        return d;
    }

    public double getArgDouble2nd() {
        String s = getArgString();
        String sa[] = SundryUtils.split(s, " ");
        if (sa.length == 1) {
            return 20000.00;
        }
        double d = SundryUtils.tD(sa[1]);
        return d;
    }
}
