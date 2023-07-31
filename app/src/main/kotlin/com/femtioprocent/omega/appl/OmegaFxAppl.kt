package com.femtioprocent.omega.appl

import com.femtioprocent.omega.OmegaContext.Companion.getMediaFile
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import de.codecentric.centerdevice.MenuToolkit
import javafx.application.Application
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.shape.Circle
import javafx.stage.Stage

class OmegaFxAppl : Application() {
    fun start2(primaryStage: Stage) {
	primaryStage.title = "Hello World!"
	val btn = Button()
	btn.text = "Say 'Hello World'"
	btn.onAction = EventHandler { event: ActionEvent? -> Log.getLogger().info("Hello World!") }
	val root = StackPane()
	root.children.add(btn)
	primaryStage.scene = Scene(root, 300.0, 250.0)
	primaryStage.show()
    }

    override fun start(stage: Stage) {
	Platform.setImplicitExit(false)
	val tk = MenuToolkit.toolkit()
	val bar = MenuBar()
	val appName = "Omega IS"
	// Application Menu
	// TBD: services menu
	val appMenu = Menu(appName) // Name for appMenu can't be dep_set at
	// LessonRuntimeAppl
	val aboutItem = tk.createAboutMenuItem(appName)
	val prefsItem = MenuItem("Preferences...")
	prefsItem.onAction = EventHandler { event: ActionEvent? -> Log.getLogger().info("prefs clicked") }
	appMenu.items.addAll(
	    aboutItem, SeparatorMenuItem(), prefsItem, SeparatorMenuItem(),
	    tk.createHideMenuItem(appName), tk.createHideOthersMenuItem(), tk.createUnhideAllMenuItem(),
	    SeparatorMenuItem(), tk.createQuitMenuItem(appName)
	)

	// File Menu (items TBD)
	val fileMenu = Menu("File")
	val newItem = MenuItem("New...")
	fileMenu.items.addAll(
	    newItem, SeparatorMenuItem(), tk.createCloseWindowMenuItem(),
	    SeparatorMenuItem(), MenuItem("TBD")
	)

	// Edit (items TBD)
	val editMenu = Menu("Edit")
	editMenu.items.addAll(MenuItem("TBD"))

	// Format (items TBD)
	val formatMenu = Menu("Format")
	formatMenu.items.addAll(MenuItem("TBD"))

	// View Menu (items TBD)
	val viewMenu = Menu("View")
	viewMenu.items.addAll(MenuItem("TBD"))

	// Window Menu
	// TBD standard window menu items
	val windowMenu = Menu("Window")
	windowMenu.items.addAll(
	    tk.createMinimizeMenuItem(), tk.createZoomMenuItem(), tk.createCycleWindowsItem(),
	    SeparatorMenuItem(), tk.createBringAllToFrontItem()
	)

	// Help Menu (items TBD)
	val helpMenu = Menu("Help")
	helpMenu.items.addAll(MenuItem("TBD"))
	bar.menus.addAll(appMenu, fileMenu, editMenu, formatMenu, viewMenu, windowMenu, helpMenu)
	tk.autoAddWindowMenuItems(windowMenu)
	tk.setGlobalMenuBar(bar)
	tk.setGlobalMenuBar(bar)
	Platform.runLater { bar.isUseSystemMenuBar = true }
	val scW = 1000
	val scH = 1000
	val ww = 700
	val hh = 330 + 22 * 3
	val xx = (scW - ww) / 2
	val yy = (scH - hh) / 2
	val circ = Circle(40.0, 40.0, 30.0)
	val root = Group(circ)
	val scene = Scene(root, 700.0, (330 + 22 * 3).toDouble())
	stage.title = "Omega IS"
	val aImname = getMediaFile("default/omega_splash.gif")
	val im = Image("file:$aImname")
	val imView = ImageView()
	imView.image = im
	imView.x = 4.0
	imView.y = 4.0
	root.children.addAll(imView)
	root.children.add(bar)
	stage.scene = scene
	stage.isAlwaysOnTop = true
	stage.show()
	Log.getLogger().info("started")
	Thread {
	    m_sleep(500)
	    Platform.runLater {

//	        stage.setIconified(true);
		stage.hide()
		Thread {
		    m_sleep(500)
		    LessonEditorAppl.main(args)
		    Platform.runLater { stage.show() }
		    m_sleep(500)
		}.start()
	    }
	}.start()
    }

    override fun stop() {
	Log.getLogger().info("stop")
	//	Platform.exit();
    } //    public void paint(Graphics g) {

    //	g.drawImage(im, 0, 0, null);
    //	g.setColor(Color.yellow);
    //	g.drawString(OmegaVersion.getVersion(), 5, 12);
    //	g.drawString(OmegaVersion.getCWD(), 5, 322 + 20 * 0);
    //	g.drawString(OmegaVersion.getJavaVersion(), 5, 322 + 20 * 1);
    //	g.drawString(OmegaVersion.getJavaVendor(), 5, 322 + 20 * 2);
    //	g.drawString(OmegaVersion.getJavaHome(), 5, 322 + 20 * 3);
    //    }
    companion object {
	lateinit var args: Array<String>

	@JvmStatic
	fun main(args: Array<String>) {
	    Companion.args = args
	    launch(*args)
	}
    }
}
