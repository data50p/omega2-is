package com.femtioprocent.omega.xml

interface ElementListener {
    fun startElement(name: String?, attr: HashMap<String?, String?>?, allAttr: HashMap<String?, String?>?)
    fun endElement(name: String?, elem_pcdata: HashMap<String?, String?>?)
}
