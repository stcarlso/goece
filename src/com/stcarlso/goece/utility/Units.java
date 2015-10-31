/***********************************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Stephen Carlson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **********************************************************************************************/

package com.stcarlso.goece.utility;

/**
 * A class listing available units for EngineeringValue.
 */
public interface Units {
	/**
	 * Indicate capacitance values with "F"
	 */
	String CAPACITANCE = "F";
	/**
	 * Indicate current values with "A"
	 */
	String CURRENT = "A";
	/**
	 * Indicate inductance values with "H"
	 */
	String INDUCTANCE = "H";
	/**
	 * Indicate power units with "W"
	 */
	String POWER = "W";
	/**
	 * Indicate resistance values with the Greek capital omega (ohm) symbol.
	 */
	String RESISTANCE = "\u03A9";
	/**
	 * Indicate voltage values with "V"
	 */
	String VOLTAGE = "V";

	/**
	 * 20% tolerance
	 */
	double TOL_20P = 0.2;
	/**
	 * 10% tolerance
	 */
	double TOL_10P = 0.1;
	/**
	 * 5% tolerance, fairly common
	 */
	double TOL_5P = 0.05;
	/**
	 * 2% tolerance
	 */
	double TOL_2P = 0.02;
	/**
	 * 1% tolerance
	 */
	double TOL_1P = 0.01;
	/**
	 * 0.1% tolerance
	 */
	double TOL_P1 = 0.001;

	/**
	 * 1 / pi
	 */
	double PI_INV = 1.0 / Math.PI;
	/**
	 * The impedance of free space divided by pi
	 */
	double Z_0 = 119.9169832;
}
