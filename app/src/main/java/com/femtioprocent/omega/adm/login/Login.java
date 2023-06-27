package com.femtioprocent.omega.adm.login;

import com.femtioprocent.omega.adm.people.PeopleTeacher;
import com.femtioprocent.omega.adm.people.PeopleUser;
import com.femtioprocent.omega.util.Log;

import javax.swing.*;

abstract public class Login {
    static final int USER = 1;
    static final int TEACHER = 2;
    static final int BOTH = 3;

    JComponent comp;
    PeopleUser user;
    PeopleTeacher teacher;
    boolean ready = false;
    Object o = new Object();
    int mode = USER;

    Login() {
        user = new PeopleUser();
        teacher = new PeopleTeacher();
    }

    public JComponent getJComponent() {
        return comp;
    }

    public void setMode(int m) {
        mode = m;
    }

    public void setName(String name) {
        Log.getLogger().info("ERR: " + "setName " + name + mode);
        switch (mode) {
            case USER:
                user.setName(name);
                break;
            case TEACHER:
                teacher.setName(name);
                break;
        }
    }

    public void waitDone() {
        synchronized (o) {
            while (!ready) {
                try {
                    o.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
