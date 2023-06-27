package com.femtioprocent.omega.servers.httpd;

import com.femtioprocent.omega.OmegaContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

public class Server extends Thread {
    ServerSocket sso;
    Hashtable ht;
    int port;

    Date start_date = new Date();

    int connection_cnt = 0;

    public Server() {
        this(8089);
    }

    public Server(int port) {
        super("httpd");
        this.port = port;
        ht = new Hashtable();
    }

    public void run() {
        OmegaContext.sout_log.getLogger().info("ERR: " + "httpd: Server started");
        try {
            sso = new ServerSocket(port);
            for (; ; ) {
                Socket so = sso.accept();
//		OmegaContext.sout_log.getLogger().info("ERR: " + "httpd: Connection accepted");
                connection_cnt++;
                ServerConnection con = new ServerConnection(so, this);
                con.start();
            }
        } catch (IOException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "httpd: Exception: " + ex);
        }
    }

    HashMap hm = new HashMap();

    public HashMap getHashMap() {
        return hm;
    }

    public static void main(String[] args) {
        Server s = new Server();
        s.start();
    }
}
