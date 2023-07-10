package com.femtioprocent.omega.lesson.actions;

import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public interface ActionI {
    public Element prefetch(String action_s);

    public void perform(Window window,
                        String action_s,
                        String[] actA,
                        String[] pathA,
                        int ord,
                        Runnable hook,
                        HashMap colors);

    public void show();

    public JPanel getCanvas();

    public String getPathList();

    public String getActorList();

    public HashMap getHm();

    public void clearScreen();

    public void clean();
}
