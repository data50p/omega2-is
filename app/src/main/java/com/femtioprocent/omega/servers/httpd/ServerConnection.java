package com.femtioprocent.omega.servers.httpd;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.SundryUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ServerConnection extends Thread {
    String prefix = ".";
    Socket so;
    Server server;

    ServerConnection(Socket so, Server server) {
        super("httpd.ServerConnection");
        this.server = server;
        this.so = so;
    }

    List gHd(BufferedReader rd) throws IOException {
        List li = new ArrayList();

        for (; ; ) {
            String s = rd.readLine();
            if (s.length() == 0)
                return li;
            li.add(s);
        }
    }

    boolean access(String fn) {
        File f = new File(fn);
        return f.isFile();
    }

    String getLsData(String fn, char t) throws IOException {
        File f = new File(fn);
//	OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + f);
        if (f.isDirectory()) {
            StringBuffer sb = new StringBuffer();
            String[] l = f.list();
            for (int i = 0; i < l.length; i++) {
                if (l[i].equals("."))
                    continue;
                if (l[i].equals(".."))
                    continue;
                if (t == 'a')
                    sb.append(l[i] + "<a href=" + l[i] + "><br>\n");
                else if (t == 'i')
                    sb.append(l[i] + "<img src=" + l[i] + "><br>\n");
                else
                    sb.append(l[i] + '\n');
            }
            return sb.toString();
        } else
            return null;
    }

    byte[] getData(String fn) throws IOException {
        File f = new File(fn);
        FileInputStream fin = new FileInputStream(f);
        byte[] data = new byte[(int) f.length()];
        fin.read(data);
        return data;
    }

    private int fill(byte[] data, int j, String s) {
        for (int i = 0; i < s.length(); i++)
            data[j + i] = (byte) (s.charAt(i));
        return s.length();
    }

    private int insertString(byte[] data, int j, String cmd, String s) {
        int j0 = j;
        if (cmd.equals("status")) {
            for (int i = 0; i < s.length(); i++)
                data[j + i] = (byte) (s.charAt(i));
            return s.length();
        }
        if (cmd.equals("lesson")) {
            Iterator it = server.hm.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                String val = (String) server.hm.get(key);

                if (key.startsWith("lesson:")) {
                    String lkey = key.substring(7);
                    if (lkey.equals("background")) {
                        s = "<p>" + lkey + " = " + val + "  " + "<IMG src=\"" + val + "\"/>\n";
                        j += fill(data, j, s);
                    } else {
                        s = "<p>" + lkey + " = " + val + "\n";
                        j += fill(data, j, s);
                    }
                }
            }
            return j - j0;
        }
        return 0;
    }

    byte[] transformData(byte[] data) {
        int j = 0;
        byte[] data2 = new byte[data.length + 10000];

        for (int i = 0; i < data.length; i++) {
            boolean b = true;
            if (data[i + 0] == '<')
                if (data[i + 1] == '!')
                    if (data[i + 2] == '-')
                        if (data[i + 3] == '-')
                            if (data[i + 4] == ' ')
                                if (data[i + 5] == 'I')
                                    if (data[i + 6] == 'W')
                                        if (data[i + 7] == 'S')
                                            if (data[i + 8] == ':') {
                                                boolean done = false;
                                                StringBuffer sb = new StringBuffer();
                                                for (int jj = 1; jj < 500; jj++) {
                                                    byte ch = data[i + 8 + jj];
                                                    if (ch == '>') {
                                                        i += jj + 8;
                                                        break;
                                                    }
                                                    if (ch == ' ' && sb.length() > 0) {
                                                        done = true;
                                                    }
                                                    if (ch != ' ' && done == false) {
                                                        sb.append((char) ch);
                                                    }
                                                }
                                                j += insertString(data2, j, sb.toString(), getInfo());
                                                b = false;
                                            }
            if (b)
                data2[j++] = data[i];
        }
        return data2;
    }

    String[] whatFile(List li) {
        String s[] = new String[3];

        String[] sa = SundryUtils.split((String) (li.get(0)), " ");
        String fn = sa[1];
        String q = null;
        int ix = fn.indexOf('?');
        if (ix != -1) {
            fn = prefix + fn.substring(0, ix);
            q = fn.substring(ix + 1);
            s[1] = fn;
            s[2] = q;
            return s;
        } else {
            fn = prefix + fn;
            if (access(fn) == false)
                fn = fn + "/index.html";
            s[0] = fn;
            return s;
        }
    }

    String fixFN(String fn, String q) {
        int ix = fn.indexOf('?');
        if (ix != -1) {
            fn = prefix + fn.substring(0, ix);
            // String q = fn.substring(ix+1);
            if (q.length() == 0 && access(fn) == false)
                fn += "/index.html";
            return fn;
        } else {
            fn = prefix + fn;
            if (access(fn) == false)
                fn += "/index.html";
            return fn;
        }
    }

    String fixQ(String fn) {
        int ix = fn.indexOf('?');
        if (ix != -1) {
            // fn = prefix + fn.substring(0, ix);
            return fn.substring(ix + 1);
        } else {
//  	    fn = prefix + fn;
//  	    if ( access(fn) == false )
//  		fn = fn + "/index.html";
            return "";
        }
    }

    static String[][] mimeT = {{"text/html", ".html"},
            {"text/html", ".iws.html"},
            {"text/html", ".ihtml"},
            {"audio/x-wav", ".wav"},
            {"audio/MP3", ".mp3"},
            {"audio/x-au", ".au"},
            {"image/gif", ".gif"},
            {"image/png", ".png"},
            {"image/jpeg", ".jpg"},
            {"image/jpeg", ".jpeg"}
    };

    String getMime(String fn) {
        for (int i = 0; i < mimeT.length; i++) {
            if (fn.endsWith(mimeT[i][1]))
                return mimeT[i][0];
        }
        return "text/plain";
    }

    boolean getSSI(String fn) {
        return
                fn.endsWith(mimeT[1][1]) ||
                        fn.endsWith(mimeT[2][1]);
    }

    String getInfo() {
        String prefix = "<TABLE FRAME=box BORDER=1>" +
                "<THEAD>" +
                "<TR></TR>" +
                "</THEAD>" +
                "<TBODY>" +

                "<TR>" +
                "<TD>currenttime.date</TD>" +
                "<TD></TD>" +
                "<TD>" + new Date() + "</TD>" +
                "</TR>" +

                "<TR>" +
                "<TD>starttime.date</TD>" +
                "<TD></TD>" +
                "<TD>" + server.start_date + "</TD>" +
                "</TR>" +

                "<TR>" +
                "<TD>Connection count</TD>" +
                "<TD></TD>" +
                "<TD>" + server.connection_cnt + "</TD>" +
                "</TR>\n";

        String s = "";

        String suffix = "</TBODY>" +
                "</TABLE>\n";

        suffix += "<p>\n";

        return prefix + s + suffix;
    }

    void serve(List sL, BufferedReader rd, DataOutputStream dos) {
        try {
//	    OmegaContext.sout_log.getLogger().info(":--: " + "WEB(" + sL.get(0) + ")");
            String[] sa = SundryUtils.split((String) (sL.get(0)), " ");
            String q = fixQ(sa[1]);
            sa[1] = fixFN(sa[1], q);
            if (OmegaConfig.T)
                OmegaContext.sout_log.getLogger().info(":--: " + "serve " + SundryUtils.arrToString(sa) + ' ' + sL + ' ' + rd + ' ' + dos);
            if ("GET".equals(sa[0]))
                doGet(sa, q, sL, rd, dos);
            if ("POST".equals(sa[0]))
                doPost(sa, q, sL, rd, dos);
        } catch (IOException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "serve(): Exception " + ex);
        }
    }

    void doPost(String[] sa, String q, List sL, BufferedReader rd, DataOutputStream dos) throws IOException {
        String fn = sa[1];
        if (fn == null) {
            String cmd = sa[0];
            dos.writeBytes("HTTP/0.9 200 OK\r\n");
            dos.writeBytes("Server: Omega 0.9\r\n");
            dos.writeBytes("MIME-OmegaVersion: 1.0\r\n");
            String mime = "text/plain";
            dos.writeBytes("Content-type: " + mime + "\r\n");
            dos.writeBytes("\r\n");
            dos.writeBytes("Omega web fail");
            dos.flush();
        } else {
            try {
                if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "doPost " + sL + ' ' + rd);
                dos.writeBytes("HTTP/0.9 200 OK ERROR\r\n");
                dos.writeBytes("Server: Omega 0.9\r\n");
                dos.writeBytes("\r\n");
                dos.flush();
            } catch (Exception ex) {
            }
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "httpd: io done");
        }
    }

    void doGet(String[] sa, String q, List sL, BufferedReader rd, DataOutputStream dos) throws IOException {
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "GET " + SundryUtils.arrToString(sa) + ',' + q);
        String fn = sa[1];
        if (fn == null) {
            String cmd = sa[0];
            dos.writeBytes("HTTP/0.9 200 OK\r\n");
            dos.writeBytes("Server: Omega 0.9\r\n");
            dos.writeBytes("MIME-OmegaVersion: 1.0\r\n");
            String mime = "text/plain";
            dos.writeBytes("Content-type: " + mime + "\r\n");
            dos.writeBytes("\r\n");
            dos.writeBytes("Omega web fail");
            dos.flush();
        } else {
            try {
                if ("ls".equals(q)) {
                    String data = getLsData(fn, 'p');
                    if (data == null)
                        throw new IOException("");
                    dos.writeBytes("HTTP/1.0 200 OK\r\n");
                    dos.writeBytes("Server: Omega 0.9\r\n");
                    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n");
                    String mime = "text/plain";
                    dos.writeBytes("Content-type: " + mime + "\r\n");
                    //dos.writeBytes("Content-length: " + data.length() + "\r\n");
                    dos.writeBytes("\r\n");
                    dos.writeBytes(data);
                    dos.flush();
                } else if ("list".equals(q)) {
                    String data = getLsData(fn, 'a');
                    if (data == null)
                        throw new IOException("");
                    dos.writeBytes("HTTP/1.0 200 OK\r\n");
                    dos.writeBytes("Server: Omega 0.9\r\n");
                    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n");
                    String mime = "text/html";
                    dos.writeBytes("Content-type: " + mime + "\r\n");
                    //dos.writeBytes("Content-length: " + data.length() + "\r\n");
                    dos.writeBytes("\r\n");
                    dos.writeBytes(data);
                    dos.flush();
                } else if ("images".equals(q)) {
                    String data = getLsData(fn, 'i');
                    if (data == null)
                        throw new IOException("");
                    dos.writeBytes("HTTP/1.0 200 OK\r\n");
                    dos.writeBytes("Server: Omega 0.9\r\n");
                    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n");
                    String mime = "text/html";
                    dos.writeBytes("Content-type: " + mime + "\r\n");
                    //dos.writeBytes("Content-length: " + data.length() + "\r\n");
                    dos.writeBytes("\r\n");
                    dos.writeBytes(data);
                    dos.flush();
                } else {
                    byte[] data = getData(fn);
                    dos.writeBytes("HTTP/1.0 200 OK\r\n");
                    dos.writeBytes("Server: Omega 0.9\r\n");
                    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n");
                    String mime = getMime(fn);
                    dos.writeBytes("Content-type: " + mime + "\r\n");
                    dos.writeBytes("\r\n");
                    if (getSSI(fn))
                        data = transformData(data);
                    dos.write(data, 0, data.length);
                    dos.flush();
                }
            } catch (IOException ex) {
                dos.writeBytes("HTTP/0.9 200 OK ERROR\r\n");
                dos.writeBytes("Server: Omega 0.9\r\n");
                dos.writeBytes("\r\n");
                dos.flush();
            }
            if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "httpd: io done");
        }
    }

    static int r_cnt = 0;

    public void run() {
        long ct0 = SundryUtils.ct();

        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "httpd Connection established");
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(so.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(so.getOutputStream()));

            List li = gHd(rd);
            serve(li, rd, dos);
            so.close();
        } catch (IOException ex) {
        }
    }
}
