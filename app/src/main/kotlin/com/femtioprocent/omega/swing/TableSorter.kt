package com.femtioprocent.omega.swing

import com.femtioprocent.omega.OmegaContext
import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JTable
import javax.swing.event.TableModelEvent
import javax.swing.table.TableModel

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel)
 * and itself implements TableModel. TableSorter does not store or copy
 * the data in the TableModel, instead it maintains an array of
 * integers which it keeps the same size as the number of rows in its
 * model. When the model changes it notifies the sorter that something
 * has changed eg. "rowsAdded" so that its internal array of integers
 * can be reallocated. As requests are made of the sorter (like
 * getValueAt(row, col) it redirects them to its model via the mapping
 * array. That way the TableSorter appears to hold another copy of the table
 * with the rows in a different order. The sorting algorthm used is stable
 * which means that it does not move around rows when its comparison
 * function returns 0 to denote that they are equivalent.
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */ // Imports for picking up mouse events from the JTable.
class TableSorter : TableMap {
    var indexes: IntArray? = null
    var sortingColumns: Vector<*> = Vector<Any?>()
    var ascending = true
    var compares = 0

    constructor() {
	indexes = IntArray(0) // for consistency
    }

    constructor(model: TableModel) {
	setModel(model)
    }

    override fun setModel(model: TableModel) {
	super.setModel(model)
	reallocateIndexes()
    }

    fun compareRowsByColumn(row1: Int, row2: Int, column: Int): Int {
	val type = model!!.getColumnClass(column)
	val data = model!!

	// Check for nulls.
	val o1 = data.getValueAt(row1, column)
	val o2 = data.getValueAt(row2, column)

	// If both values are null, return 0.
	if (o1 == null && o2 == null) {
	    return 0
	} else if (o1 == null) { // Define null less than everything.
	    return -1
	} else if (o2 == null) {
	    return 1
	}

	/*
         * We copy all returned values from the getValue call in case
         * an optimised model is reusing one object to return many
         * values.  The Number subclasses in the JDK are immutable and
         * so will not be used in this way but other subclasses of
         * Number might want to do this to save space and avoid
         * unnecessary heap allocation.
         */return if (type.superclass == Number::class.java) {
	    val n1 = data.getValueAt(row1, column) as Number
	    val d1 = n1.toDouble()
	    val n2 = data.getValueAt(row2, column) as Number
	    val d2 = n2.toDouble()
	    if (d1 < d2) {
		-1
	    } else if (d1 > d2) {
		1
	    } else {
		0
	    }
	} else if (type == Date::class.java) {
	    val d1 = data.getValueAt(row1, column) as Date
	    val n1 = d1.time
	    val d2 = data.getValueAt(row2, column) as Date
	    val n2 = d2.time
	    if (n1 < n2) {
		-1
	    } else if (n1 > n2) {
		1
	    } else {
		0
	    }
	} else if (type == String::class.java) {
	    val s1 = data.getValueAt(row1, column) as String
	    val s2 = data.getValueAt(row2, column) as String
	    val result = s1.compareTo(s2)
	    if (result < 0) {
		-1
	    } else if (result > 0) {
		1
	    } else {
		0
	    }
	} else if (type == Boolean::class.java) {
	    val bool1 = data.getValueAt(row1, column) as Boolean
	    val bool2 = data.getValueAt(row2, column) as Boolean
	    if (bool1 == bool2) {
		0
	    } else if (bool1) { // Define false < true
		1
	    } else {
		-1
	    }
	} else {
	    val v1 = data.getValueAt(row1, column)
	    val s1 = v1.toString()
	    val v2 = data.getValueAt(row2, column)
	    val s2 = v2.toString()
	    val result = s1.compareTo(s2)
	    if (result < 0) {
		-1
	    } else if (result > 0) {
		1
	    } else {
		0
	    }
	}
    }

    fun compare(row1: Int, row2: Int): Int {
	compares++
	for (level in sortingColumns.indices) {
	    val column = sortingColumns.elementAt(level) as Int
	    val result = compareRowsByColumn(row1, row2, column)
	    if (result != 0) {
		return if (ascending) result else -result
	    }
	}
	return 0
    }

    fun reallocateIndexes() {
	val rowCount = model!!.rowCount

	// Set up a new array of indexes with the right number of elements
	// for the new data model.
	indexes = IntArray(rowCount)

	// Initialise with the identity mapping.
	for (row in 0 until rowCount) {
	    indexes!![row] = row
	}
    }

    override fun tableChanged(e: TableModelEvent) {
	//OmegaContext.sout_log.getLogger().info("Sorter: tableChanged");
	reallocateIndexes()
	super.tableChanged(e)
    }

    fun checkModel() {
	if (indexes!!.size != model!!.rowCount) {
	    OmegaContext.sout_log.getLogger().info(":--: " + "Sorter not informed of a change in model.")
	}
    }

    fun sort(sender: Any?) {
	checkModel()
	compares = 0
	// n2sort();
	// qsort(0, indexes.length-1);
	shuttlesort(indexes!!.clone(), indexes!!, 0, indexes!!.size)
	//OmegaContext.sout_log.getLogger().info("Compares: "+compares);
    }

    fun n2sort() {
	for (i in 0 until rowCount) {
	    for (j in i + 1 until rowCount) {
		if (compare(indexes!![i], indexes!![j]) == -1) {
		    swap(i, j)
		}
	    }
	}
    }

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    fun shuttlesort(from: IntArray, to: IntArray, low: Int, high: Int) {
	if (high - low < 2) {
	    return
	}
	val middle = (low + high) / 2
	shuttlesort(to, from, low, middle)
	shuttlesort(to, from, middle, high)
	var p = low
	var q = middle

	/* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first dep_set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */if (high - low >= 4 && compare(
		from[middle - 1],
		from[middle]
	    ) <= 0
	) {
	    for (i in low until high) {
		to[i] = from[i]
	    }
	    return
	}

	// A normal merge.
	for (i in low until high) {
	    if (q >= high || p < middle && compare(from[p], from[q]) <= 0) {
		to[i] = from[p++]
	    } else {
		to[i] = from[q++]
	    }
	}
    }

    fun swap(i: Int, j: Int) {
	val tmp = indexes!![i]
	indexes!![i] = indexes!![j]
	indexes!![j] = tmp
    }

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".
    override fun getValueAt(aRow: Int, aColumn: Int): Any {
	checkModel()
	return model!!.getValueAt(indexes!![aRow], aColumn)
    }

    override fun setValueAt(aValue: Any, aRow: Int, aColumn: Int) {
	checkModel()
	model!!.setValueAt(aValue, indexes!![aRow], aColumn)
    }

    @JvmOverloads
    fun sortByColumn(column: Int, ascending: Boolean = true) {
	this.ascending = ascending
	sortingColumns.removeAllElements()
	sortingColumns.addElement(column as Nothing?)
	sort(this)
	super.tableChanged(TableModelEvent(this))
    }

    // There is no-where else to put this.
    // Add a mouse listener to the Table to trigger a table sort
    // when a column heading is clicked in the JTable.
    fun addMouseListenerToHeaderInTable(table: JTable) {
	val sorter = this
	table.columnSelectionAllowed = false
	val listMouseListener: MouseAdapter = object : MouseAdapter() {
	    override fun mouseClicked(e: MouseEvent) {
		val columnModel = table.columnModel
		val viewColumn = columnModel.getColumnIndexAtX(e.x)
		val column = table.convertColumnIndexToModel(viewColumn)
		if (e.clickCount == 1 && column != -1) {
		    //OmegaContext.sout_log.getLogger().info("Sorting ...");
		    val shiftPressed = e.modifiersEx and InputEvent.SHIFT_DOWN_MASK
		    val ascending = shiftPressed == 0
		    sorter.sortByColumn(column, ascending)
		}
	    }
	}
	val th = table.tableHeader
	th.addMouseListener(listMouseListener)
    }
}
