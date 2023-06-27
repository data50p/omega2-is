package com.femtioprocent.omega.xml;

import java.util.HashMap;

public interface ElementListener {
    void startElement(String name, HashMap attr, HashMap allAttr);

    void endElement(String name, HashMap elem_pcdata);
}
