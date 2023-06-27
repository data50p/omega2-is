package com.femtioprocent.omega.lesson.canvas.result;

import com.femtioprocent.omega.adm.register.data.CreateEntry;

import javax.swing.*;
import java.awt.*;

public class CreateRegisterPanel extends RegisterPanel {
    JLabel type, lduration;
    JTextField sentence, duration;

    CreateRegisterPanel() {
        setBackground(new Color(240, 200, 240));
    }

    void populate() {
        JPanel pan = this;

        type = new JLabel("");
        lduration = new JLabel("Time: ");
        duration = new JTextField("0.0");
        sentence = new JTextField(30);

        int X = 0;
        int Y = 0;

        pan.add(new JLabel("Test:"), gbcf.createL(X, Y, 1));
        pan.add(type, gbcf.createL(++X, Y, 1));

        Y++;
        X = 0;
        pan.add(new JLabel("Sentence:"), gbcf.createL(X, Y, 1));
        pan.add(sentence, gbcf.createL(++X, Y, 1));
        pan.add(lduration, gbcf.createL(++X, Y, 1));
        pan.add(duration, gbcf.createL(++X, Y, 1));
    }

    public void set(CreateEntry te) {
        sentence.setText(te.sentence);
        duration.setText("" + te.duration / 1000.0);
    }
}
