package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.t9n.T;

public class TriggerEventSetVisibility extends TriggerEventSelections {
    static String[] st_selections_cmd = new String[]{
            "100",
            "0",
            "5",
            "10",
            "30",
            "50",
            "70",
            "90"
    };
    static String[] st_selections_human = new String[]{
            T.t("Visible"),
            T.t("Invisible"),
            T.t(" 5% visible"),
            T.t("10% visible"),
            T.t("30% visible"),
            T.t("50% visible"),
            T.t("70% visible"),
            T.t("90% visible")
    };
    static int tab[] = {
            100,
            0,
            5,
            10,
            30,
            50,
            70,
            90
    };

    public TriggerEventSetVisibility() {
        super("100");
    }

    public TriggerEventSetVisibility(String arg) {
        super(arg);
    }

    public String getCmd() {
        return "SetVisibility";
    }

    public String getCmdLabel() {
        return T.t("dep_set Visibility");
    }

    public String getHelp() {
        return "";
    }

    public String[] getSelections_cmd() {
        return st_selections_cmd;
    }

    public String[] getSelections_human() {
        return st_selections_human;
    }

    public int getArgInt() {
        String s = getArgString();
        int ix = getIx(st_selections_cmd, s, 0);
        return tab[ix];
    }

    static int getIx(String sa[], String s) {
        return getIx(sa, s, 0);
    }
}
