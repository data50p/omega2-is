package com.femtioprocent.omega.lesson.remote;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.SundryUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    Thread th;
    int port = 9900;

    public Server() {
        th = new Thread(this);
        th.start();
    }

    public void run() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "Server started");
        for (; ; ) {
            try {
                ServerSocket sso = new ServerSocket(port);
                for (; ; ) {
                    Socket so = sso.accept();
//log		    OmegaContext.sout_log.getLogger().info(":--: " + "lessond: Connection accepted");
                    Connection con = new Connection(so, this);
                    con.start();
                }
            } catch (IOException ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "lessond: Exception: " + ex);
                SundryUtils.m_sleep(3000);
            }
        }
    }
}
