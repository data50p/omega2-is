package com.femtioprocent.omega.anim.tool.timeline;

public class TriggerEventOption extends TriggerEventSelections {
    static String[] st_selections_cmd = new String[]{
            ""
    };
    static String[] st_selections_human = new String[]{
            "normal"
    };

    public TriggerEventOption() {
        super("");
    }

    public TriggerEventOption(String arg) {
        super(arg);
    }

    public String getCmd() {
        return "Option";
    }

    public String getCmdLabel() {
        return "option";
    }

    public String getHelp() {
        return "Options";
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
