package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.util.ExtensionFileFilter;
import darrylbu.util.SwingUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

public class ChooseOmegaAssetsDir extends JFileChooser {
    public static String ext = "omega_assets";

    public ChooseOmegaAssetsDir() {
        super(new File("."));

        JTextField jTextField = SwingUtils.getDescendantOfType(
                JTextField.class, this, "Text", "");
        jTextField.setEditable(false);

        final JList list2 = SwingUtils.getDescendantOfType(JList.class, this, "Enabled", true);
        if (list2 != null) {
            final MouseListener mouseListener = list2.getMouseListeners()[2];
            list2.removeMouseListener(mouseListener);
            list2.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        mouseListener.mouseClicked(e);
                    }
                }
            });
        }

        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension(ext);
        setFileFilter(fi);
        setMultiSelectionEnabled(false);
        disableNav(this);
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setFileView(new FileView() {
            @Override
            public Boolean isTraversable(File f) {
                return (f.isDirectory() && f.getName().endsWith("." + ext));
            }
        });
        setFileSystemView(new FileSystemView() {
            @Override
            public File createNewFolder(File containingDir) throws IOException {
                return null;
            }
        });
    }

    private void disableNav(Container c) {
        int jbCnt = 0;
        for (Component x : c.getComponents())
            if (x instanceof JComboBox)
                ((JComboBox) x).setEnabled(false);
            else if (x instanceof JButton) {
                String text = ((JButton) x).getText();
                if (text == null || text.isEmpty())
                    ((JButton) x).setEnabled(false);
                if (text != null && text.length() > 0) {
                    jbCnt++;
                    if (jbCnt == 1)
                        ((JButton) x).setEnabled(false);
                }
            } else if (x instanceof Container)
                disableNav((Container) x);
    }
}
