package com.femtioprocent.omega.util;

import com.femtioprocent.omega.OmegaContext;

import java.lang.reflect.InvocationTargetException;

public class Factory {
    static public Object createObject(String clazz_name) {
        try {
            Class clazz = Class.forName(clazz_name);
            Object o = clazz.getDeclaredConstructor().newInstance();
            return o;
        } catch (ClassNotFoundException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Can't load class " + clazz_name + ": " + ex);
        } catch (IllegalAccessException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Can't access class " + clazz_name + ": " + ex);
        } catch (InstantiationException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Can't instantiate class " + clazz_name + ": " + ex);
        } catch (InvocationTargetException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Can't instantiate class " + clazz_name + ": " + ex);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
