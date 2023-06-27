package com.femtioprocent.omega.appl;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.PreferenceUtil;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Settings {
    static HashMap flags;
    static List args;

    HashMap settings = new HashMap();

    static Settings default_settings;

    public static Settings getSettings() {
        if (default_settings == null) {
            default_settings = new Settings();
            OmegaContext.sout_log.getLogger().info("ERR: " + "Settings created");
        }

        return default_settings;
    }

    Settings() {
        OmegaContext.sout_log.getLogger().info("ERR: " + "" + flags);
        OmegaContext.sout_log.getLogger().info("ERR: " + "" + args);

        settings.put("audio-cache", false);
        settings.put("audio-jmf", false);
        settings.put("audio-bufsize", "16");
        settings.put("audio-write-ahead", "2");
        settings.put("audio-silent", false);
        settings.put("audio-debug", false);

        PreferenceUtil pu = new PreferenceUtil(Settings.class);
        HashMap hm = (HashMap) pu.getObject("settings", new HashMap());
        settings.putAll(hm);
    }

    void list() {
        OmegaContext.sout_log.getLogger().info("ERR: " + "list: " + settings);
    }

    class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            save();
            System.exit(0);
        }
    }

    JFrame f;
    JPanel pan;

    void save() {
        Component[] cA = pan.getComponents();
        for (int i = 0; i < cA.length; i++) {
            Component comp = cA[i];
            if (comp instanceof JTextField) {
                JTextField tf = (JTextField) comp;
                OmegaContext.sout_log.getLogger().info("ERR: " + "" + tf.getText());
            }
            if (comp instanceof JLabel) {
                JLabel la = (JLabel) comp;
                JTextField tf = (JTextField) la.getLabelFor();

                Object o = settings.get(la.getText());

                if (o instanceof Boolean) {
                    Boolean B = false;
                    String s = tf.getText();
                    if ("true".equalsIgnoreCase(s) ||
                            "t".equalsIgnoreCase(s) ||
                            "ja".equalsIgnoreCase(s) ||
                            "yes".equalsIgnoreCase(s))
                        B = true;
                    settings.put(la.getText(), B);
                } else {
                    settings.put(la.getText(), tf.getText());
                }
            }
        }
        PreferenceUtil pu = new PreferenceUtil(Settings.class);
        pu.save("settings", settings);
    }

    void gui() {
        f = new JFrame("Omega - Guru Settings");
        pan = new JPanel();
        pan.setLayout(new GridLayout(0, 2));
        Container c = f.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(pan, BorderLayout.CENTER);
        JButton exitB = new JButton("Save & Exit");
        exitB.addActionListener(new MyActionListener());
        c.add(exitB, BorderLayout.SOUTH);

        Iterator it = settings.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String val = settings.get(key).toString();

            JLabel la;
            JTextField tf;

            pan.add(la = new JLabel(key));
            pan.add(tf = new JTextField(val));

            la.setLabelFor(tf);

        }
        f.pack();
        f.setVisible(true);
    }

    public HashMap getSettingsHashMap() {
        return settings;

    }

    public boolean getBoolean(String key) {
        return ((Boolean) settings.get(key)).booleanValue();

    }

    public String getString(String key) {
        return settings.get(key).toString();
    }

    void main() {
        if (flags.get("l") != null) {
            list();
        }
        if (true || flags.get("s") != null) {
            gui();
        }
    }

    public static void main(String[] argv) {
        flags = SundryUtils.flagAsMap(argv);
        args = SundryUtils.argAsList(argv);
        Settings s = Settings.getSettings();
        s.main();
    }

}
