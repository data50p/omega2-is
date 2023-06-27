package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.t9n.T;

public class TriggerEventSetLayer extends TriggerEventSelections {
    public static String[] st_selections_cmd = new String[]{
            "Behind",
            "Back",
            "Middle",
            "Front",
            "Top"
    };
    public static String[] st_selections_human = new String[]{
            T.t("Behind all"),
            T.t("Back"),
            T.t("Middle"),
            T.t("Front"),
            T.t("On Top")
    };

    public TriggerEventSetLayer() {
        super("Middle");
    }

    public TriggerEventSetLayer(String arg) {
        super(arg);
    }

    public String getCmd() {
        return "SetLayer";
    }

    public String getCmdLabel() {
        return T.t("dep_set Layer");
    }

    public String getHelp() {
        return T.t("Z order");
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
        return getIx(st_selections_cmd, s, 2);
    }
}
