package com.femtioprocent.omega.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HtmlFrame extends JFrame {
    public HtmlPanel htmlp;

    public HtmlFrame(String url_s) {
        super("Omega - " + base(url_s));
        init(url_s);
    }

    private void init(String url_s) {
        if (htmlp != null)
            return;
        htmlp = new HtmlPanel(url_s);
        Container co = getContentPane();
        co.setLayout(new BorderLayout());
        co.add(htmlp, BorderLayout.CENTER);
        JPanel jp = new JPanel();
        JButton jb;
        jp.add(jb = new JButton("Close"));
        co.add(jp, BorderLayout.SOUTH);
        jb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });
        setSize(700, 500);
    }

    private static String base(String s) {
        int ix = s.lastIndexOf('/');
        int ix1 = s.lastIndexOf(':');
        if (ix1 != -1 || ix != -1)
            return s.substring((ix > ix1 ? ix : ix1) + 1);
        return s;
    }

    public void goTo(String s) {
        setTitle("Omega - " + base(s));
        htmlp.goTo(s);
    }

}
