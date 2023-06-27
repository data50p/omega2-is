package com.femtioprocent.omega.adm.register.data;

import com.femtioprocent.omega.xml.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class Result {
    Result() {

    }

    public String mkFname(String pname) {
        Date d = getFirstPerformDate();
//	DateFormat df = DateFormat.getDateTimeInstance();
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String s = pname + '-' + df.format(d) + '-' + getLessonName();
        String fn = s.replace(':', '-').replace(' ', '_');
        return fn;
    }

    String getType() {
        return "generic";
    }

    abstract String getLessonName();

    abstract Date getPerformDate();

    abstract Date getFirstPerformDate();

    abstract Element getElement();
}
