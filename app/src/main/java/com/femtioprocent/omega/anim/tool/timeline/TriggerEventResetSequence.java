package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.t9n.T;

public class TriggerEventResetSequence extends TriggerEvent {
    public TriggerEventResetSequence() {
        super("");
    }

    public TriggerEventResetSequence(String arg) {
        super(arg);
    }

    public String getCmd() {
        return "ResetSequence";
    }

    public String getCmdLabel() {
        return T.t("Reset Sequence");
    }

    public String getHelp() {
        return T.t("relative {, [, or empty for here");
    }
}
