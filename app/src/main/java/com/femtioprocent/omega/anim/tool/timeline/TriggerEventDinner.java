package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.t9n.T;

public class TriggerEventDinner extends TriggerEventSelections {
    static String[] st_selections_cmd = new String[]{
            "",
            "eat",
            "eaten"
    };
    static String[] st_selections_human = new String[]{
            T.t("none"),
            T.t("Can eat"),
            T.t("Can bee eaten")
    };

    public TriggerEventDinner() {
        super("");
    }

    public TriggerEventDinner(String arg) {
        super(arg);
    }

    public String getCmd() {
        return "Dinner";
    }

    public String getCmdLabel() {
        return T.t("image eat/eaten");
    }

    public String getHelp() {
        return T.t("Eat images");
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
