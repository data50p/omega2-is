package com.femtioprocent.omega.xml;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.SundryUtils;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;


@Deprecated
public class SAX_base extends DefaultHandler {
    int depth;
    HashMap flag;
    HashMap allAttr = new HashMap();

    static class MyErrorHandler extends DefaultHandler {
        // treat validation errors as fatal
        public void error(SAXParseException e)
                throws SAXParseException {
            throw e;
        }

        // dump warnings too
        public void warning(SAXParseException err)
                throws SAXParseException {
            OmegaContext.sout_log.getLogger().info("** Warning"
                    + ", line " + err.getLineNumber()
                    + ", uri " + err.getSystemId());
            OmegaContext.sout_log.getLogger().info("   " + err.getMessage());
        }
    }


    public void setDocumentLocator(Locator l) {
        // we'd record this if we needed to resolve relative URIs
        // in content or attributes, or wanted to give diagnostics.
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument()
            throws SAXException {
    }

    private Stack pcdataS = new Stack();
    private Stack elemS = new Stack();

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes attrs)
            throws SAXException {
        while (pcdataS.size() <= depth)
            pcdataS.push(new StringBuffer());
        while (elemS.size() <= depth + 1)
            elemS.push(new HashMap());

        HashMap attr = null;
        if (attrs != null) {
            boolean b = false;
            attr = new HashMap();
            for (int i = 0; i < attrs.getLength(); i++) {
                String n = attrs.getQName(i);
                String v = attrs.getValue(i);
                attr.put(n, v);
            }
            allAttr.put(qName, attr);
        }
        startElementHook(qName, attr, allAttr);

        depth++;
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        depth--;

        StringBuffer sb = (StringBuffer) pcdataS.get(depth);
        HashMap ht = (HashMap) elemS.get(depth);
        ht.put(qName, sb.toString());
        pcdataS.set(depth, new StringBuffer());


        HashMap ht1 = (HashMap) elemS.get(depth + 1);
        ht.putAll(ht1);
        endElementHook(qName, ht);

        elemS.set(depth + 1, new HashMap());
    }

    public void characters(char buf[], int offset, int len)
            throws SAXException {
        StringBuffer sb = (StringBuffer) pcdataS.get(depth - 1);
        sb.append(new String(buf, offset, len));
    }

    public void ignorableWhitespace(char buf[], int offset, int len)
            throws SAXException {
//	OmegaContext.sout_log.getLogger().info("ERR: " + "iW " + new String(buf, offset, len));
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        OmegaContext.sout_log.getLogger().info("ERR: " + "pI " + target);
    }

    public void notationDecl(String name, String publicId, String systemId) {
        OmegaContext.sout_log.getLogger().info("ERR: " + "nD " + name);
    }

    public void unparsedEntityDecl(String name, String publicId,
                                   String systemId, String notationName) {
        OmegaContext.sout_log.getLogger().info("ERR: " + "UeD " + name);
    }

    /**
     * Called for every scanned element
     */
    public void startElementHook(String name, HashMap attr, HashMap allAttr) {
        OmegaContext.sout_log.getLogger().info("" + depth + " <" + name + "  " + attr + " " + allAttr);
    }

    /**
     * Called for every scanned element.
     * "name_pcdata" contains all pcdata for last occurence of name (=key).
     * Only last piece kept if broken by other elements.
     */
    public void endElementHook(String name, HashMap elem_pcdata) {
        if (flag.get("L") != null)
            OmegaContext.sout_log.getLogger().info("" + depth + "  " + name + "> " + elem_pcdata + "");
        else
            OmegaContext.sout_log.getLogger().info("" + depth + "  " + name + "> " + elem_pcdata.get(name) + "");
    }

    public static void start(String file, SAX_base sb) throws IOException {
        start(file, sb, true);
    }

    public static void start(String file, SAX_base sb, boolean validating) throws IOException {
        InputSource input;

        try {
            String uri = "file:" + new File(file).getAbsolutePath();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            if (validating)
                spf.setValidating(true);

            spf.setNamespaceAware(true);

            SAXParser sp = spf.newSAXParser();
            XMLReader xmlr = sp.getXMLReader();
//	    Parser parser = sp.getParser();
            xmlr.setContentHandler(sb);
            xmlr.setErrorHandler(new MyErrorHandler());
            xmlr.parse(uri);
//	    parser.setDocumentHandler(sb);
//	    parser.setErrorHandler(new MyErrorHandler());
//	    parser.parse(uri);
        } catch (SAXParseException err) {
            OmegaContext.sout_log.getLogger().info("** Parsing error"
                    + ", line " + err.getLineNumber()
                    + ", uri " + err.getSystemId());
            OmegaContext.sout_log.getLogger().info("   " + err.getMessage());
            err.printStackTrace();
        } catch (SAXException e) {
            Exception x = e;
            if (e.getException() != null)
                x = e.getException();
            x.printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void main(String argv[]) throws IOException {
        HashMap flag = SundryUtils.flagAsMap(argv);
        List argl = SundryUtils.argAsList(argv);

        OmegaContext.sout_log.getLogger().info("ERR: " + "argl " + argl + ' ' + flag);

        String file = (String) argl.get(0);

        SAX_base sb = new SAX_base();

        boolean validating = true;
        if (flag.get("n") != null)
            validating = false;

        sb.flag = flag;
        start(file, sb, validating);
    }
}
