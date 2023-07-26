package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaConfig.isKeyESC
import com.femtioprocent.omega.OmegaConfig.isKeyNext
import com.femtioprocent.omega.OmegaConfig.isKeySelect
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.lessonLang
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.LessonContext
import com.femtioprocent.omega.lesson.repository.LessonItem
import com.femtioprocent.omega.lesson.repository.Locator
import com.femtioprocent.omega.swing.ScaledImageIcon.createImageIcon
import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.*
import java.io.File
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel

/*
--gapE---|=====|--gapB--|=========|---gapE---




 */
class LessonMainCanvas(l_ctxt: LessonContext?) : BaseCanvas(l_ctxt!!) {
    private val item_fo = Font("Arial", Font.PLAIN, ( /*h*/20 * 0.8).toInt())
    private val trgt_fo = Font("Arial", Font.PLAIN, ( /*h*/20 * 0.8).toInt())
    private val trgtA_fo = Font("Arial", Font.PLAIN, ( /*h*/20 * 0.5).toInt())
    var title: JLabel? = null
    var parent: JLabel? = null
    @JvmField
    val lesson: Array<ImageAreaJB?>? = arrayOfNulls(10)
    var locator = Locator()
    var modeIsTest = false
    fun setModeIsTest(b: Boolean) {
	modeIsTest = b
	updLessons()
    }

    fun updLessons() {
	for (i in lesson!!.indices) {
	    if (lesson[i] != null) {
		val litm = lesson[i]!!.o as LessonItem?
		lesson[i]!!.isEnabled = !(modeIsTest && litm!!.isStory)
	    }
	}
    }

    override val panelName: String
	get() = "main"

    override fun resized() {
	for (i in lesson!!.indices) {
	    if (lesson[i] != null) {
		remove(lesson[i])
		lesson[i] = null
	    }
	}
	populate(false)
    }

    protected fun resized2() {
	populate(true)
    }

    fun reload() {
	if (buttons[0] != null) populate(true)
    }

    var lessonBase: String? = null
    fun setLessonBase(bs: String?): Int {
	lessonBase = bs
	resized2()
	val I = bs_hm["" + bs] ?: return 0
	return 0
    }

    fun addLessonBase(bs: String, ord: Int) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "add " + this.bs + ' ' + bs);
	bs_hm["" + lessonBase] = ord
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "<!add> LBhm: " + bs_hm);
	if (lessonBase != null) lessonBase = lessonBase + '/' + bs else lessonBase = bs
	resized2()
	requestFocus()
	mkButtons()
    }

    fun tellLessonBase(bs: String?, ord: Int) {
	bs_hm["" + lessonBase] = ord
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "<!tell> LBhm: " + bs_hm);
    }

    private fun hasFile(): Boolean {
	val lb = lessonBase
	val file: File
	file =
	    if (lb == null) File( /*Locator.fbase + */omegaAssets("lesson-" + lessonLang + "/active/story")) // LESSON-DIR-A
	    else File( /*Locator.fbase + */omegaAssets("lesson-" + lessonLang + "/active/" + lb + "/story")) // LESSON-DIR-A
	return file.exists()
    }

    val isStory: Boolean
	get() = !modeIsTest && hasFile()

    fun mkButtons() {
	if (isStory) populateButtons(
	    arrayOf("Finish", "", "", "", "Load Story"),
	    arrayOf("quit", "", "", "", "read_story")
	) else populateButtons(
	    arrayOf("Finish", "", "", "", ""), arrayOf("quit", "", "", "", "")
	)
	updLeftButton()
    }

    fun populate(requestF: Boolean) {
	val h1 = 0.25
	val h2 = 0.02
	val h3 = 0.19
	val hh = (1.0 - h1 - 2 * h2 - h3) / 2
	val v0 = gY(h2)
	val v1 = gY(h1)
	val v2 = gY(h1 + hh + h2)
	val v3 = gY(h1 + hh + h2 + hh + h2)
	val l1 = 0.1
	val l2 = 0.2
	val l25 = 0.30
	val l3 = 0.4
	val l4 = 0.6
	val l5 = 0.8
	val yf1 = 0.2
	val yf2 = 0.2 + 0.3
	val yf3 = 0.2 + 0.6
	val bw = gX(hh * 0.75)
	val bh = gY(hh * 0.9)
	val bh2 = gY(hh * 0.55)
	val fs = caH / 48
	fo = Font("arial", Font.PLAIN, fs)
	if (title == null) {
	    title = JLabel(t(""))
	    add(title)
	}
	if (parent == null) {
	    parent = JLabel(t(""))
	    add(parent)
	}
	mkButtons()

//log	OmegaContext.sout_log.getLogger().info(":--: " + "base is " + bs);
	val lessons_name: Array<String?>?
	lessons_name = if (lessonBase == null) locator.allLessonsInDir else locator.getAllLessonsInDir(
	    lessonBase!!
	)
	val w = caW / 2
	val h = caH / 5
	val imic = createImageIcon(
	    this,
	    "toolbarButtonGraphics/omega/omega_title.png",
	    w,
	    h
	) ?: return
	if (imic != null) {
	    title!!.setSize(imic.image.getWidth(null), imic.image.getHeight(null))
	    title!!.setLocation(gX(l25) - imic.image.getHeight(null), v0)
	    title!!.icon = imic
	}
	set(title!!)
	if (lesson != null && lessons_name != null) {

// 	    buttons[0].requestFocus();  // focus are lost when remove buttons
	    for (i in lesson.indices) {
		if (lesson[i] != null) {
		    lesson[i]!!.isVisible = false
		    //---		    remove(lesson[i]);
//		    lesson[i] = null;
		}
		repaint()
	    }
	    var to = lessons_name.size
	    if (to >= 10) to = 10
	    for (i in 0 until to) {
		val litm = LessonItem(lessons_name[i]!!)
		if (i == 0) {
		    var imn = litm.getLessonParentImage("_parent")
		    var imicp = createImageIcon(
			this,
			imn,
			w / 5,
			h
		    )
		    if (imicp == null) {
			imn = litm.getLessonParentImage("_enter")
			imicp = createImageIcon(
			    this,
			    imn,
			    w / 5,
			    h
			)
		    }
		    if (imicp == null) {
			imn = litm.getLessonParentImage("")
			imicp = createImageIcon(
			    this,
			    imn,
			    w / 5,
			    h
			)
		    }
		    OmegaContext.sout_log.getLogger().info(":--: PARENT $i $imn $imicp")
		    if (imicp != null) {
			parent!!.setSize(imicp.image.getWidth(null), imicp.image.getHeight(null))
			parent!!.setLocation(gX(l25) + w + 10, v0)
			parent!!.icon = imicp
			set(parent!!)
		    } else {
			parent!!.icon = null
		    }
		}
		val dispName = litm.getLessonDisplayName(lessonLang)
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "recreate le " + i + ' ' + litm);
		var l: ImageAreaJB
		if (lesson[i] == null) {
		    lesson[i] = ImageAreaJB(
			"",
			i,
			bw,
			bh - 20,
			dispName
		    )
		    add(lesson[i])
		}
		if (litm.isDir) {
		    lesson[i]!!.setNew( /*T.t("Lesson") + ' ' + */litm.lessonShortName,
			litm.lessonImageFileName,
			litm.getLessonImageFileName("_enter"),
			litm,
			dispName
		    )
		} else {
		    lesson[i]!!.setNew( /*T.t("Lesson") + ' ' + */litm.lessonShortName,
			litm.lessonImageFileName,
			litm.getLessonImageFileName("_enter"),
			litm,
			dispName
		    )
		}
		lesson[i]!!.isEnabled = !(modeIsTest && litm.isStory)
	    }
	}
	for (i in lesson!!.indices) {
	    if (lesson[i] != null) {
		val l = lesson[i]
		l!!.setSize(bw - 15, bh)
		l.setLocation(10 + i % 5 * (bw + 3), if (i < 5) v1 else v2)
		setAlt(l)
	    }
	}
	updLeftButton()
	requestFocusOrd(0)
    }

    fun updLeftButton() {
	val b: JButton
	b = buttons[0]!!
	if (lessonBase == null) {
	    b.text = t("Logout")
	    setJBIcon(b, "toolbarButtonGraphics/omega/main_logout.png")
	} else {
	    b.text = t("Up level")
	    setJBIcon(b, "toolbarButtonGraphics/omega/main_uplevel.png")
	}
    }

    var current_ix = 0
    var current_iy = 0
    fun gotoBox(ix: Int, iy: Int) {}
    fun gotoBoxRel(dx: Int, dy: Int) {}
    fun selectBox() {}
    override fun populateGUI() {}
    override fun ownKeyCode(kc: Int, is_shift: Boolean): Boolean {
	if (Lesson.skip_F) return true
	if (isKeyESC(kc)) {
	    om_msg_mgr.fire("button main:quit")
	    return true
	}
	if (isKeyNext(kc)) {
	    if (is_shift) setPrevRed() else setNextRed()
	    return true
	}
	if (isKeySelect(kc)) {
	    if (last_focus_ord >= 0 && last_focus_ord <= 9) lesson!![last_focus_ord]!!.doClick() else super.ownKeyCode(
		kc,
		is_shift
	    )
	    return true
	}
	return super.ownKeyCode(kc, is_shift)
    }

    override fun paintComponent(g: Graphics) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "++++++++++ repaint LMC ");
	//SundryUtils.m_sleep(100);
	if (title == null) populate(false)
	val g2 = g as Graphics2D
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	drawBG(g2)
	g.setColor(Color.black)
	val fo = g.getFont()
    }

    override fun enter() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "LeMa-Enter");
	super.enter()
	val I = bs_hm["" + lessonBase]
	setNoRed(0)
    }

    override fun leave() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "LeMa-Leave");
	super.leave()
    }

    var red_stack: Stack<*> = Stack<Any?>()
    fun setRedClear() {
	red_stack = Stack<Any?>()
	requestFocus()
	requestFocusOrd(0)
    }

    fun setRedPop(): Int {
	if (red_stack.size == 0) {
	    requestFocus()
	    return 0
	}
	val ord = red_stack.pop() as Int
	requestFocus()
	requestFocusOrd(ord)
	return ord
    }

    fun setRedPush(ord: Int) {
	red_stack.push(ord as Nothing?)
    }

    var last_focus_ord = 0

    init {
	focus_list = CycleList(-1)
    }

    // 0 1 2 3 4
    // 5 6 7 8 9
    // 10     11
    fun requestFocusOrd(ord: Int) {
	for (i in 0..9) if (lesson!![i] != null) if (ord == i) {
	    lesson[ord]!!.border = BorderFactory.createLineBorder(Color(242, 80, 80), 5)
	    lesson[ord]!!.showEnter()
	} else if (i == last_focus_ord) {
	    lesson[i]!!.border = BorderFactory.createLineBorder(Color(80, 80, 80), 5)
	    lesson[i]!!.showNormal()
	}
	last_focus_ord = ord
    }

    override fun setNextRed() {
	val how_many = howManyLessonButtons()
	var ord = last_focus_ord + 1
	if (ord == how_many) {
	    super.setRed(0) // BAD
	    requestFocusOrd(10)
	    return
	}
	if (isStory && ord == 11) {
	    super.setRed(4) // BAD
	    requestFocusOrd(11)
	    return
	}
	if (ord > 9 || lesson!![ord] == null || !lesson[ord]!!.isVisible) {
	    ord = 0
	}
	setAllNoRed()
	requestFocusOrd(ord)
    }

    override fun setRed(ix: Int) {
	if (ix > 9 || lesson!![ix] == null || !lesson[ix]!!.isVisible) {
	    return
	}
	setNoRed(0)
	requestFocusOrd(ix)
    }

    override fun setPrevRed() {
	var ord = last_focus_ord - 1
	if (ord < 0) for (i in 9 downTo 1) {
	    if (lesson!![i] != null || lesson[i]!!.isVisible) {
		ord = i
		break
	    }
	}
	setNoRed(0)
	requestFocusOrd(ord)
    }

    fun howManyLessonButtons(): Int {
	for (i in lesson!!.indices) {
	    if (lesson[i] == null || !lesson[i]!!.isVisible) return i
	}
	return lesson.size
    }

    companion object {
	const val gapB = 5f
	const val gapE = 5f
	const val tgH = 8f
	var bs_hm = HashMap<String, Int>()
    }
}
