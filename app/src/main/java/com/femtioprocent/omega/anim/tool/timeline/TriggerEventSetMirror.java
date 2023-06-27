package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.t9n.T;

public class TriggerEventSetMirror extends TriggerEventSelections {
    static String[] st_selections_cmd = new String[]{
            "",
            "X",
            "Y",
            "X and Y"
    };
    public static String[] st_selections_human = new String[]{
            T.t("none"),
            T.t("X only"),
            T.t("Y only "),
            T.t("both X and Y")
    };

    public TriggerEventSetMirror() {
        super("");
    }

    public TriggerEventSetMirror(String arg) {
        super(arg);
    }

    public String getCmd() {
        return "SetMirror";
    }

    public String getCmdLabel() {
        return T.t("image Mirror");
    }

    public String getHelp() {
        return T.t("Transform image");
    }

    public String[] getSelections_cmd() {
        return st_selections_cmd;
    }

    public String[] getSelections_human() {
        return st_selections_human;
    }

    public int getArgInt() {
        String s = getArgString();
        return getIx(s);
    }

    static int getIx(String s) {
        for (int i = 0; i < st_selections_cmd.length; i++)
            if (st_selections_cmd[i].equals(s))
                return i;
        return 2;
    }
}
