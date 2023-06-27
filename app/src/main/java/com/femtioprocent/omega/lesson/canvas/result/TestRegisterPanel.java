package com.femtioprocent.omega.lesson.canvas.result;

import com.femtioprocent.omega.adm.register.data.TestEntry;

import javax.swing.*;
import java.awt.*;

public class TestRegisterPanel extends RegisterPanel {
    JLabel type, lduration;
    JTextField answer, sentence, duration;

    TestRegisterPanel() {
        setBackground(new Color(240, 240, 200));
    }

    void populate() {
        JPanel pan = this;

        type = new JLabel("");
        lduration = new JLabel("Time: ");
        duration = new JTextField("0.0");
        answer = new JTextField(30);
        sentence = new JTextField(30);

        int X = 0;
        int Y = 0;

        pan.add(new JLabel("Test:"), gbcf.createL(X, Y, 1));
        pan.add(type, gbcf.createL(++X, Y, 1));

        Y++;
        X = 0;
        pan.add(new JLabel("Pupil answer:"), gbcf.createL(X, Y, 1));
        pan.add(answer, gbcf.createL(++X, Y, 1));

        Y++;
        X = 0;
        pan.add(new JLabel("Correct answer:"), gbcf.createL(X, Y, 1));
        pan.add(sentence, gbcf.createL(++X, Y, 1));
        pan.add(lduration, gbcf.createL(++X, Y, 1));
        pan.add(duration, gbcf.createL(++X, Y, 1));
    }

    public void set(TestEntry te) {
        answer.setText(te.answer);
        sentence.setText(te.sentence);
        duration.setText("" + te.duration / 1000.0);
    }
}
