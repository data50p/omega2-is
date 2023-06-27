package com.femtioprocent.omega.adm.login;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.Factory;

public class LoginFactory {
    static public Login createLogin(String name) {
        String n = "Login" + name;
        try {
            Login login = (Login) Factory.createObject(n);
            return login;
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Can't create " + n + ": " + ex);
        }
        return null;
    }
}
