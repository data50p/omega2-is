package com.femtioprocent.omega.util;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.appl.Omega_IS;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.*;

public class Log {
    static FileHandler fh = null;
    static Handler h2;

    static class MyHandler extends StreamHandler {
        MyHandler(OutputStream out, Formatter f) {
            super(out, f);
        }

        public synchronized void publish(LogRecord record) {
            super.publish(record);
            flush();
        }
    }

    private static class MyFormatter extends Formatter {
        static DateFormat dformat = new SimpleDateFormat("dd/MM HH:mm:ss.SSS");
        long last = SundryUtils.ct();

        public String format(LogRecord record) {
            String s = record.getSourceClassName();
            s = s.substring(s.lastIndexOf('.') + 1);
            long ms = record.getMillis();
            long th_id = record.getLongThreadID();
            Date dt = new Date(ms);
            String d = dformat.format(dt);
            int lt = (int) (ms - last);
            last = ms;
            return "" +
                    SundryUtils.padRight("" + record.getLevel(), 10, ' ') +
                    d + ' ' +
                    SundryUtils.padLeft("" + th_id, 12, ' ') + ' ' +
                    SundryUtils.padLeft("" + lt, 5, ' ') + ' ' +
                    SundryUtils.padRight("" + s, 23, ' ') + ' ' +
                    SundryUtils.padRight("" + record.getSourceMethodName(), 22, ' ') + ' ' +
                    record.getMessage() + '\n';
        }
    }

    private static MyFormatter my_formatter = new MyFormatter();

    private static HashMap<String, Logger> myMap = new HashMap<String, Logger>();

    public static Logger getLogger() {
        return getLogger(Omega_IS.class);
    }

    private static Logger getLogger(Class clazz) {
        try {
            Logger logger = myMap.get(clazz.getName());
            if (logger != null)
                return logger;
            logger = Logger.getLogger(clazz.getName());
            myMap.put(clazz.getName(), logger);
            //logger.getParent().setLevel(Level.OFF);
            logger.setLevel(Level.ALL);
            if (fh == null) {
                File d = new File("logs");
                d.mkdir();
                fh = new FileHandler(d.getName() + '/' + "omega.log");
            }
            fh.setFormatter(my_formatter);
            logger.addHandler(fh);
            if (OmegaContext.isDeveloper()) {
                h2 = new MyHandler(System.err, my_formatter);
                logger.addHandler(h2);
            }
            logger.setUseParentHandlers(false);
            return logger;
        } catch (IOException ex) {
        } catch (NoClassDefFoundError ex) {
        }
        return null;
    }
}

