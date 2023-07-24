package com.femtioprocent.omega.util

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import java.io.File
import java.util.*
import javax.swing.filechooser.FileFilter

/*
 * @(#)ExtensionFileFilter.java	1.9 99/04/23
 *
 * Copyright (c) 1998, 1999 by Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */ /**
 * A convenience implementation of FileFilter that filters out
 * all files except for those type extensions that it knows about.
 *
 *
 * Extensions are of the type ".foo", which is typically found on
 * Windows and Unix boxes, but not on Macinthosh. Case is ignored.
 *
 *
 * Example - create a new filter that filerts out all files
 * but gif and jpg image files:
 *
 *
 * JFileChooser chooser = new JFileChooser();
 * ExtensionFileFilter filter = new ExtensionFileFilter(
 * new String{"gif", "jpg"}, "JPEG & GIF Images")
 * chooser.addChoosableFileFilter(filter);
 * chooser.showOpenDialog(this);
 *
 * @author Jeff Dinkins
 * @version 1.9 04/23/99
 */
class ExtensionFileFilter : FileFilter {
    private var filters: Hashtable<String, FileFilter>? = null
    private var description: String? = null
    private var fullDescription: String? = null
    private var useExtensionsInDescription = true
    private var only_ = false

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see .addExtension
     */
    constructor() {
	filters = Hashtable()
	only_ = false
    }

    constructor(multi: Boolean) {
	filters = Hashtable()
	only_ = multi
    }
    /**
     * Creates a file filter that accepts the given file type.
     * Example: new ExtensionFileFilter("jpg", "JPEG Image Images");
     *
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @see .addExtension
     */
    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new ExtensionFileFilter("jpg");
     *
     * @see .addExtension
     */
    @JvmOverloads
    constructor(extension: String?, description: String? = null) : this() {
	extension?.let { addExtension(it) }
	description?.let { setDescription(it) }
    }

    constructor(only_: Boolean, filters: Array<String>) : this(filters, null) {
	this.only_ = only_
    }
    /**
     * Creates a file filter from the given string array and description.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @see .addExtension
     */
    /**
     * Creates a file filter from the given string array.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"});
     *
     *
     * Note that the "." before the extension is not needed adn
     * will be ignored.
     *
     * @see .addExtension
     */
    @JvmOverloads
    constructor(filters: Array<String>, description: String? = null) : this() {
	for (i in filters.indices) {
	    // add filters one by one
	    addExtension(filters[i])
	}
	description?.let { setDescription(it) }
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     *
     *
     * Files that begin with "." are ignored.
     *
     * @see .getExtension
     *
     * @see FileFilter.accepts
     */
    override fun accept(f: File): Boolean {
	if (f != null) {
	    if (f.isDirectory) {
		return true
	    }
	    val extension = getExtension(f)
	    if (extension != null && filters!![getExtension(f)] != null) {
		return if (only_ && fileNameHas(f, '-')) false else true
	    }
	}
	return false
    }

    /**
     * Return the extension portion of the file's name .
     *
     * @see .getExtension
     *
     * @see FileFilter.accept
     */
    fun getExtension(f: File?): String? {
	if (f != null) {
	    val filename = f.name
	    val i = filename.lastIndexOf('.')
	    if (i > 0 && i < filename.length - 1) {
		return filename.substring(i + 1).lowercase(Locale.getDefault())
	    }
	}
	return null
    }

    fun fileNameHas(f: File?, ch: Char): Boolean {
	if (f != null) {
	    val filename = f.name
	    if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: try $filename $ch")
	    return if (filename.indexOf(ch) != -1) true else false
	}
	return false
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     *
     *
     * For Example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     *
     *
     * ExtensionFileFilter filter = new ExtensionFileFilter();
     * filter.addExtension("jpg");
     * filter.addExtension("tif");
     *
     *
     * Note that the "." before the extension is not needed and will be ignored.
     */
    fun addExtension(extension: String) {
	if (filters == null) {
	    filters = Hashtable(5)
	}
	filters!![extension.lowercase(Locale.getDefault())] = this
	fullDescription = null
    }

    /**
     * Returns the human readable description of this filter. For
     * Example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @see setDescription
     *
     * @see setExtensionListInDescription
     *
     * @see isExtensionListInDescription
     *
     * @see FileFilter.getDescription
     */
    override fun getDescription(): String {
	if (fullDescription == null) {
	    if (description == null || isExtensionListInDescription) {
		fullDescription = if (description == null) "(" else "$description ("
		// build the description from the extension list
		val extensions = filters!!.keys()
		if (extensions != null) {
		    fullDescription += "." + extensions.nextElement() as String
		    while (extensions.hasMoreElements()) {
			fullDescription += ", ." + extensions.nextElement() as String
		    }
		}
		fullDescription += ")"
	    } else {
		fullDescription = description
	    }
	}
	return fullDescription!!
    }

    /**
     * Sets the human readable description of this filter. For
     * Example: filter.setDescription("Gif and JPG Images");
     *
     * @see setDescription
     *
     * @see setExtensionListInDescription
     *
     * @see isExtensionListInDescription
     */
    fun setDescription(description: String?) {
	this.description = description
	fullDescription = null
    }

    var isExtensionListInDescription: Boolean
	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should
	 * show up in the human readable description.
	 *
	 *
	 * Only relevent if a description was provided in the constructor
	 * or using setDescription();
	 *
	 * @see getDescription
	 *
	 * @see setDescription
	 *
	 * @see setExtensionListInDescription
	 */
	get() = useExtensionsInDescription
	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should
	 * show up in the human readable description.
	 *
	 *
	 * Only relevent if a description was provided in the constructor
	 * or using setDescription();
	 *
	 * @see getDescription
	 *
	 * @see setDescription
	 *
	 * @see isExtensionListInDescription
	 */
	set(b) {
	    useExtensionsInDescription = b
	    fullDescription = null
	}

    companion object {
	private const val TYPE_UNKNOWN = "Type Unknown"
	private const val HIDDEN_FILE = "Hidden File"
    }
}
