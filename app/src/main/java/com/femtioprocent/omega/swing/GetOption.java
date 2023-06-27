package com.femtioprocent.omega.swing;

import javax.swing.*;

public class GetOption {
    public static int getOption(String msg, String[] options) {
        int sel = JOptionPane.showOptionDialog(null,
                msg,
                "Omega - Option",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        return sel;
    }

    public static void showMsg(String msg) {
        JOptionPane.showMessageDialog(null,
                msg,
                "Omega - Message",
                JOptionPane.WARNING_MESSAGE);
    }
}
