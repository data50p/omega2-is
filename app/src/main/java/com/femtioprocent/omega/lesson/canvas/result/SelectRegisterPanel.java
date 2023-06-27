package com.femtioprocent.omega.lesson.canvas.result;

import com.femtioprocent.omega.adm.register.data.SelectEntry;

import javax.swing.*;
import java.awt.*;

public class SelectRegisterPanel extends RegisterPanel {
    JLabel type, lwhen;
    JTextField extra, word, when;

    SelectRegisterPanel() {
        setBackground(new Color(200, 200, 240));
    }

    void populate() {
        JPanel pan = this;

        type = new JLabel("");
        lwhen = new JLabel("Time: ");
        when = new JTextField("0.0");
        extra = new JTextField(30);
        word = new JTextField(30);

        int X = 0;
        int Y = 0;

        pan.add(new JLabel("Test:"), gbcf.createL(X, Y, 1));
        pan.add(type, gbcf.createL(++X, Y, 1));

        Y++;
        X = 0;
        pan.add(new JLabel("Pupil answer:"), gbcf.createL(X, Y, 1));
        pan.add(extra, gbcf.createL(++X, Y, 1));

        Y++;
        X = 0;
        pan.add(new JLabel("Correct answer:"), gbcf.createL(X, Y, 1));
        pan.add(word, gbcf.createL(++X, Y, 1));
        pan.add(lwhen, gbcf.createL(++X, Y, 1));
        pan.add(when, gbcf.createL(++X, Y, 1));
    }

    public void set(SelectEntry se) {
        extra.setText(se.extra);
        word.setText(se.word);
        when.setText("" + se.when / 1000.0);
    }
}
