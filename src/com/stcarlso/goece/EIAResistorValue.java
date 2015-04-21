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

package com.stcarlso.goece;

/**
 * Represents an engineering resistor value (convenience subclass of EngineeringValue)
 */
public class EIAResistorValue extends EngineeringValue {
	private static final long serialVersionUID = 6556740813218260654L;
	/**
	 * 20 %
	 */
	public static final EIAResistorTable.EIASeries E6 = EIAResistorTable.EIASeries.E6;
	/**
	 * 10 %
	 */
	public static final EIAResistorTable.EIASeries E12 = EIAResistorTable.EIASeries.E12;
	/**
	 * 5 %
	 */
	public static final EIAResistorTable.EIASeries E24 = EIAResistorTable.EIASeries.E24;
	/**
	 * 2 %
	 */
	public static final EIAResistorTable.EIASeries E48 = EIAResistorTable.EIASeries.E48;
	/**
	 * 1 %
	 */
	public static final EIAResistorTable.EIASeries E96 = EIAResistorTable.EIASeries.E96;

	/**
	 * Converts an EIA series to its recommended tolerance.
	 *
	 * @param series the resistor series to convert
	 * @return the recommended tolerance for that series
	 */
	public static double eiaSeriesToTolerance(final EIAResistorTable.EIASeries series) {
		final double tol;
		switch (series) {
		case E6:
			// 20%
			tol = Units.TOL_20P;
			break;
		case E12:
			// 10%
			tol = Units.TOL_10P;
			break;
		case E24:
			// 5%
			tol = Units.TOL_5P;
			break;
		case E48:
			// 2%
			tol = Units.TOL_2P;
			break;
		case E96:
		default:
			// 1%
			tol = Units.TOL_1P;
			break;
		}
		return tol;
	}

	/**
	 * The EIA resistor series which represents the tolerance of this value.
	 */
	protected EIAResistorTable.EIASeries series;

	/**
	 * Create a new EIA resistor value.
	 *
	 * @param value the resistor value
	 * @param series the resistor EIA series where this resistor was taken (used for tolerance)
	 */
	public EIAResistorValue(final double value, final EIAResistorTable.EIASeries series) {
		this(value, series, eiaSeriesToTolerance(series));
	}
	/**
	 * Create a new EIA resistor value with a tolerance override.
	 *
	 * @param value the resistor value
	 * @param series the resistor EIA series where this resistor was taken
	 * @param tolerance the tolerance to apply (overrides series code)
	 */
	public EIAResistorValue(final double value, final EIAResistorTable.EIASeries series,
							final double tolerance) {
		super(value, tolerance, Units.RESISTANCE);
		this.series = series;
	}
	/**
	 * Retrieves the series code of this resistor.
	 *
	 * @return the resistor series code
	 */
	public EIAResistorTable.EIASeries getSeries() {
		return series;
	}
}
