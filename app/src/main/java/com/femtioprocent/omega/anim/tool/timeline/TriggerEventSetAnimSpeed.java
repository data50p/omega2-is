package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;

public class TriggerEventSetAnimSpeed extends TriggerEvent {
    public String getCmd() {
        return "SetAnimSpeed";
    }

    public String getCmdLabel() {
        return T.t("Anim speed");
    }

    public String getHelp() {
        return T.t("<sec / frame>");
    }

    public TriggerEventSetAnimSpeed() {
        super("0.2");
    }

    public TriggerEventSetAnimSpeed(String arg) {
        super(arg);
    }

    public double getArgDouble() {
        String s = getArgString();
        double d = SundryUtils.tD(s.trim());
        return d;
    }
}
