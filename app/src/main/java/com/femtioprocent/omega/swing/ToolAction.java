package com.femtioprocent.omega.swing;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToolAction extends AbstractAction {
    String cmd;
    ToolExecute texec;

    public ToolAction(String text, String cmd, ToolExecute texec) {
        super(text);
        this.cmd = cmd;
        this.texec = texec;
    }

    public ToolAction(String text, String icons, String cmd, ToolExecute texec) {
        super(text, OmegaSwingUtils.getImageIcon("toolbarButtonGraphics/" + icons + "24.gif"));
        this.cmd = cmd;
        this.texec = texec;
    }

    public ToolAction(String text, String icons, String cmd, ToolExecute texec, boolean _b) {
        super(text, OmegaSwingUtils.getImageIcon("toolbarButtonGraphics/" + icons + "24.png"));
        this.cmd = cmd;
        this.texec = texec;
    }

    public ToolAction() {
        this.cmd = null;
    }

    public void actionPerformed(ActionEvent ae) {
        if (texec != null)
            texec.execute(getCommand());
    }

    public String getCommand() {
        return cmd;
    }

    public Icon getIcon() {
        return (Icon) getValue(Action.SMALL_ICON);
    }
}
