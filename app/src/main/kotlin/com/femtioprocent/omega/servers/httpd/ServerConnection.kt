package com.femtioprocent.omega.servers.httpd

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.util.SundryUtils.arrToString
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.split
import java.io.*
import java.net.Socket
import java.util.*

class ServerConnection internal constructor(var so: Socket, var server: Server) : Thread("httpd.ServerConnection") {
    var prefix = "."

    @Throws(IOException::class)
    fun gHd(rd: BufferedReader): List<String?> {
	val li: MutableList<String?> = ArrayList()
	while (true) {
	    val s = rd.readLine()
	    if (s.length == 0) return li
	    li.add(s)
	}
    }

    fun access(fn: String?): Boolean {
	val f = File(fn)
	return f.isFile
    }

    @Throws(IOException::class)
    fun getLsData(fn: String?, t: Char): String? {
	val f = File(fn)
	//	OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + f);
	return if (f.isDirectory) {
	    val sb = StringBuffer()
	    val l = f.list()
	    for (i in l.indices) {
		if (l[i] == ".") continue
		if (l[i] == "..") continue
		if (t == 'a') sb.append(l[i] + "<a href=" + l[i] + "><br>\n") else if (t == 'i') sb.append(l[i] + "<img src=" + l[i] + "><br>\n") else sb.append(
			l[i] + '\n'
		)
	    }
	    sb.toString()
	} else null
    }

    @Throws(IOException::class)
    fun getData(fn: String?): ByteArray {
	val f = File(fn)
	val fin = FileInputStream(f)
	val data = ByteArray(f.length().toInt())
	fin.read(data)
	return data
    }

    private fun fill(data: ByteArray, j: Int, s: String): Int {
	for (i in 0 until s.length) data[j + i] = s[i].code.toByte()
	return s.length
    }

    private fun insertString(data: ByteArray, j: Int, cmd: String, s: String): Int {
	var j = j
	var s = s
	val j0 = j
	if (cmd == "status") {
	    for (i in 0 until s.length) data[j + i] = s[i].code.toByte()
	    return s.length
	}
	if (cmd == "lesson") {
	    val it: Iterator<*> = server.hm.keys.iterator()
	    while (it.hasNext()) {
		val key = it.next() as String
		val `val` = server.hm[key]
		if (key.startsWith("lesson:")) {
		    val lkey = key.substring(7)
		    if (lkey == "background") {
			s = "<p>$lkey = $`val`  <IMG src=\"$`val`\"/>\n"
			j += fill(data, j, s)
		    } else {
			s = "<p>$lkey = $`val`\n"
			j += fill(data, j, s)
		    }
		}
	    }
	    return j - j0
	}
	return 0
    }

    fun transformData(data: ByteArray): ByteArray {
	var j = 0
	val data2 = ByteArray(data.size + 10000)
	var i = 0
	while (i < data.size) {
	    var b = true
	    if (data[i + 0] == '<'.code.toByte()) if (data[i + 1] == '!'.code.toByte()) if (data[i + 2] == '-'.code.toByte()) if (data[i + 3] == '-'.code.toByte()) if (data[i + 4] == ' '.code.toByte()) if (data[i + 5] == 'I'.code.toByte()) if (data[i + 6] == 'W'.code.toByte()) if (data[i + 7] == 'S'.code.toByte()) if (data[i + 8] == ':'.code.toByte()) {
		var done = false
		val sb = StringBuffer()
		for (jj in 1..499) {
		    val ch = data[i + 8 + jj]
		    if (ch == '>'.code.toByte()) {
			i += jj + 8
			break
		    }
		    if (ch == ' '.code.toByte() && sb.length > 0) {
			done = true
		    }
		    if (ch != ' '.code.toByte() && done == false) {
			sb.append(Char(ch.toUShort()))
		    }
		}
		j += insertString(data2, j, sb.toString(), info)
		b = false
	    }
	    if (b) data2[j++] = data[i]
	    i++
	}
	return data2
    }

    fun whatFile(li: List<String?>): Array<String?> {
	val s = arrayOfNulls<String>(3)
	val sa = split(li[0], " ")
	var fn = sa[1]
	var q: String? = null
	val ix = fn!!.indexOf('?')
	return if (ix != -1) {
	    fn = prefix + fn.substring(0, ix)
	    q = fn.substring(ix + 1)
	    s[1] = fn
	    s[2] = q
	    s
	} else {
	    fn = prefix + fn
	    if (access(fn) == false) fn = "$fn/index.html"
	    s[0] = fn
	    s
	}
    }

    fun fixFN(fn: String?, q: String): String {
	var fn = fn
	val ix = fn!!.indexOf('?')
	return if (ix != -1) {
	    fn = prefix + fn.substring(0, ix)
	    // String q = fn.substring(ix+1);
	    if (q.length == 0 && access(fn) == false) fn += "/index.html"
	    fn
	} else {
	    fn = prefix + fn
	    if (access(fn) == false) fn += "/index.html"
	    fn
	}
    }

    fun fixQ(fn: String?): String {
	val ix = fn!!.indexOf('?')
	return if (ix != -1) {
	    // fn = prefix + fn.substring(0, ix);
	    fn.substring(ix + 1)
	} else {
//  	    fn = prefix + fn;
//  	    if ( access(fn) == false )
//  		fn = fn + "/index.html";
	    ""
	}
    }

    fun getMime(fn: String): String {
	for (i in mimeT.indices) {
	    if (fn.endsWith(mimeT[i][1])) return mimeT[i][0]
	}
	return "text/plain"
    }

    fun getSSI(fn: String): Boolean {
	return fn.endsWith(mimeT[1][1]) ||
		fn.endsWith(mimeT[2][1])
    }

    val info: String
	get() {
	    val prefix = "<TABLE FRAME=box BORDER=1>" +
		    "<THEAD>" +
		    "<TR></TR>" +
		    "</THEAD>" +
		    "<TBODY>" +
		    "<TR>" +
		    "<TD>currenttime.date</TD>" +
		    "<TD></TD>" +
		    "<TD>" + Date() + "</TD>" +
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
		    "</TR>\n"
	    val s = ""
	    var suffix = "</TBODY>" +
		    "</TABLE>\n"
	    suffix += "<p>\n"
	    return prefix + s + suffix
	}

    fun serve(sL: List<String?>, rd: BufferedReader, dos: DataOutputStream) {
	try {
//	    OmegaContext.sout_log.getLogger().info(":--: " + "WEB(" + sL.get(0) + ")");
	    val sa = split(sL[0], " ")
	    val q = fixQ(sa[1])
	    sa[1] = fixFN(sa[1], q)
	    if (OmegaConfig.T) OmegaContext.sout_log.getLogger()
		    .info(":--: " + "serve " + arrToString(sa) + ' ' + sL + ' ' + rd + ' ' + dos)
	    if ("GET" == sa[0]) doGet(sa, q, sL, rd, dos)
	    if ("POST" == sa[0]) doPost(sa, q, sL, rd, dos)
	} catch (ex: IOException) {
	    OmegaContext.sout_log.getLogger().info("ERR: serve(): Exception $ex")
	}
    }

    @Throws(IOException::class)
    fun doPost(sa: Array<String?>, q: String?, sL: List<String?>, rd: BufferedReader, dos: DataOutputStream) {
	val fn = sa[1]
	if (fn == null) {
	    val cmd = sa[0]
	    dos.writeBytes("HTTP/0.9 200 OK\r\n")
	    dos.writeBytes("Server: Omega 0.9\r\n")
	    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n")
	    val mime = "text/plain"
	    dos.writeBytes("Content-type: $mime\r\n")
	    dos.writeBytes("\r\n")
	    dos.writeBytes("Omega web fail")
	    dos.flush()
	} else {
	    try {
		if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: doPost $sL $rd")
		dos.writeBytes("HTTP/0.9 200 OK ERROR\r\n")
		dos.writeBytes("Server: Omega 0.9\r\n")
		dos.writeBytes("\r\n")
		dos.flush()
	    } catch (ex: Exception) {
	    }
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "httpd: io done");
	}
    }

    @Throws(IOException::class)
    fun doGet(sa: Array<String?>, q: String, sL: List<String?>?, rd: BufferedReader?, dos: DataOutputStream) {
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "GET " + arrToString(sa) + ',' + q)
	val fn = sa[1]
	if (fn == null) {
	    val cmd = sa[0]
	    dos.writeBytes("HTTP/0.9 200 OK\r\n")
	    dos.writeBytes("Server: Omega 0.9\r\n")
	    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n")
	    val mime = "text/plain"
	    dos.writeBytes("Content-type: $mime\r\n")
	    dos.writeBytes("\r\n")
	    dos.writeBytes("Omega web fail")
	    dos.flush()
	} else {
	    try {
		if ("ls" == q) {
		    val data = getLsData(fn, 'p') ?: throw IOException("")
		    dos.writeBytes("HTTP/1.0 200 OK\r\n")
		    dos.writeBytes("Server: Omega 0.9\r\n")
		    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n")
		    val mime = "text/plain"
		    dos.writeBytes("Content-type: $mime\r\n")
		    //dos.writeBytes("Content-length: " + data.length() + "\r\n");
		    dos.writeBytes("\r\n")
		    dos.writeBytes(data)
		    dos.flush()
		} else if ("list" == q) {
		    val data = getLsData(fn, 'a') ?: throw IOException("")
		    dos.writeBytes("HTTP/1.0 200 OK\r\n")
		    dos.writeBytes("Server: Omega 0.9\r\n")
		    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n")
		    val mime = "text/html"
		    dos.writeBytes("Content-type: $mime\r\n")
		    //dos.writeBytes("Content-length: " + data.length() + "\r\n");
		    dos.writeBytes("\r\n")
		    dos.writeBytes(data)
		    dos.flush()
		} else if ("images" == q) {
		    val data = getLsData(fn, 'i') ?: throw IOException("")
		    dos.writeBytes("HTTP/1.0 200 OK\r\n")
		    dos.writeBytes("Server: Omega 0.9\r\n")
		    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n")
		    val mime = "text/html"
		    dos.writeBytes("Content-type: $mime\r\n")
		    //dos.writeBytes("Content-length: " + data.length() + "\r\n");
		    dos.writeBytes("\r\n")
		    dos.writeBytes(data)
		    dos.flush()
		} else {
		    var data = getData(fn)
		    dos.writeBytes("HTTP/1.0 200 OK\r\n")
		    dos.writeBytes("Server: Omega 0.9\r\n")
		    dos.writeBytes("MIME-OmegaVersion: 1.0\r\n")
		    val mime = getMime(fn)
		    dos.writeBytes("Content-type: $mime\r\n")
		    dos.writeBytes("\r\n")
		    if (getSSI(fn)) data = transformData(data)
		    dos.write(data, 0, data.size)
		    dos.flush()
		}
	    } catch (ex: IOException) {
		dos.writeBytes("HTTP/0.9 200 OK ERROR\r\n")
		dos.writeBytes("Server: Omega 0.9\r\n")
		dos.writeBytes("\r\n")
		dos.flush()
	    }
	    if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "httpd: io done")
	}
    }

    override fun run() {
	val ct0 = ct()
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "httpd Connection established")
	try {
	    val rd = BufferedReader(InputStreamReader(so.getInputStream()))
	    val dos = DataOutputStream(BufferedOutputStream(so.getOutputStream()))
	    val li = gHd(rd)
	    serve(li, rd, dos)
	    so.close()
	} catch (ex: IOException) {
	}
    }

    companion object {
	var mimeT = arrayOf(
		arrayOf("text/html", ".html"),
		arrayOf("text/html", ".iws.html"),
		arrayOf("text/html", ".ihtml"),
		arrayOf("audio/x-wav", ".wav"),
		arrayOf("audio/MP3", ".mp3"),
		arrayOf("audio/x-au", ".au"),
		arrayOf("image/gif", ".gif"),
		arrayOf("image/png", ".png"),
		arrayOf("image/jpeg", ".jpg"),
		arrayOf("image/jpeg", ".jpeg")
	)
	var r_cnt = 0
    }
}
