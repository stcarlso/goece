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

import java.io.Serializable;

/**
 * Represents a value in engineering notation, with the units, suffix, significand, and number
 * of significant figures stored. Optional tolerance is also carried.
 */
public class EngineeringValue implements Serializable {
	private static final long serialVersionUID = 3381934552647230468L;

	/**
	 * Formats a tolerance value as a string.
	 *
	 * @param tolIn the tolerance value to display
	 * @return the value with a reasonable number of decimal places and no extra sigfigs!
	 */
	public static String toleranceToString(final double tolIn) {
		// Do what we can to fix the broken mess that is floating point
		final double tol = tolIn * 100.0;
		final int tolInt = (int)Math.round(100.0 * tol), dp;
		if (tolInt % 100 == 0)
			dp = 0;
		else if (tolInt % 10 == 0)
			dp = 1;
		else
			dp = 2;
		return String.format("%." + Integer.toString(dp) + "f", tol);
	}
	/**
	 * Generates a raw value from a significand and prefix code. Mainly useful when constructing
	 * a value from a friendly entry field allowing discrete prefix selection.
	 *
	 * @param significand the significand (need not be in 1..1000)
	 * @param prefixCode the SI prefix code
	 * @return the corresponding raw value
	 */
	public static double valueFromSigExp(final double significand, final int prefixCode) {
		if (prefixCode < 0 || prefixCode >= ENGR_THRESHOLD.length)
			throw new IllegalArgumentException("prefix code");
		return significand * ENGR_THRESHOLD[prefixCode];
	}

	/**
	 * The plus/minus symbol. Do not localize.
	 */
	public static final String P_M_SYMBOL = "\u00B1";
	/**
	 * The prefix the unit gets for each cut-off below.
	 */
	public static final String[] ENGR_NAMES = {
		"f", "p", "n", "\u03BC", "m", "", "K", "M", "G", "T", "P", "E"
	};
	/**
	 * Cut-off values for engineering formatting.
	 */
	public static final double[] ENGR_THRESHOLD = {
		1e-15, 1e-12, 1e-9, 1e-6, 1e-3, 1, 1e3, 1e6, 1e9, 1e12, 1e15, 1e18
	};

	/**
	 * The number of significant digits.
	 */
	protected final int sigfigs;
	/**
	 * Raw significand of the value (>= 1.0 unless below minimum suffix, < 1000.0 unless above
	 * maximum suffix).
	 */
	protected final double significand;
	/**
	 * The suffix code. Actual suffix is looked up in the suffixes table.
	 */
	protected final int prefix;
	/**
	 * The raw value originally supplied to this class.
	 */
	protected final double raw;
	/**
	 * The value tolerance, or 0.0 if none is given.
	 */
	protected final double tolerance;
	/**
	 * Units, without the suffix.
	 */
	protected final String units;

	/**
	 * Create an engineering value with no units, no tolerance, and 3 significant figures.
	 *
	 * @param value the raw value
	 */
	public EngineeringValue(final double value) {
		this(value, "");
	}
	/**
	 * Creates a new engineering value with the specified units and 3 significant figures.
	 *
	 * @param value the raw value
	 * @param units the units to assign to this value
	 */
	public EngineeringValue(final double value, final String units) {
		this(value, 0.0, units);
	}
	/**
	 * Creates a new engineering value with the specified tolerance and units. The value will
	 * have 3 significant figures.
	 *
	 * @param value the raw value
	 * @param tolerance the tolerance of this value (0.1 = 10%, 0.01 = 1%) or 0 to suppress
	 * @param units the units to assign to this value
	 */
	public EngineeringValue(final double value, final double tolerance, final String units) {
		this(value, tolerance, 3, units);
	}
	/**
	 * Create a new engineering value based on an existing value's tolerance, significant
	 * figures, and units, but with a new raw value.
	 *
	 * @param value the raw value
	 * @param template the template value where units, tolerance, and significant figures are
	 * copied
	 */
	public EngineeringValue(final double value, final EngineeringValue template) {
		this(value, template.getTolerance(), template.getSigfigs(), template.getUnits());
	}
	/**
	 * Creates a new engineering value with the specified tolerance, precision, and units.
	 *
	 * @param value the raw value
	 * @param tolerance the tolerance of this value (0.1 = 10%, 0.01 = 1%) or 0 to suppress
	 * @param sigfigs the number of significant figures
	 * @param units the units to assign to this value
	 */
	public EngineeringValue(final double value, final double tolerance, final int sigfigs,
							final String units) {
		final double absValue = Math.abs(value);
		// Do not allow NaN
		if (Double.isNaN(value))
			throw new IllegalArgumentException("value: " + value);
		if (units == null)
			throw new NullPointerException("units");
		if (tolerance < 0.0 || tolerance >= 1.0)
			throw new IllegalArgumentException("tolerance: " + tolerance);
		if (sigfigs < 1 || sigfigs > 14)
			throw new IllegalArgumentException("significant figures: " + sigfigs);
		// Initialize tolerance and units
		raw = value;
		this.sigfigs = sigfigs;
		this.tolerance = tolerance;
		this.units = units;
		int code = 6;
		double engr = value;
		// Look for the prefix
		if (absValue > 0.0 && !Double.isInfinite(value)) {
			if (absValue >= ENGR_THRESHOLD[ENGR_THRESHOLD.length - 1])
				// Infinite enough
				engr = (value > 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
			else if (absValue < ENGR_THRESHOLD[0])
				// Flush to zero
				engr = 0.0;
			else
				// Somewhere in between
				for (int i = 0; i < ENGR_THRESHOLD.length && i < ENGR_NAMES.length; i++)
					if (absValue < ENGR_THRESHOLD[i + 1]) {
						// Found correct prefix
						code = i;
						engr = value / ENGR_THRESHOLD[code];
						break;
					}
		}
		// Assign significand and prefix code
		prefix = code;
		significand = engr;
	}
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final EngineeringValue value = (EngineeringValue)o;
		return Double.compare(value.getValue(), getValue()) == 0 && getUnits().equals(
			value.getUnits());
	}
	/**
	 * Retrieves the number of significant figures of this value.
	 *
	 * @return the total number of significant digits
	 */
	public int getSigfigs() {
		return sigfigs;
	}
	/**
	 * Retrieves the significand of this value.
	 *
	 * @return the value significand used when displaying engineering notation
	 */
	public double getSignificand() {
		return significand;
	}
	/**
	 * Retrieves the SI prefix of this value. This prefixes the units.
	 *
	 * @return the SI prefix of this value
	 */
	public String getSIPrefix() {
		return ENGR_NAMES[prefix];
	}
	/**
	 * Retrieves the SI prefix code of this value. Only useful when indexing the names array
	 * in prefix choice dialogs.
	 *
	 * @return the SI prefix code of this value
	 */
	public int getSIPrefixCode() {
		return prefix;
	}
	/**
	 * Retrieves the tolerance of this value, or 0.0 if no tolerance is available.
	 *
	 * @return the tolerance from 0.0 to 1.0
	 */
	public double getTolerance() {
		return tolerance;
	}
	/**
	 * Retrieves the units of this value, or "" if no units were provided.
	 *
	 * @return the units associated with this value
	 */
	public String getUnits() {
		return units;
	}
	/**
	 * Retrieves the original value.
	 *
	 * @return the raw value
	 */
	public double getValue() {
		return raw;
	}
	public int hashCode() {
		final long temp = Double.doubleToLongBits(getValue());
		return 31 * (int)(temp ^ (temp >>> 32)) + getUnits().hashCode();
	}
	/**
	 * Convenience method to copy the metadata of this value into a new object.
	 *
	 * @param newRaw the new raw value
	 * @return a new instance with the specified value, but units and tolerance from this object
	 */
	public EngineeringValue newValue(final double newRaw) {
		return new EngineeringValue(newRaw, this);
	}
	/**
	 * Returns the significand of this value rounded to the significant figures places. Does
	 * not include E+ or E- exponent.
	 *
	 * @return the significand of this value rounded to significant figures
	 */
	public String significandToString() {
		return significandToString(getSigfigs());
	}
	/**
	 * Returns the significand of this value rounded to a custom number of significant figures.
	 * Does not include E+ or E- exponent.
	 *
	 * @param sf the number of significant figures to apply
	 * @return the significand of this value rounded to significant figures
	 */
	public String significandToString(final int sf) {
		final double sig = getSignificand(), absSig = Math.abs(sig);
		final String out;
		if (Double.isInfinite(sig))
			// If infinite, display it that way
			out = (sig > 0.0) ? "\u221E" : "-\u221E";
		else {
			final int decimals;
			// Calculate number of decimal places to show
			if (absSig >= 100.0)
				decimals = sf - 3;
			else if (absSig >= 10.0)
				decimals = sf - 2;
			else if (absSig >= 1.0)
				decimals = sf - 1;
			else
				decimals = sf;
			// Compose format string
			out = String.format("%." + decimals + "f", sig);
		}
		return out;
	}
	public String toString() {
		final StringBuilder format = new StringBuilder(significandToString());
		final double tol = getTolerance();
		format.append(" %s%s");
		if (tol > 0.0) {
			// value +/- #%
			format.append(' ');
			format.append(P_M_SYMBOL);
			format.append(toleranceToString(tol));
			format.append("%%");
		}
		return String.format(format.toString(), getSIPrefix(), getUnits());
	}
}
