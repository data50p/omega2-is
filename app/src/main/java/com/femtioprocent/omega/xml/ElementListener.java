package com.femtioprocent.omega.xml;

import java.util.HashMap;

public interface ElementListener {
    void startElement(String name, HashMap<String,String> attr, HashMap<String,String> allAttr);

    void endElement(String name, HashMap<String,String> elem_pcdata);
}
