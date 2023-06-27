package com.femtioprocent.omega.adm.login;

import com.femtioprocent.omega.OmegaContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginSimple extends Login {
    class Comp extends JComponent {
        JButton login;
        JButton cancel;
        final JTextField tf;

        Comp() {
            setLayout(new BorderLayout());
            JPanel p = new JPanel();
            JPanel p2 = new JPanel();
            p.setLayout(new BorderLayout());
            p2.setLayout(new FlowLayout());
            login = new JButton("Login");
            cancel = new JButton("As Guest");
            tf = new JTextField(20);

            JTextArea text = new JTextArea("Ange ditt namn\n" +
                    "Tryck sedan på 'Login'");
            text.setEditable(false);
            text.setBackground(cancel.getBackground());

            p2.add(login);
            p2.add(cancel);

            p.add(tf, BorderLayout.NORTH);
            p.add(p2, BorderLayout.SOUTH);

            add(text, BorderLayout.NORTH);
            add(p, BorderLayout.CENTER);

            login.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    synchronized (o) {
                        if (tf.getText().length() > 0) {
                            OmegaContext.sout_log.getLogger().info("ERR: " + "action " + tf.getText());
                            LoginSimple.this.setName(tf.getText());
                            ready = true;
                            o.notify();
                        }
                    }
                }
            });

            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    synchronized (o) {
                        tf.setText("");
                        setName(null);
                        ready = true;
                        o.notify();
                    }
                }
            });
        }
    }

    public LoginSimple() {
        comp = new Comp();
    }
}
