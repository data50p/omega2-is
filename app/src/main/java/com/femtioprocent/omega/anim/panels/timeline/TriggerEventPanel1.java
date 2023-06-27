package com.femtioprocent.omega.anim.panels.timeline;

import com.femtioprocent.omega.anim.tool.timeline.TriggerEvent;

import javax.swing.*;

public class TriggerEventPanel1 extends TriggerEventPanel {
    TriggerEventPanel1(TriggerEvent te) {
        label = new JTextField(te.getCmdLabel());
        label.setEditable(false);
        if (te.hasSelections())
            cell_edit = new JComboBox();
        else
            cell_edit = new JTextField();
        help = new JTextField(te.getHelp());
        cb = new JCheckBox();
    }

    void setEC(Object o) {
        ((JComboBox) cell_edit).setSelectedItem(o);
    }

    void setEC_TF(String s) {
        ((JTextField) cell_edit).setText(s);
    }


    void setArg(String s) {
        tf.setText(s);
    }
}
