package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;

public class TriggerEventScale extends TriggerEvent {
    public String getCmd() {
        return "Scale";
    }

    public String getCmdLabel() {
        return T.t("Scale object");
    }

    public String getHelp() {
        return T.t("<factor change / sec> <end factor=1.0>");
    }

    public TriggerEventScale() {
        super("0.0");
    }

    public TriggerEventScale(String arg) {
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

    public double getArgDouble2nd(double dsp) {
        String s = getArgString();
        String sa[] = SundryUtils.split(s, " ");
        if (sa.length == 1) {
            return dsp >= 0.0 ? 100.0 : 0.001;
        }
        double d = SundryUtils.tD(sa[1]);
        return d;
    }
}
