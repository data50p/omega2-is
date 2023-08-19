package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.OmegaVersion
import com.femtioprocent.omega.adm.assets.TargetCombinations
import com.femtioprocent.omega.adm.assets.TargetCombinations.TCItem
import com.femtioprocent.omega.lesson.LessonContext
import com.femtioprocent.omega.lesson.appl.ApplContext
import com.femtioprocent.omega.swing.TableSorter
import com.femtioprocent.omega.swing.filechooser.ChooseDir
import com.femtioprocent.omega.swing.filechooser.ChooseOmegaBundleFile
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files.mkRelFname1
import com.femtioprocent.omega.util.Files.toURL
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.value.Value
import com.femtioprocent.omega.xml.Element
import org.hs.jfc.FormPanel
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.TableModel
import javax.swing.text.Document

class OmegaAssetsProperty internal constructor(var owner: JFrame?, var l_ctxt: LessonContext) :
	Property_B(owner, t("Omega - Assets Bundle Dialog")) {
    var guimap = HashMap<String, JComponent>()
    var table: JTable? = null
    var set_act_b: JButton? = null
    var set_sgn_b: JButton? = null
    var rb_def: JRadioButton? = null
    var rb_act: JRadioButton? = null
    var rb_defSign: JRadioButton? = null
    var rb_actSign: JRadioButton? = null
    var tmod: OmAssProp_TableModel? = null
    var latestTargetCombinations: TargetCombinations? = null
    private var oaBundleJB: JButton? = null
    private var imBundle: JButton? = null
    private var scanBundle: JButton? = null
    private var infoTF: JTextField? = null
    fun destroy() {}
    fun refresh() {}
    override fun getPreferredSize(): Dimension {
	val d = super.getPreferredSize()
	return Dimension(d.getWidth().toInt() + 100, d.getHeight().toInt())
    }

    inner class myActionListener : ActionListener {
	operator fun set(what: String, value: String?) {
	    var value = value
	    val tf = guimap[what] as JTextField?
	    if (value == null) {
		val def = tf!!.text
		value = l_ctxt.lessonCanvas.askForOneTarget(this@OmegaAssetsProperty, def)
		if (value == null) value = def

		// what to do ?
	    }
	}

	override fun actionPerformed(ev: ActionEvent) {
	    val s = ev.actionCommand
	    if (s == "save bundle") {
		val choose_f = ChooseOmegaBundleFile()
		val url_s: String? = null
		val rv = choose_f.showDialog(ApplContext.top_frame, t("Save"))
		OmegaContext.sout_log.getLogger().info(":--: choose file -> $rv")
		if (rv == JFileChooser.APPROVE_OPTION) {
		    var file = choose_f.selectedFile
		    if (!file.name.endsWith(OmegaConfig.OMEGA_BUNDLE_EXTENSION)) file =
			    File(file.path + OmegaConfig.OMEGA_BUNDLE_EXTENSION)
		    try {
			val manifest: MutableList<String> = ArrayList()
			for (s2 in targetCombinationsBuilder.asOne().src_set) {
			    val manifestInfo = manifestInfo(s2.fn)
			    manifest.add(manifestInfo)
			}
			for (s2 in targetCombinationsBuilder.asOne().dep_set) {
			    val manifestInfo = manifestInfo(s2.fn)
			    manifest.add(manifestInfo)
			}
			val out = ZipOutputStream(FileOutputStream(file))
			val sb = StringBuilder()
			sb.append("type: omega-assets\n")
			sb.append("version: " + OmegaVersion.theOmegaVersion + "\n")
			sb.append("saved: " + Date() + "\n")
			sb.append("user: " + System.getProperty("user.name") + "\n")
			sb.append("info: " + infoTF!!.text + "\n")
			for (man in manifest) sb.append(man + "\n")
			putData(out, OMEGA_BUNDLE_MANIFEST, sb.toString())
			for (s2 in targetCombinationsBuilder.asOne().src_set) {
			    put(out, s2.fn)
			}
			for (s2 in targetCombinationsBuilder.asOne().dep_set) {
			    put(out, s2.fn)
			}
			out.close()
		    } catch (e: IOException) {
		    }
		}
	    }
	    if (s == "new bundle") {
		targetCombinationsBuilder = TargetCombinations.Builder()
		//		targetCombinationsBuilder.add(latestTargetCombinations);
		latestTargetCombinations = targetCombinationsBuilder.asOne()
		tmod!!.update(latestTargetCombinations!!)
		oaBundleJB!!.text = t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize()
	    }
	    if (s == "add bundle") {
		targetCombinationsBuilder.add(latestTargetCombinations!!)
		latestTargetCombinations = targetCombinationsBuilder.asOne()
		tmod!!.update(latestTargetCombinations!!)
		oaBundleJB!!.text = t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize()
	    }
	    if (s == "import bundle") {
		importOmegaAssetsBundle(true)
		latestTargetCombinations?.let { tmod!!.update(it) }
		oaBundleJB!!.text = t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize()
	    }
	    if (s == "view bundle") {
		importOmegaAssetsBundle(false)
		latestTargetCombinations?.let { tmod!!.update(it) }
	    }
	    if (s == "scan add bundle") {
		scanAddOmegaAssetsBundle()
		latestTargetCombinations?.let { tmod!!.update(it) }
		oaBundleJB!!.text = t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize()
	    }
	    if (s == "close") {
		isVisible = false
	    }
	}
    }

    private fun scanAddOmegaAssetsBundle() {
	val choose_f = ChooseDir()
	val rv = choose_f.showDialog(ApplContext.top_frame, t("Scan"))
	OmegaContext.sout_log.getLogger().info(":--: choose file -> $rv")
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val dir = choose_f.selectedFile
	    val list: MutableList<File> = ArrayList()
	    scanOmegaLessons(dir, list)
	    val th = Thread {
		try {
		    imBundle!!.isEnabled = false
		    scanBundle!!.isEnabled = false
		    for (file in list) {
			val url_s = toURL(file)
			val fn = mkRelFname1(url_s!!)
			OmegaContext.serr_log.getLogger().info("scanned: $fn")
			l_ctxt.lesson.messageHandler.sendMsgWait("load", fn)
			m_sleep(200)
			latestTargetCombinations = l_ctxt.lessonCanvas.getAllTargetCombinationsEx2(false)
			latestTargetCombinations!!.src_set.add(TCItem(l_ctxt.lesson.loadedFName!!))
			targetCombinationsBuilder.add(latestTargetCombinations!!)
			latestTargetCombinations = targetCombinationsBuilder.asOne()
			tmod!!.update(latestTargetCombinations!!)
			oaBundleJB!!.text = t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize()
		    }
		} finally {
		    imBundle!!.isEnabled = true
		    scanBundle!!.isEnabled = true
		}
	    }
	    th.start()
	}
    }

    private fun scanOmegaLessons(file: File, list: MutableList<File>) {
	val files = file.listFiles()
	if ( files == null )
	{
	    OmegaContext.serr_log.getLogger().warning("Files is null: $file")
	} else {
	    for (f in files) {
		if (f.isDirectory) scanOmegaLessons(f, list)
		if (f.name.endsWith(".omega_lesson")) list.add(f)
	    }
	}
    }

    private fun manifestInfo(fName: String, exist: Boolean? = null): String {
	val f = File(omegaAssets(fName))
	return if (exist == null) if (f.exists() && f.canRead()) "entry: " + f.length() + ", " + fName else "entry: -1, $fName" else "entry: " + f.length() + ", " + fName
    }

    private fun importOmegaAssetsBundle(unpack: Boolean) {
	latestTargetCombinations =
		if (latestTargetCombinations == null || unpack) TargetCombinations() else latestTargetCombinations
	val choose_f = ChooseOmegaBundleFile()
	val url_s: String? = null
	val rv = choose_f.showDialog(ApplContext.top_frame, t(if (unpack) "Import" else "List"))
	OmegaContext.sout_log.getLogger().info(":--: choose file -> $rv")
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_f.selectedFile
	    if (!file.name.endsWith(".omega_bundle")) // add dialog
		return
	    try {
		val `in` = ZipInputStream(FileInputStream(file))
		while (true) {
		    val zent = `in`.nextEntry ?: break
		    var name = zent.name
		    if (zent.isDirectory) {
			val dir = File(omegaAssets(name))
			if (unpack) {
			    if (dir.mkdirs()) {
				OmegaContext.serr_log.getLogger().info("Created dir: T $dir")
			    } else {
				OmegaContext.serr_log.getLogger().info("Created dir: f $dir")
			    }
			}
		    } else {
			try {
			    OmegaContext.serr_log.getLogger().info("Got: " + name + ' ' + omegaAssets("."))
			    var output: FileOutputStream? = null
			    val obManifest = OMEGA_BUNDLE_MANIFEST == name
			    if (obManifest) name = rmExt(file.name) + "-" + name
			    val entFile = File(omegaAssets(name))
			    val time = zent.time
			    try {
				if (unpack) {
				    if (!entFile.parentFile.exists()) entFile.parentFile.mkdirs()
				    if (entFile.exists()) {
					OmegaContext.serr_log.getLogger().info("Overwrite: exist $entFile")
					//                                    continue;
				    }
				}
				output = if (unpack) FileOutputStream(entFile) else null
				var len = 0
				val buffer = ByteArray(4096)
				while (`in`.read(buffer).also { len = it } > 0) {
				    if (unpack) output!!.write(buffer, 0, len)
				    // hack
				    if (obManifest) {
					val infoText = String(buffer, 0, len)
					val sa = infoText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
						.toTypedArray()
					for (s in sa) {
					    if (s.startsWith("info: ")) infoTF!!.text = s.substring(6)
					}
				    }
				}
				if (name.endsWith(".omega_lesson")) {
				    latestTargetCombinations!!.src_set.add(TCItem(name))
				} else {
				    if (!obManifest) latestTargetCombinations!!.dep_set.add(TCItem(name))
				}
			    } catch (e: IOException) {
				e.printStackTrace()
			    } finally {
				output?.close()
			    }
			    if (unpack) entFile.setLastModified(time)
			} catch (ex: Exception) {
			    ex.printStackTrace()
			}
		    }
		}
		`in`.close()
	    } catch (e: IOException) {
	    }
	}
    }

    private fun rmExt(name: String): String {
	val ix = name.lastIndexOf(".")
	return if (ix != -1) name.substring(0, ix) else name
    }

    private fun print(pw: PrintWriter, prfx: String, s2: String) {
	val f = File(omegaAssets(s2))
	val stat = if (f.exists() && f.canRead()) "OK" else "??"
	pw.println("$prfx, $stat, $s2")
    }

    @Throws(IOException::class)
    private fun put(out: ZipOutputStream, s2: String) {
	val data = fileAsBytaArray(s2)
	if (data != null) {
	    val e = ZipEntry(s2)
	    val f = File(omegaAssets(s2))
	    e.time = f.lastModified()
	    out.putNextEntry(e)
	    out.write(data, 0, data.size)
	    out.closeEntry()
	}
    }

    @Throws(IOException::class)
    private fun putData(out: ZipOutputStream, name: String, text: String) {
	val e = ZipEntry(name)
	out.putNextEntry(e)
	out.write(text.toByteArray(charset("utf-8")))
	out.closeEntry()
    }

    private fun fileAsBytaArray(fn: String): ByteArray? {
	val f = File(omegaAssets(fn))
	try {
	    var fileSize = f.length()
	    if (fileSize > Int.MAX_VALUE) {
		fileSize = Int.MAX_VALUE.toLong()
	    }
	    val `is`: InputStream = FileInputStream(f)
	    val data = ByteArray(fileSize.toInt())
	    val buf = ByteArray(1024)
	    var pos = 0
	    while (true) {
		val n = `is`.read(buf)
		if (n == -1) break
		System.arraycopy(buf, 0, data, pos, n)
		pos += n
	    }
	    return data
	} catch (e: FileNotFoundException) {
	    e.printStackTrace()
	} catch (e: IOException) {
	    e.printStackTrace()
	}
	return null
    }

    var myactl: myActionListener = myActionListener()

    // when item in table selected
    inner class MyListSelectionModel : DefaultListSelectionModel(), ListSelectionListener {
	init {
	    addListSelectionListener(this)
	}

	override fun valueChanged(ev: ListSelectionEvent) {
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "" + ev);
	    if (ev.valueIsAdjusting == false) {
		val lselmod_ = ev.source as MyListSelectionModel
		val ix = lselmod_.minSelectionIndex
		if (ix >= 0) {
		    val tmod = table!!.model as TableModel
		    val s = tmod.getValueAt(ix, COL_ACT1) as String
		}
	    }
	}
    }

    var lselmod: MyListSelectionModel = MyListSelectionModel()

    init {
	build(contentPane)
	pack()
	isVisible = true
    }

    internal inner class CloseAction : AbstractAction(t("Close")) {
	override fun actionPerformed(ev: ActionEvent) {
	    isVisible = false
	}
    }

    fun build(con: Container) {
	val fpan = FormPanel(5, 5, 7, 15)

	//	JPanel pan1 = new JPanel();
	con.layout = BorderLayout()
	var jl: JLabel?
	var tf: JTextField
	var cb: JComboBox<*>
	var ch: JCheckBox
	var jb: JButton
	var Y = 0
	var X = 0

// 	fpan.add(new JLabel(T.t("Parameter:   ")), gbcf.createL(X++, Y, 1));
// 	fpan.add(new JLabel(T.t("Value:          ")),  gbcf.createL(X++, Y, 1));

// 	Y++;
// 	X = 0;
	fpan.add(JLabel(t("Info")).also { jl = it }, JTextField("", 40).also { tf = it }, Y, ++X)
	infoTF = tf
	infoTF!!.isEditable = true
	guimap["info"] = tf
	tf.document.addDocumentListener(mydocl)
	tf.isEnabled = true
	fpan.add(JLabel(""), JButton(t("New Omega Bundle")).also { jb = it }, Y, ++X)
	jb.actionCommand = "new bundle"
	jb.addActionListener(myactl)
	fpan.add(
		JLabel(""),
		JButton(t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize()).also { jb = it },
		Y,
		++X
	)
	jb.actionCommand = "add bundle"
	jb.addActionListener(myactl)
	oaBundleJB = jb
	fpan.add(JLabel(""), JButton(t("Scan and Add to Bundle")).also { jb = it }, Y, ++X)
	jb.actionCommand = "scan add bundle"
	jb.addActionListener(myactl)
	scanBundle = jb
	Y++
	X = 0
	fpan.add(JLabel(""), JButton(t("Save Omega Assets Bundle")).also { jb = it }, Y, ++X)
	jb.actionCommand = "save bundle"
	jb.addActionListener(myactl)
	fpan.add(JLabel(""), JButton(t("View & Add Omega Bundle")).also { jb = it }, Y, ++X)
	jb.actionCommand = "view bundle"
	jb.addActionListener(myactl)
	fpan.add(JLabel(""), JButton(t("Import Omega Assets Bundle")).also { jb = it }, Y, ++X)
	jb.actionCommand = "import bundle"
	jb.addActionListener(myactl)
	imBundle = jb
	Y++
	X = 0
	fpan.add(JLabel(""), JLabel(t("(Shift) Click on the table header to (reverse) sort")), Y, ++X)
	Y++
	X = 0
	latestTargetCombinations = l_ctxt.lessonCanvas.getAllTargetCombinationsEx2(false)
	latestTargetCombinations!!.src_set.add(TCItem(l_ctxt.lesson.loadedFName!!))
	tmod = OmAssProp_TableModel(this, latestTargetCombinations!!)
	val tsort = TableSorter(tmod!!)
	table = JTable(tsort)
	tsort.addMouseListenerToHeaderInTable(table!!)
	val jscr = JScrollPane(
		table,
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
	)
	for (i in 0 until table!!.columnModel.columnCount) {
	    val tcol = table!!.columnModel.getColumn(i)
	    tcol.preferredWidth = if (i == 0) 350 else if (i == 1) 350 else if (i == 2) 60 else 60
	}
	try {
	    table!!.autoResizeMode = JTable.AUTO_RESIZE_OFF
	    table!!.selectionModel = lselmod
	    table!!.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
	    table!!.setRowSelectionInterval(0, 0)
	    table!!.preferredScrollableViewportSize = Dimension(830, 300)
	} catch (ex: Exception) {
	}
	con.add(fpan, BorderLayout.NORTH)
	con.add(jscr, BorderLayout.CENTER)
	val jpa = JPanel()
	jpa.add(JButton(CloseAction()).also { jb = it })
	con.add(jpa, BorderLayout.SOUTH)
	oaBundleJB!!.text = t("Import Omega Bundle") + " " + targetCombinationsBuilder.srcSize()
    }

    override fun updTrigger(doc: Document) {
	guimap.keys.forEach {key ->
	    val o: Any? = guimap[key]
	    if (o is JTextField) {
		val tf = o
		if (doc === tf.document) {
		    val txt = tf.text
		    fireValueChanged(Value(key, txt))
		}
	    }
	}
    }

    fun setLabel(id: String, txt: String?) {
	guimap.keys.forEach {key ->
	    if (key == id) {
		val o: Any? = guimap[key]
		if (o is JLabel) {
		    o.text = txt
		}
	    }
	}
    }

    override fun updTrigger(cb: JComboBox<*>?) {
	try {
	    val cbg: JComboBox<*>?

//  	    cbg = (JComboBox)guimap.get("type");
//  	    if ( cb == cbg ) {
//  		String s = (String)cb.getSelectedItem();
//  		OmegaContext.sout_log.getLogger().info(":--: " + "CB type " + cb);
//  		if ( s.equals("action") )
//  		    setLabel("Llid", "Path id");
//  		if ( s.equals("actor") )
//    		    setLabel("Llid", "Path id");
//  		else
//  		    setLabel("Llid", "-");
//  	    }
	    cbg = guimap["Slid"] as JComboBox<*>?
	    if (cb === cbg) {
		val tf = guimap["lid"] as JTextField?
		updTF(tf!!, cbg)
	    }
	} catch (ex: ClassCastException) {
	    OmegaContext.sout_log.getLogger().info("ERR: CCE $ex")
	}
    }

    fun updTrigger(ch: JCheckBox?) {
	try {
	    val chg: JCheckBox?
	    chg = guimap["Slid"] as JCheckBox?
	} catch (ex: ClassCastException) {
	    OmegaContext.sout_log.getLogger().info("ERR: CCE $ex")
	}
    }

    val element: Element
	get() {
	    val el = Element("test_prop")
	    var pel = Element("test")
	    pel.addAttr("kind", "pre")
	    pel.addAttr("ord", "1")
	    pel.addAttr("text", (guimap["pret1"] as JTextField?)!!.text)
	    el.add(pel)
	    pel = Element("test")
	    pel.addAttr("kind", "pre")
	    pel.addAttr("ord", "2")
	    pel.addAttr("text", (guimap["pret2"] as JTextField?)!!.text)
	    el.add(pel)
	    pel = Element("test")
	    pel.addAttr("kind", "post")
	    pel.addAttr("ord", "1")
	    pel.addAttr("text", (guimap["postt1"] as JTextField?)!!.text)
	    el.add(pel)
	    pel = Element("test")
	    pel.addAttr("kind", "post")
	    pel.addAttr("ord", "2")
	    pel.addAttr("text", (guimap["postt2"] as JTextField?)!!.text)
	    el.add(pel)
	    return el
	}

    companion object {
	const val COL_MEDIA = 0
	const val COL_FOOBAR = 1
	const val COL_ACT1 = 2
	const val COL_ACT2 = 3
	const val OMEGA_BUNDLE_MANIFEST = "bundle.omega_manifest"
	var targetCombinationsBuilder = TargetCombinations.Builder()
    }
}
