/***********************************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Stephen Carlson
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
	 * Creates a list of unit choices, with all of the valid unit prefixes prepended in
	 * magnitude order to the specified suffix.
	 *
	 * @param suffix the base unit suffix (m, V, A...)
	 * @return an array of unit choices from smallest to largest
	 */
	public static String[] buildUnitChoices(final String suffix) {
		final String[] unitList = new String[ENGR_NAMES.length - 1];
		for (int i = 0; i < unitList.length; i++)
			unitList[i] = ENGR_NAMES[i] + suffix;
		return unitList;
	}
	/**
	 * Formats a significand from 0 (inclusive) to 1000 (exclusive!) as a string in engineering
	 * notation.
	 *
	 * @param value the value to format
	 * @param sigfigs the number of significant figures to use
	 * @return the value formatted with the correct number of significant figures and the radix
	 * point in the appropriate location
	 */
	public static String significandToString(final double value, final int sigfigs) {
		final double absSig = Math.abs(value);
		final String out;
		if (Double.isInfinite(value))
			// If infinite, display it that way
			out = (value > 0.0) ? "\u221E" : "-\u221E";
		else {
			final int decimals;
			// Calculate number of decimal places to show
			if (absSig >= 99.95)
				decimals = Math.max(sigfigs - 3, 0);
			else if (absSig >= 9.995)
				decimals = Math.max(sigfigs - 2, 0);
			else if (absSig >= 0.9995 || absSig == 0.0)
				decimals = sigfigs - 1;
			else
				decimals = sigfigs;
			// Compose format string
			out = String.format("%." + decimals + "f", value);
		}
		return out;
	}
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
		"f", "p", "n", "\u03BC", "m", "", "K", "M", "G", "T", "P"
	};
	/**
	 * Cut-off values for engineering formatting.
	 */
	public static final double[] ENGR_THRESHOLD = {
		1e-15, 1e-12, 1e-9, 1e-6, 1e-3, 1, 1e3, 1e6, 1e9, 1e12, 1e15
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
			throw new IllegalArgumentException("value: NaN");
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
		int code = 5;
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
					if (absValue < ENGR_THRESHOLD[i + 1] * 0.9995) {
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
	/**
	 * Adds this EngineeringValue to another. This is really only useful in the ComplexValue
	 * instance, but still works for the real valued case.
	 *
	 * The type (real vs complex), units, tolerance, and significant figures are inherited from
	 * the left-hand side value.
	 *
	 * @param other the addend
	 * @return the sum
	 */
	public EngineeringValue add(final EngineeringValue other) {
		return newValue(getValue() + other.getReal());
	}
	/**
	 * Appends the tolerance specifier if necessary.
	 *
	 * @param format the location for the formatted tolerance to be placed
	 */
	private void appendToleranceFormat(final StringBuilder format) {
		final double tol = getTolerance();
		if (tol > 0.0) {
			// value +/- #%
			format.append(' ');
			format.append(P_M_SYMBOL);
			format.append(toleranceToString(tol));
			format.append("%%");
		}
	}
	/**
	 * Divides this EngineeringValue by another. This is really only useful in the
	 * ComplexValue instance, but still works for the real valued case.
	 *
	 * The type (real vs complex), units, tolerance, and significant figures are inherited from
	 * the left-hand side value.
	 *
	 * @param other the divisor
	 * @return the quotient
	 * @throws ArithmeticException if the divisor's magnitude is zero
	 */
	public EngineeringValue divide(final EngineeringValue other) {
		final double divisor = other.getReal();
		if (divisor == 0.0)
			throw new ArithmeticException("Real-valued division by zero");
		return newValue(getValue() / divisor);
	}
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final EngineeringValue value = (EngineeringValue)o;
		return Double.compare(value.getValue(), getValue()) == 0 && getUnits().equals(
			value.getUnits());
	}
	/**
	 * Retrieves the phase angle of this value.
	 *
	 * @return the phase angle in degrees
	 */
	public double getAngle() {
		return 0.0;
	}
	/**
	 * Retrieves the imaginary part of this value. This value is precomputed.
	 *
	 * @return the imaginary part, magnitude * sin(phase)
	 */
	public double getImaginary() {
		return 0.0;
	}
	/**
	 * Retrieves the real part of this value. This value is precomputed.
	 *
	 * @return the real part, magnitude * cos(phase)
	 */
	public double getReal() {
		return raw;
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
	 * Multiplies this EngineeringValue by another. This is really only useful in the
	 * ComplexValue instance, but still works for the real valued case.
	 *
	 * The type (real vs complex), units, tolerance, and significant figures are inherited from
	 * the left-hand side value.
	 *
	 * @param other the multiplicand
	 * @return the product
	 */
	public EngineeringValue multiply(final EngineeringValue other) {
		return newValue(getValue() * other.getReal());
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
	 * Raises this value to the power of the exponent. For real values, equivalent of
	 * Math.pow(), but applies DeMoivre's Theorem for complex values.
	 *
	 * The type (real vs complex), units, tolerance, and significant figures are inherited from
	 * this value.
	 *
	 * @param exponent the power to raise this value
	 * @return this value raised to the specified power
	 */
	public EngineeringValue pow(final double exponent) {
		return newValue(Math.pow(getValue(), exponent));
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
		return significandToString(getSignificand(), sf);
	}
	/**
	 * Subtracts this EngineeringValue from another. This is really only useful in the
	 * ComplexValue instance, but still works for the real valued case.
	 *
	 * @param other the subtrahend
	 * @return the difference
	 */
	public EngineeringValue subtract(final EngineeringValue other) {
		return newValue(getValue() - other.getReal());
	}
	/**
	 * A variation of toString() which uses valueToString() and omits the SI prefix, for units
	 * which have the prefix already or are in the English system (why?)
	 *
	 * @param sf the number of significant figures to apply
	 * @return this value as a a string
	 */
	public String toExponentialString(final int sf) {
		final StringBuilder format = new StringBuilder(valueToString(sf));
		format.append(" %s");
		appendToleranceFormat(format);
		return String.format(format.toString(), getUnits()).trim();
	}
	public String toString() {
		final StringBuilder format = new StringBuilder(significandToString());
		format.append(" %s%s");
		appendToleranceFormat(format);
		return String.format(format.toString(), getSIPrefix(), getUnits()).trim();
	}
	/**
	 * Converts the raw value of this EngineeringValue to a string with the correct number of
	 * significant figures and the E+/E- specifier if needed, but with no units or other
	 * suffixes. Essentially performs a String.format() call as a double.
	 *
	 * @param sf the number of significant figures to use
	 * @return the raw value as a a string
	 */
	public String valueToString(final int sf) {
		final String formatString;
		if (sf > 0)
			formatString = "%." + Integer.toString(sf) + "g";
		else
			// Prevent crash with negative value
			formatString = "%.0f";
		return String.format(formatString, getValue());
	}
}
