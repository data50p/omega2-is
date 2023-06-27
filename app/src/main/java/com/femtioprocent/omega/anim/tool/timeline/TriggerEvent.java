package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.xml.Element;

public class TriggerEvent {
    public String name;
    public String arg;
    public String arg_human;

    public boolean is_on = false;

    public TriggerEvent(String arg) {
        this.arg = arg;
        this.arg_human = arg;
    }

    public String getCmd() {
        return "";
    }

    public String getCmdLabel() {
        return "";
    }

    public TriggerEvent() {
        this(null);
    }

    public String getHelp() {
        return "";
    }

    public String[] getSelections_cmd() {
        return null;
    }

    public String[] getSelections_human() {
        return null;
    }

    public String getArgString() {
        if (arg == null)
            return "";
        return arg;
    }

    public String getArgString_human() {
        if (arg_human == null)
            return "";
        return arg_human;
    }

    public void setArg(String arg) {
        this.arg = arg;
        this.arg_human = arg;
    }

//      public void setArg_human(String arg) {
//  	this.arg_human = arg;
//  	this.arg = arg;
//      }

    public void setOn(boolean is_on) {
        this.is_on = is_on;
    }

    public String toString() {
        return getCmd() + ' ' + arg;
    }

    public boolean hasSelections() {
        return false;
    }

    public Element getElement() {
        Element el = new Element("TriggerEvent");
        el.addAttr("cmd", getCmd());
        el.addAttr("arg", arg);
        el.addAttr("isOn", is_on ? "true" : "false");
        return el;
    }

    public void doAction() {
    }

    public String[] getFiles() {
        return null;
    }
}
