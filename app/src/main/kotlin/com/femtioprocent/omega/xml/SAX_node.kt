package com.femtioprocent.omega.xml

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.util.Log.getLogger
import org.xml.sax.*
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.IOException
import java.util.*
import javax.xml.parsers.SAXParserFactory

class SAX_node : DefaultHandler() {
    internal class MyErrorHandler : DefaultHandler() {
	// treat validation errors as fatal
	@Throws(SAXParseException::class)
	override fun error(e: SAXParseException) {
	    throw e
	}

	// dump warnings too
	@Throws(SAXParseException::class)
	override fun warning(err: SAXParseException) {
	    OmegaContext.sout_log.getLogger().info(
		    "** Warning"
			    + ", line " + err.lineNumber
			    + ", uri " + err.systemId
	    )
	    OmegaContext.sout_log.getLogger().info("   " + err.message)
	}
    }

    override fun setDocumentLocator(l: Locator) {
	// we'd record this if we needed to resolve relative URIs
	// in content or attributes, or wanted to give diagnostics.
    }

    @Throws(SAXException::class)
    override fun startDocument() {
    }

    @Throws(SAXException::class)
    override fun endDocument() {
    }

    var stack: Stack<Element> = Stack()

    @Throws(SAXException::class)
    override fun startElement(
	    namespaceURI: String,
	    localName: String,
	    qName: String,
	    attrs: Attributes
    ) {
	val el1 = Element(qName)
	if (attrs != null) {
	    for (i in 0 until attrs.length) {
		val n = attrs.getQName(i)
		val v = attrs.getValue(i)
		el1.addAttr(n, v)
	    }
	}
	stack.push(el1)
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
	val e = stack.pop() as Element
	if (!stack.isEmpty()) {
	    val e1 = stack.peek() as Element
	    e1.add(e)
	} else {
	    el = e
	}
    }

    @Throws(SAXException::class)
    override fun characters(buf: CharArray, offset: Int, len: Int) {
	var len = len
	val e = stack.peek() as Element
	if (buf[offset + len - 1] == '\n') len--
	val s = String(buf, offset, len)
	if ( s.length > 0) e.add(PCDATA(s))
    }

    @Throws(SAXException::class)
    override fun ignorableWhitespace(buf: CharArray, offset: Int, len: Int) {
//	OmegaContext.sout_log.getLogger().info(":--: " + "iW " + new String(buf, offset, len));
    }

    @Throws(SAXException::class)
    override fun processingInstruction(target: String, data: String) {
	OmegaContext.sout_log.getLogger().info(":--: pI $target")
    }

    override fun notationDecl(name: String, publicId: String, systemId: String) {
	OmegaContext.sout_log.getLogger().info(":--: nD $name")
    }

    override fun unparsedEntityDecl(
	    name: String, publicId: String,
	    systemId: String, notationName: String
    ) {
	OmegaContext.sout_log.getLogger().info("ERR: UeD $name")
    }

    companion object {
	var el: Element? = null

	/**
	 * Called for every scanned element
	 */
	@Throws(IOException::class)
	private fun element(file: String, sn: SAX_node, validating: Boolean = true): Element? {
	    var input: InputSource
	    try {
		val uri = File(file).toURI().toString()
		val spf = SAXParserFactory.newInstance()
		if (validating) spf.isValidating = true
		spf.isNamespaceAware = !true
		val sp = spf.newSAXParser()
		val xmlr = sp.xmlReader
		//	    Parser parser = sp.getParser();
		xmlr.contentHandler = sn
		xmlr.errorHandler = MyErrorHandler()
		xmlr.parse(uri)

//	    SAXParserFactory spf = SAXParserFactory.newInstance();
//	    if (validating)
//		spf.setValidating(true);

//	    SAXParser sp = spf.newSAXParser();
//	    Parser parser = sp.getParser();
//	    parser.setDocumentHandler(sn);
//	    parser.setErrorHandler(new MyErrorHandler());
//	    parser.parse(uri);
		return el
	    } catch (err: SAXParseException) {
		getLogger().info(
			"** Parsing error"
				+ ", line " + err.lineNumber
				+ ", uri " + err.systemId
		)
		getLogger().info("   " + err.message)
	    } catch (e: SAXException) {
		var x: Exception = e
		if (e.exception != null) x = e.exception
		x.printStackTrace()
	    } catch (t: Throwable) {
//            t.printStackTrace ();
	    }
	    return null
	}

	fun parse(file: String, validating: Boolean): Element? {
	    try {
		val sn = SAX_node()
		getLogger().info("Loading xml: (A) $file")
		val el = element(file, sn, validating)
		getLogger().info("           : $el")
		return el
	    } catch (ex: IOException) {
		getLogger().info("           : $ex")
	    }
	    return null
	}
    }
}
