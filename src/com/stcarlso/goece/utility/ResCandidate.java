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

import java.util.*;

/**
 * Represents a pair of resistors that are a candidate for the best match.
 */
public abstract class ResCandidate implements Comparable<ResCandidate> {
	/**
	 * The first resistor value.
	 */
	private final double r1;
	/**
	 * The second resistor value.
	 */
	private final double r2;
	/**
	 * The target resistor value.
	 */
	private final double target;
	/**
	 * The value achieved by this pair.
	 */
	private final double value;

	/**
	 * Creates a new candidate resistor pair.
	 *
	 * @param r1 the first resistor value
	 * @param r2 the second resistor value
	 * @param value the value that these resistors form
	 * @param target the target value to be achieved
	 */
	protected ResCandidate(final double r1, final double r2, final double value,
						   final double target) {
		this.r1 = r1;
		this.r2 = r2;
		this.target = target;
		this.value = value;
	}
	@Override
	public int compareTo(ResCandidate other) {
		// Compare using absolute value of error for quick drill down
		int ret = Double.compare(Math.abs(getError()), Math.abs(other.getError()));
		if (ret == 0) {
			final boolean r1r2 = Double.compare(getR1(), getR2()) == 0,
				r2r1 = Double.compare(other.getR1(), other.getR2()) == 0;
			// If the same, give the advantage to the side with r1 == r2, but no advantage
			// if both are equal (this fixes DividerCandidate on 50/50 ratios)
			if (r1r2 && !r2r1)
				ret = -1;
			else if (r2r1 && !r1r2)
				ret = 1;
		}
		return ret;
	}
	/**
	 * Creates a copy of this value with the same target, but a different pair of source
	 * values. Intended for better abstraction.
	 *
	 * @param r1 the first resistor value
	 * @param r2 the second resistor value
	 * @return a value of the same class as this one, but with the new r1 and r2 value
	 */
	public abstract ResCandidate create(final double r1, final double r2);
	public boolean equals(Object o) {
		return this == o || (o != null && o instanceof ResCandidate &&
			compareTo((ResCandidate)o) == 0);
	}
	/**
	 * Generates the list of candidate resistor values for the target value in this object.
	 *
	 * @param series the EIA resistor series to use
	 * @return a list of all valid values that could be used for this target
	 */
	public double[] generateValues(final EIATable.EIASeries series) {
		// Get target series
		final int[] values = EIATable.seriesValues(series);
		// Generate list of all possible values
		final List<Double> candidates = new ArrayList<Double>(values.length * 8 + 1);
		populateValues(values, candidates);
		final double[] ret = new double[candidates.size()];
		// Copy to array (why cannot use toArray()!?)
		for (int i = 0; i < ret.length; i++)
			ret[i] = candidates.get(i);
		return ret;
	}
	/**
	 * Gets the relative error.
	 *
	 * @return the relative error, not as a percentage (0-1)
	 */
	public double getError() {
		final double t = getTarget(), num = getValue() - t, error;
		// Do not divide by zero, round errors off to allow really equal values to be equal
		if (t == 0.0)
			error = ECECalc.ieeeRound(num);
		else
			error = ECECalc.ieeeRound(num / t);
		return error;
	}
	/**
	 * Gets the first resistor value.
	 *
	 * @return the first resistor value
	 */
	public double getR1() {
		return r1;
	}
	/**
	 * Gets the second resistor value.
	 *
	 * @return the second resistor value
	 */
	public double getR2() {
		return r2;
	}
	/**
	 * Gets the target resistor value.
	 *
	 * @return the target resistor value
	 */
	public double getTarget() {
		return target;
	}
	/**
	 * Gets the value of this pair.
	 *
	 * @return the net value of the two resistors in this pair
	 */
	public double getValue() {
		return value;
	}
	public int hashCode() {
		final long temp = Double.doubleToLongBits(Math.abs(getError()));
		return (int)(temp ^ (temp >>> 32));
	}
	/**
	 * Loads the candidate list with possible values.
	 *
	 * @param values the value prefixes (3 digits from EIASeries.seriesValues)
	 * @param candidates the list where candidate values should be added
	 */
	protected abstract void populateValues(int[] values, List<Double> candidates);
	/**
	 * Return if it is possible for the candidate resistor to be part of a combination to
	 * form the target value of this object.
	 *
	 * @param candidate the value in question
	 * @return whether candidate can form a pair that could possibly generate the target
	 */
	public abstract boolean possible(double candidate);
	/**
	 * Return if it is possible for the candidate resistor to be part of a combination to
	 * form the target value of this object.
	 *
	 * @return whether the value in this object could possibly generate the target
	 */
	public boolean possible() {
		return possible(getValue());
	}
	public String toString() {
		return new EngineeringValue(value, Units.RESISTANCE).toString();
	}
}
