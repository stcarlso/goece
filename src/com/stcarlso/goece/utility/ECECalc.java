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
 * A class containing all calculation methods used in multiple classes.
 */
public final class ECECalc {
	/**
	 * Calculates the complete elliptic integral of the first kind for the parameter x.
	 *
	 * @param x the parameter of the integral
	 * @return the elliptic integral
	 */
	public static double elliptic(final double x) {
		return elliptic(x, 24);
	}
	/**
	 * Calculates the complete elliptic integral of the first kind for the parameter x.
	 *
	 * @param x the parameter of the integral
	 * @param terms the number of terms to compute in the series approximation
	 * @return the elliptic integral
	 */
	public static double elliptic(final double x, final int terms) {
		double sum = 1.0;
		// x can be positive or negative in theory
		double kpow = 1.0, num = 1.0, denom = 1.0;
		// <= 0 terms is meaningless, and 1 term is always pi / 2
		for (int i = 1; i < terms; i++) {
			final int idx = i << 1;
			kpow = kpow * x * x;
			num *= idx - 1;
			denom *= idx;
			// Saves 1 multiply?
			final double ratio = num / denom;
			sum += kpow * ratio * ratio;
		}
		return sum * Math.PI * 0.5;
	}
	/**
	 * Rounds the double-precision value to 40 bits. Essential for equalizing small calculation
	 * errors from base-two rounding through calculation chains. This is about the 1E-9 decimal
	 * place relative to the mantissa.
	 *
	 * @param value the value to round
	 * @return the value rounded to 40 bits
	 */
	public static double ieeeRound(final double value) {
		return Math.scalb(Math.rint(Math.scalb(value, 40)), -40);
	}
	/**
	 * Resistors get an "ordinal" value for a particular iteration that goes from 0 to 8 *
	 * values.length to allow triangle iteration.
	 *
	 * @param index the ordinal index
	 * @param values the 3-digit values from EIATable.seriesValues
	 * @return that resistor value (index 0 is 0 ohms)
	 */
	public static double ordinalResistor(final int index, final int[] values) {
		final double ret;
		final int idx = index - 1, len = values.length;
		if (index == 0)
			// Make sure that this exists
			ret = 0.0;
		else
			// Values are striped through the periods, with exponent increasing on each wrap
			ret = values[idx % len] * Math.pow(10.0, (idx / len) - 3);
		return ret;
	}
	/**
	 * Calculates the equivalent resistance of two resistors in parallel.
	 *
	 * @param r1 the first resistor value
	 * @param r2 the second resistor value
	 * @return the equivalent parallel resistance
	 */
	public static double parallelResistance(final double r1, final double r2) {
		final double denom = r1 + r2, ret;
		if (denom == 0.0)
			// Do not divide by zero
			ret = 0.0;
		else if (Double.isInfinite(r1))
			// r1 = +inf, return r2
			ret = r2;
		else if (Double.isInfinite(r2))
			// r2 = +inf, return r1
			ret = r1;
		else
			ret = (r1 * r2) / denom;
		return ret;
	}
	/**
	 * Calculates the voltage division ratio between two series-connected resistors.
	 *
	 * @param r1 the top resistor value
	 * @param r2 the bottom resistor value
	 * @return the ratio of the voltage in between the resistors to the voltage supplied up top
	 */
	public static double voltageDivide(final double r1, final double r2) {
		// Handle infinite and zero resistances
		final double ratio;
		if (r2 == 0.0 || Double.isInfinite(r1))
			ratio = 0.0;
		else if (Double.isInfinite(r2) || r1 == 0.0)
			ratio = 1.0;
		else
			ratio = r2 / (r1 + r2);
		return ratio;
	}
}
