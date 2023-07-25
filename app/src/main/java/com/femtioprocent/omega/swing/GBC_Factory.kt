package com.femtioprocent.omega.swing

import java.awt.GridBagConstraints
import java.awt.Insets

class GBC_Factory {
    var a = 2
    fun create(x: Int, y: Int): GridBagConstraints {
	return GridBagConstraints(x, y, 1, 1,
		0.0, 0.0,
		GridBagConstraints.CENTER,
		GridBagConstraints.HORIZONTAL,
		Insets(a, a, a, a),
		0, 0)
    }

    fun create(x: Int, y: Int, w: Int): GridBagConstraints {
	return GridBagConstraints(x, y, w, 1,
		0.0, 0.0,
		GridBagConstraints.CENTER,
		GridBagConstraints.HORIZONTAL,
		Insets(a, a, a, a),
		0, 0)
    }

    fun createL(x: Int, y: Int, w: Int): GridBagConstraints {
	return GridBagConstraints(x, y, w, 1,
		0.0, 0.0,
		GridBagConstraints.WEST,
		GridBagConstraints.HORIZONTAL,
		Insets(a, a, a, a),
		0, 0)
    }

    fun createR(x: Int, y: Int): GridBagConstraints {
	return GridBagConstraints(x, y, GridBagConstraints.REMAINDER, 1,
		0.0, 0.0,
		GridBagConstraints.EAST,
		GridBagConstraints.HORIZONTAL,
		Insets(a, a, a, a),
		0, 0)
    }

    fun createR2(x: Int, y: Int): GridBagConstraints {
	return GridBagConstraints(x, y, GridBagConstraints.REMAINDER, 1,
		0.0, 0.0,
		GridBagConstraints.EAST,
		0,
		Insets(a, a, a, a),
		0, 0)
    }
}
