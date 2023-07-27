package com.femtioprocent.omega.swing

import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel

/**
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting
 * a TableMap which has not been subclassed into a chain of table filters
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne
 */
open class TableMap : AbstractTableModel(), TableModelListener {
    @JvmField
    var model: TableModel? = null

    open fun setModel(model: TableModel) {
	this.model = model
	model.addTableModelListener(this)
    }

    // By default, implement TableModel by forwarding all messages 
    // to the model. 
    override fun getValueAt(aRow: Int, aColumn: Int): Any {
	return model!!.getValueAt(aRow, aColumn)
    }

    override fun setValueAt(aValue: Any, aRow: Int, aColumn: Int) {
	model!!.setValueAt(aValue, aRow, aColumn)
    }

    override fun getRowCount(): Int {
	return if (model == null) 0 else model!!.rowCount
    }

    override fun getColumnCount(): Int {
	return if (model == null) 0 else model!!.columnCount
    }

    override fun getColumnName(aColumn: Int): String {
	return model!!.getColumnName(aColumn)
    }

    override fun getColumnClass(aColumn: Int): Class<*> {
	return model!!.getColumnClass(aColumn)
    }

    override fun isCellEditable(row: Int, column: Int): Boolean {
	return model!!.isCellEditable(row, column)
    }

    //
    // Implementation of the TableModelListener interface, 
    //
    // By default forward all events to all the listeners. 
    override fun tableChanged(e: TableModelEvent) {
	fireTableChanged(e)
    }
}
