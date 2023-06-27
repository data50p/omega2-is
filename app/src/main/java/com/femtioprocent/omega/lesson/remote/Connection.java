package com.femtioprocent.omega.lesson.remote;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.util.SundryUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Connection extends Thread {
    Socket so;
    Server server;

    Connection(Socket so, Server server) {
        super("lessond.Connection");
        this.server = server;
        this.so = so;
    }

    void serve(InputStream is, DataOutputStream dos) { // BufferedReader rd, DataOutputStream dos) {
        try {
            LOOP:
            for (; ; ) {
                int ch = is.read(); //.readLine();
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "read lessond " + ch);
                if (ch == -1)
                    break;

                try {
                    switch (ch) {
                        case 'l':
                            Lesson.le_canvas.gotoBoxRel(1, 0);
                            break;
                        case 'h':
                            Lesson.le_canvas.gotoBoxRel(-1, 0);
                            break;
                        case 'k':
                            Lesson.le_canvas.gotoBoxRel(0, -1);
                            break;
                        case 'j':
                            Lesson.le_canvas.gotoBoxRel(0, 1);
                            break;
                        case ' ':
                            Lesson.le_canvas.selectBox(false, SundryUtils.ct());
                            break;
                        case 'Q':
                            break LOOP;
                    }
                } catch (NullPointerException ex) {
                }
            }
        } catch (IOException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "serve(): Exception " + ex);
        }
    }

    public void run() {
        long ct0 = SundryUtils.ct();

        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("ERR: " + "lessond Connection established");
        try {
                /*BufferedReader rd = new BufferedReader(new InputStreamReader(*/
            InputStream is = so.getInputStream(); // ));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(so.getOutputStream()));

            serve(is, dos);
            is.close();
            dos.close();
            so.close();
        } catch (IOException ex) {
        }
    }
}
