package com.femtioprocent.omega.util

import com.femtioprocent.omega.OmegaContext
import java.io.File
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * InvokeExternBrowser (original name: BrowserLauncher) is a class that provides one static method, openURL, which opens the default
 * web browser for the current user of the system to the given URL.  It may support other
 * protocols depending on the system -- mailto, ftp, etc. -- but that has not been rigorously
 * tested and is not guaranteed to work.
 *
 *
 * Yes, this is platform-specific code, and yes, it may rely on classes on certain platforms
 * that are not part of the standard JDK.  What we're trying to do, though, is to take something
 * that's frequently desirable but inherently platform-specific -- opening a default browser --
 * and allow programmers (you, for example) to do so without worrying about dropping into native
 * code or doing anything else similarly evil.
 *
 *
 * Anyway, this code is completely in Java and will run on all JDK 1.1-compliant systems without
 * modification or a need for additional libraries.  All classes that are required on certain
 * platforms to allow this to run are dynamically loaded at runtime via reflection and, if not
 * found, will not cause this to do anything other than returning an error when opening the
 * browser.
 *
 *
 * There are certain system requirements for this class, as it's running through LessonRuntimeAppl.exec(),
 * which is Java's way of making a native system call.  Currently, this requires that a Macintosh
 * have a Finder which supports the GURL event, which is true for Mac OS 8.0 and 8.1 systems that
 * have the Internet Scripting AppleScript dictionary installed in the Scripting Additions folder
 * in the Extensions folder (which is installed by default as far as I know under Mac OS 8.0 and
 * 8.1), and for all Mac OS 8.5 and later systems.  On Windows, it only runs under Win32 systems
 * (Windows 95, 98, and NT 4.0, as well as later versions of all).  On other systems, this drops
 * back from the inherently platform-sensitive concept of a default browser and simply attempts
 * to launch Netscape via a shell command.
 *
 *
 * This code is Copyright 1999-2001 by Eric Albert (ejalbert@cs.stanford.edu) and may be
 * redistributed or modified in any form without restrictions as long as the portion of this
 * comment from this paragraph through the end of the comment is not removed.  The author
 * requests that he be notified of any application, applet, or other binary that makes use of
 * this code, but that's more out of curiosity than anything and is not required.  This software
 * includes no warranty.  The author is not repsonsible for any loss of data or functionality
 * or any adverse or unexpected effects of using this software.
 *
 *
 * Credits:
 * <br></br>Steven Spencer, JavaWorld magazine ([Java Tip 66](http://www.javaworld.com/javaworld/javatips/jw-javatip66.html))
 * <br></br>Thanks also to Ron B. Yeh, Eric Shapiro, Ben Engber, Paul Teitlebaum, Andrea Cantatore,
 * Larry Barowski, Trevor Bedzek, Frank Miedrich, and Ron Rabakukk
 *
 * @author Eric Albert ([ejalbert@cs.stanford.edu](mailto:ejalbert@cs.stanford.edu))
 * @version 1.4b1 (Released June 20, 2001)
 */
object InvokeExternBrowser {
    /**
     * The Java virtual machine that we are running on.  Actually, in most cases we only care
     * about the operating system, but some operating systems require us to switch on the VM.
     */
    private var jvm = 0

    /**
     * The browser for the system
     */
    private var browser: Any? = null

    /**
     * Caches whether any classes, methods, and fields that are not part of the JDK and need to
     * be dynamically loaded at runtime loaded successfully.
     *
     *
     * Note that if this is `false`, `openURL()` will always return an
     * IOException.
     */
    private var loadedWithoutErrors = false

    /**
     * The com.apple.mrj.MRJFileUtils class
     */
    private var mrjFileUtilsClass: Class<*>? = null

    /**
     * The com.apple.mrj.MRJOSType class
     */
    private var mrjOSTypeClass: Class<*>? = null

    /**
     * The com.apple.MacOS.AEDesc class
     */
    private var aeDescClass: Class<*>? = null

    /**
     * The <init>(int) method of com.apple.MacOS.AETarget
    </init> */
    private var aeTargetConstructor: Constructor<*>? = null

    /**
     * The <init>(int, int, int) method of com.apple.MacOS.AppleEvent
    </init> */
    private var appleEventConstructor: Constructor<*>? = null

    /**
     * The <init>(String) method of com.apple.MacOS.AEDesc
    </init> */
    private var aeDescConstructor: Constructor<*>? = null

    /**
     * The findFolder method of com.apple.mrj.MRJFileUtils
     */
    private var findFolder: Method? = null

    /**
     * The getFileCreator method of com.apple.mrj.MRJFileUtils
     */
    private var getFileCreator: Method? = null

    /**
     * The getFileType method of com.apple.mrj.MRJFileUtils
     */
    private var getFileType: Method? = null

    /**
     * The openURL method of com.apple.mrj.MRJFileUtils
     */
    private var openURL: Method? = null

    /**
     * The makeOSType method of com.apple.MacOS.OSUtils
     */
    private var makeOSType: Method? = null

    /**
     * The putParameter method of com.apple.MacOS.AppleEvent
     */
    private var putParameter: Method? = null

    /**
     * The sendNoReply method of com.apple.MacOS.AppleEvent
     */
    private var sendNoReply: Method? = null

    /**
     * Actually an MRJOSType pointing to the System Folder on a Macintosh
     */
    private var kSystemFolderType: Any? = null

    /**
     * The keyDirectObject AppleEvent parameter type
     */
    private var keyDirectObject: Int? = null

    /**
     * The kAutoGenerateReturnID AppleEvent code
     */
    private var kAutoGenerateReturnID: Int? = null

    /**
     * The kAnyTransactionID AppleEvent code
     */
    private var kAnyTransactionID: Int? = null

    /**
     * The linkage object required for JDirect 3 on Mac OS X.
     */
    private var linkage: Any? = null

    /**
     * The framework to reference on Mac OS X
     */
    private const val JDirect_MacOSX =
	"/System/Library/Frameworks/Carbon.framework/Frameworks/HIToolbox.framework/HIToolbox"

    /**
     * JVM constant for MRJ 2.0
     */
    private const val MRJ_2_0 = 0

    /**
     * JVM constant for MRJ 2.1 or later
     */
    private const val MRJ_2_1 = 1

    /**
     * JVM constant for Java on Mac OS X 10.0 (MRJ 3.0)
     */
    private const val MRJ_3_0 = 3

    /**
     * JVM constant for MRJ 3.1
     */
    private const val MRJ_3_1 = 4

    /**
     * JVM constant for any Windows NT JVM
     */
    private const val WINDOWS_NT = 5

    /**
     * JVM constant for any Windows 9x JVM
     */
    private const val WINDOWS_9x = 6

    /**
     * JVM constant for any other platform
     */
    private const val OTHER = -1

    /**
     * The file type of the Finder on a Macintosh.  Hardcoding "Finder" would keep non-U.SundryUtils. English
     * systems from working properly.
     */
    private const val FINDER_TYPE = "FNDR"

    /**
     * The creator code of the Finder on a Macintosh, which is needed to send AppleEvents to the
     * application.
     */
    private const val FINDER_CREATOR = "MACS"

    /**
     * The name for the AppleEvent type corresponding to a GetURL event.
     */
    private const val GURL_EVENT = "GURL"

    /**
     * The first parameter that needs to be passed into LessonRuntimeAppl.exec() to open the default web
     * browser on Windows.
     */
    private const val FIRST_WINDOWS_PARAMETER = "/c"

    /**
     * The second parameter for LessonRuntimeAppl.exec() on Windows.
     */
    private const val SECOND_WINDOWS_PARAMETER = "start"

    /**
     * The third parameter for LessonRuntimeAppl.exec() on Windows.  This is a "title"
     * parameter that the command line expects.  Setting this parameter allows
     * URLs containing spaces to work.
     */
    private const val THIRD_WINDOWS_PARAMETER = "\"\""

    /**
     * The shell parameters for Netscape that opens a given URL in an already-open copy of Netscape
     * on many command-line systems.
     */
    private const val NETSCAPE_REMOTE_PARAMETER = "-remote"
    private const val NETSCAPE_OPEN_PARAMETER_START = "'openURL("
    private const val NETSCAPE_OPEN_PARAMETER_END = ")'"

    /**
     * The message from any exception thrown throughout the initialization process.
     */
    private var errorMessage: String? = null

    /**
     * An initialization block that determines the operating system and loads the necessary
     * runtime data.
     */
    init {
	loadedWithoutErrors = true
	val osName = System.getProperty("os.name")
	if (osName.startsWith("Mac OS")) {
	    val mrjVersion = System.getProperty("mrj.version")
	    val majorMRJVersion = mrjVersion.substring(0, 3)
	    try {
		val version = java.lang.Double.valueOf(majorMRJVersion)
		if (version == 2.0) {
		    jvm = MRJ_2_0
		} else if (version >= 2.1 && version < 3) {
		    // Assume that all 2.x versions of MRJ work the same.  MRJ 2.1 actually
		    // works via LessonRuntimeAppl.exec() and 2.2 supports that but has an openURL() method
		    // as well that we currently ignore.
		    jvm = MRJ_2_1
		} else if (version == 3.0) {
		    jvm = MRJ_3_0
		} else if (version >= 3.1) {
		    // Assume that all 3.1 and later versions of MRJ work the same.
		    jvm = MRJ_3_1
		} else {
		    loadedWithoutErrors = false
		    errorMessage = "Unsupported MRJ version: $version"
		}
	    } catch (nfe: NumberFormatException) {
		loadedWithoutErrors = false
		errorMessage = "Invalid MRJ version: $mrjVersion"
	    }
	} else if (osName.startsWith("Windows")) {
	    if (osName.indexOf("9") != -1) {
		jvm = WINDOWS_9x
	    } else {
		jvm = WINDOWS_NT
	    }
	} else {
	    jvm = OTHER
	}
	if (loadedWithoutErrors) {        // if we haven't hit any errors yet
	    loadedWithoutErrors = loadClasses()
	}
    }

    /**
     * Called by a static initializer to load any classes, fields, and methods required at runtime
     * to locate the user's web browser.
     *
     * @return `true` if all intialization succeeded
     * `false` if any portion of the initialization failed
     */
    private fun loadClasses(): Boolean {
	when (jvm) {
	    MRJ_2_0 -> try {
		val aeTargetClass = Class.forName("com.apple.MacOS.AETarget")
		val osUtilsClass = Class.forName("com.apple.MacOS.OSUtils")
		val appleEventClass = Class.forName("com.apple.MacOS.AppleEvent")
		val aeClass = Class.forName("com.apple.MacOS.ae")
		aeDescClass = Class.forName("com.apple.MacOS.AEDesc")
		aeTargetConstructor = aeTargetClass.getDeclaredConstructor(
		    *arrayOf<Class<*>?>(
			Int::class.javaPrimitiveType
		    )
		)
		appleEventConstructor = appleEventClass.getDeclaredConstructor(
		    *arrayOf(
			Int::class.javaPrimitiveType,
			Int::class.javaPrimitiveType,
			aeTargetClass,
			Int::class.javaPrimitiveType,
			Int::class.javaPrimitiveType
		    )
		)
		aeDescConstructor = aeDescClass!!.getDeclaredConstructor(
		    *arrayOf<Class<*>>(
			String::class.java
		    )
		)
		makeOSType = osUtilsClass.getDeclaredMethod(
		    "makeOSType", *arrayOf<Class<*>>(
			String::class.java
		    )
		)
		putParameter = appleEventClass.getDeclaredMethod(
		    "putParameter", *arrayOf(
			Int::class.javaPrimitiveType, aeDescClass
		    )
		)
		sendNoReply = appleEventClass.getDeclaredMethod("sendNoReply", *arrayOf())
		val keyDirectObjectField = aeClass.getDeclaredField("keyDirectObject")
		keyDirectObject = keyDirectObjectField[null] as Int
		val autoGenerateReturnIDField = appleEventClass.getDeclaredField("kAutoGenerateReturnID")
		kAutoGenerateReturnID = autoGenerateReturnIDField[null] as Int
		val anyTransactionIDField = appleEventClass.getDeclaredField("kAnyTransactionID")
		kAnyTransactionID = anyTransactionIDField[null] as Int
	    } catch (cnfe: ClassNotFoundException) {
		errorMessage = cnfe.message
		return false
	    } catch (nsme: NoSuchMethodException) {
		errorMessage = nsme.message
		return false
	    } catch (nsfe: NoSuchFieldException) {
		errorMessage = nsfe.message
		return false
	    } catch (iae: IllegalAccessException) {
		errorMessage = iae.message
		return false
	    }

	    MRJ_2_1 -> try {
		mrjFileUtilsClass = Class.forName("com.apple.mrj.MRJFileUtils")
		mrjOSTypeClass = Class.forName("com.apple.mrj.MRJOSType")
		val systemFolderField = mrjFileUtilsClass!!.getDeclaredField("kSystemFolderType")
		kSystemFolderType = systemFolderField[null]
		findFolder = mrjFileUtilsClass!!.getDeclaredMethod("findFolder", *arrayOf(mrjOSTypeClass))
		getFileCreator = mrjFileUtilsClass!!.getDeclaredMethod(
		    "getFileCreator", *arrayOf<Class<*>>(
			File::class.java
		    )
		)
		getFileType = mrjFileUtilsClass!!.getDeclaredMethod(
		    "getFileType", *arrayOf<Class<*>>(
			File::class.java
		    )
		)
	    } catch (cnfe: ClassNotFoundException) {
		errorMessage = cnfe.message
		return false
	    } catch (nsfe: NoSuchFieldException) {
		errorMessage = nsfe.message
		return false
	    } catch (nsme: NoSuchMethodException) {
		errorMessage = nsme.message
		return false
	    } catch (se: SecurityException) {
		errorMessage = se.message
		return false
	    } catch (iae: IllegalAccessException) {
		errorMessage = iae.message
		return false
	    }

	    MRJ_3_0 -> try {
		val linker = Class.forName("com.apple.mrj.jdirect.Linker")
		val constructor = linker.getConstructor(
		    *arrayOf<Class<*>>(
			Class::class.java
		    )
		)
		linkage = constructor.newInstance(*arrayOf<Any>(InvokeExternBrowser::class.java))
	    } catch (cnfe: ClassNotFoundException) {
		errorMessage = cnfe.message
		return false
	    } catch (nsme: NoSuchMethodException) {
		errorMessage = nsme.message
		return false
	    } catch (ite: InvocationTargetException) {
		errorMessage = ite.message
		return false
	    } catch (ie: InstantiationException) {
		errorMessage = ie.message
		return false
	    } catch (iae: IllegalAccessException) {
		errorMessage = iae.message
		return false
	    }

	    MRJ_3_1 -> try {
		mrjFileUtilsClass = Class.forName("com.apple.mrj.MRJFileUtils")
		openURL = mrjFileUtilsClass!!.getDeclaredMethod(
		    "openURL", *arrayOf<Class<*>>(
			String::class.java
		    )
		)
	    } catch (cnfe: ClassNotFoundException) {
		errorMessage = cnfe.message
		return false
	    } catch (nsme: NoSuchMethodException) {
		errorMessage = nsme.message
		return false
	    }

	    else -> {}
	}
	return true
    }

    /**
     * Attempts to locate the default web browser on the local system.  Caches results so it
     * only locates the browser once for each use of this class per JVM instance.
     *
     * @return The browser for the system.  Note that this may not be what you would consider
     * to be a standard web browser; instead, it's the application that gets called to
     * open the default web browser.  In some cases, this will be a non-String object
     * that provides the means of calling the default browser.
     */
    private fun locateBrowser(): Any? {
	if (browser != null) {
	    return browser
	}
	when (jvm) {
	    MRJ_2_0 -> {
		return try {
		    val finderCreatorCode = makeOSType!!.invoke(
			null,
			*arrayOf<Any>(FINDER_CREATOR)
		    ) as Int
		    val aeTarget =
			aeTargetConstructor!!.newInstance(*arrayOf<Any>(finderCreatorCode))
		    val gurlType = makeOSType!!.invoke(
			null,
			*arrayOf<Any>(GURL_EVENT)
		    ) as Int
		    // Don't dep_set browser = appleEvent because then the next time we call
		    // locateBrowser(), we'll get the same AppleEvent, to which we'll already have
		    // added the relevant parameter. Instead, regenerate the AppleEvent every time.
		    // There's probably a way to do this better; if any has any ideas, please let
		    // me know.
		    appleEventConstructor!!.newInstance(
			gurlType,
			gurlType,
			aeTarget,
			kAutoGenerateReturnID,
			kAnyTransactionID
		    )
		} catch (iae: IllegalAccessException) {
		    browser = null
		    errorMessage = iae.message
		    browser
		} catch (ie: InstantiationException) {
		    browser = null
		    errorMessage = ie.message
		    browser
		} catch (ite: InvocationTargetException) {
		    browser = null
		    errorMessage = ite.message
		    browser
		}
		val systemFolder: File
		try {
		    systemFolder = findFolder!!.invoke(null, *arrayOf(kSystemFolderType)) as File
		} catch (iare: IllegalArgumentException) {
		    browser = null
		    errorMessage = iare.message
		    return browser
		} catch (iae: IllegalAccessException) {
		    browser = null
		    errorMessage = iae.message
		    return browser
		} catch (ite: InvocationTargetException) {
		    browser = null
		    errorMessage = ite.targetException.javaClass.toString() + ": " + ite.targetException.message
		    return browser
		}
		val systemFolderFiles = systemFolder.list()
		// Avoid a FilenameFilter because that can't be stopped mid-list
		var i = 0
		while (i < systemFolderFiles.size) {
		    try {
			val file = File(systemFolder, systemFolderFiles[i])
			if (!file.isFile) {
			    i++
			    continue
			}
			// We're looking for a file with a creator code of 'MACS' and
			// a type of 'FNDR'.  Only requiring the type results in non-Finder
			// applications being picked up on certain Mac OS 9 systems,
			// especially German ones, and sending a GURL event to those
			// applications results in a logout under Multiple Users.
			val fileType = getFileType!!.invoke(null, *arrayOf<Any>(file))
			if (FINDER_TYPE == fileType.toString()) {
			    val fileCreator = getFileCreator!!.invoke(null, *arrayOf<Any>(file))
			    if (FINDER_CREATOR == fileCreator.toString()) {
				browser = file.toString() // Actually the Finder, but that's OK
				return browser
			    }
			}
		    } catch (iare: IllegalArgumentException) {
			browser = browser
			errorMessage = iare.message
			return null
		    } catch (iae: IllegalAccessException) {
			browser = null
			errorMessage = iae.message
			return browser
		    } catch (ite: InvocationTargetException) {
			browser = null
			errorMessage = ite.targetException.javaClass.toString() + ": " + ite.targetException.message
			return browser
		    }
		    i++
		}
		browser = null
	    }

	    MRJ_2_1 -> {
		val systemFolder: File
		try {
		    systemFolder = findFolder!!.invoke(null, *arrayOf(kSystemFolderType)) as File
		} catch (iare: IllegalArgumentException) {
		    browser = null
		    errorMessage = iare.message
		    return browser
		} catch (iae: IllegalAccessException) {
		    browser = null
		    errorMessage = iae.message
		    return browser
		} catch (ite: InvocationTargetException) {
		    browser = null
		    errorMessage = ite.targetException.javaClass.toString() + ": " + ite.targetException.message
		    return browser
		}
		val systemFolderFiles = systemFolder.list()
		var i = 0
		while (i < systemFolderFiles.size) {
		    try {
			val file = File(systemFolder, systemFolderFiles[i])
			if (!file.isFile) {
			    i++
			    continue
			}
			val fileType = getFileType!!.invoke(null, *arrayOf<Any>(file))
			if (FINDER_TYPE == fileType.toString()) {
			    val fileCreator = getFileCreator!!.invoke(null, *arrayOf<Any>(file))
			    if (FINDER_CREATOR == fileCreator.toString()) {
				browser = file.toString()
				return browser
			    }
			}
		    } catch (iare: IllegalArgumentException) {
			browser = browser
			errorMessage = iare.message
			return null
		    } catch (iae: IllegalAccessException) {
			browser = null
			errorMessage = iae.message
			return browser
		    } catch (ite: InvocationTargetException) {
			browser = null
			errorMessage = ite.targetException.javaClass.toString() + ": " + ite.targetException.message
			return browser
		    }
		    i++
		}
		browser = null
	    }

	    MRJ_3_0, MRJ_3_1 -> browser = "" // Return something non-null
	    WINDOWS_NT -> browser = "cmd.exe"
	    WINDOWS_9x -> browser = "command.com"
	    OTHER -> browser = "mozilla"
	    else -> browser = "mozilla"
	}
	return browser
    }

    /**
     * Attempts to open the default web browser to the given URL.
     *
     * @param url The URL to open
     * @throws IOException If the web browser could not be located or does not run
     */
    @Throws(IOException::class)
    fun openURL(url: String) {
	if (!loadedWithoutErrors) {
	    throw IOException("Exception in finding browser: " + errorMessage)
	}
	var browser: Any? = locateBrowser()
	    ?: throw IOException("Unable to locate browser: " + errorMessage)
	when (jvm) {
	    MRJ_2_0 -> {
		var aeDesc: Any? = null
		try {
		    aeDesc = aeDescConstructor!!.newInstance(*arrayOf<Any>(url))
		    putParameter!!.invoke(browser, *arrayOf(keyDirectObject, aeDesc))
		    sendNoReply!!.invoke(browser, *arrayOf())
		} catch (ite: InvocationTargetException) {
		    throw IOException("InvocationTargetException while creating AEDesc: " + ite.message)
		} catch (iae: IllegalAccessException) {
		    throw IOException("IllegalAccessException while building AppleEvent: " + iae.message)
		} catch (ie: InstantiationException) {
		    throw IOException("InstantiationException while creating AEDesc: " + ie.message)
		} finally {
		    aeDesc = null // Encourage it to get disposed if it was created
		    browser = null // Ditto
		}
	    }

	    MRJ_2_1 -> Runtime.getRuntime().exec(arrayOf(browser as String, url))
	    MRJ_3_0 -> {
		val instance = IntArray(1)
		var result = ICStart(instance, 0)
		if (result == 0) {
		    val selectionStart = intArrayOf(0)
		    val urlBytes = url.toByteArray()
		    val selectionEnd = intArrayOf(urlBytes.size)
		    result = ICLaunchURL(
			instance[0], byteArrayOf(0), urlBytes,
			urlBytes.size, selectionStart,
			selectionEnd
		    )
		    if (result == 0) {
			// Ignore the return value; the URL was launched successfully
			// regardless of what happens here.
			ICStop(instance)
		    } else {
			throw IOException("Unable to launch URL: $result")
		    }
		} else {
		    throw IOException("Unable to create an Internet OmegaConfig instance: $result")
		}
	    }

	    MRJ_3_1 -> try {
		openURL!!.invoke(null, *arrayOf<Any>(url))
	    } catch (ite: InvocationTargetException) {
		throw IOException("InvocationTargetException while calling openURL: " + ite.message)
	    } catch (iae: IllegalAccessException) {
		throw IOException("IllegalAccessException while calling openURL: " + iae.message)
	    }

	    WINDOWS_NT, WINDOWS_9x -> {
		// Add quotes around the URL to allow ampersands and other special
		// characters to work.
		val process = Runtime.getRuntime().exec(
		    arrayOf(
			browser as String,
			FIRST_WINDOWS_PARAMETER,
			SECOND_WINDOWS_PARAMETER,
			THIRD_WINDOWS_PARAMETER,
			'"'.toString() + url + '"'
		    )
		)
		// This avoids a memory leak on some versions of Java on Windows.
		// That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
		try {
		    process.waitFor()
		    process.exitValue()
		} catch (ie: InterruptedException) {
		    throw IOException("InterruptedException while launching browser: " + ie.message)
		}
	    }

	    OTHER -> {
		// Assume that we're on Unix and that Netscape is installed

		// First, attempt to open the URL in a currently running session of Netscape
		val process = Runtime.getRuntime().exec(
		    arrayOf<String>(
			browser as String,
			NETSCAPE_REMOTE_PARAMETER,
			NETSCAPE_OPEN_PARAMETER_START +
				url +
				NETSCAPE_OPEN_PARAMETER_END
		    )
		)
		try {
		    val exitCode: Int = process.waitFor()
		    if (exitCode != 0) {        // if Netscape was not open
			Runtime.getRuntime().exec(arrayOf(browser, url))
		    }
		} catch (ie: InterruptedException) {
		    throw IOException("InterruptedException while launching browser: " + ie.message)
		}
	    }

	    else ->                 // This should never occur, but if it does, we'll try the simplest thing possible
		Runtime.getRuntime().exec(arrayOf(browser as String, url))
	}
    }

    /**
     * Methods required for Mac OS X.  The presence of native methods does not cause
     * any problems on other platforms.
     */
    private external fun ICStart(instance: IntArray, signature: Int): Int
    private external fun ICStop(instance: IntArray): Int
    private external fun ICLaunchURL(
	instance: Int, hint: ByteArray, data: ByteArray, len: Int,
	selectionStart: IntArray, selectionEnd: IntArray
    ): Int

    fun show_if(url_s: String): Boolean {
	if (OmegaContext.extern_help_browser) {
	    try {
		openURL(url_s)
		OmegaContext.sout_log.getLogger().info("ERR: " + "done...")
	    } catch (ex: IOException) {
		OmegaContext.sout_log.getLogger().info("ERR: Can't open url $url_s $ex")
		return false
	    }
	    return true
	}
	return false
    }

    @JvmStatic
    fun main(args: Array<String>) {
	try {
	    openURL(args[0])
	} catch (ex: Exception) {
	}
    }
}
