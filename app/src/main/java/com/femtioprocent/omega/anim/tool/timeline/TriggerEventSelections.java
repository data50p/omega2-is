package com.femtioprocent.omega.anim.tool.timeline;

public class TriggerEventSelections extends TriggerEvent {
    public TriggerEventSelections() {
        this(null);
    }

    public TriggerEventSelections(String arg) {
        super(arg);
        setArgFromCmd(arg);
    }

    public boolean hasSelections() {
        return true;
    }

    public void setArgFromHuman(String arg) {
        for (int i = 0; i < getSelections_human().length; i++)
            if (getSelections_human()[i].equals(arg)) {
                this.arg_human = getSelections_human()[i];
                this.arg = getSelections_cmd()[i];
                return;
            }
        this.arg = arg;
        this.arg_human = arg;
    }

    public void setArgFromCmd(String arg) {
        for (int i = 0; i < getSelections_cmd().length; i++)
            if (getSelections_cmd()[i].equals(arg)) {
                this.arg_human = getSelections_human()[i];
                this.arg = getSelections_cmd()[i];
                return;
            }
        this.arg = arg;
        this.arg_human = arg;
    }

    static int getIx(String[] sa, String s, int def) {
        for (int i = 0; i < sa.length; i++)
            if (sa[i].equals(s))
                return i;
        return def;
    }
}
