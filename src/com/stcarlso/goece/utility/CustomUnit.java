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
 * Denotes a custom unit which can be used for this dialog. Stores the unit name and the
 * conversion factor (the custom value is <i>multiplied</i> by the factor).
 */
public class CustomUnit {
	/**
	 * The multiplicative conversion factor for this unit.
	 */
	public final double factor;
	/**
	 * The unit name shown in the dropdown and entry box.
	 */
	public final String unit;

	/**
	 * Creates a new custom unit.
	 *
	 * @param unit the unit name
	 * @param factor the unit conversion factor
	 */
	public CustomUnit(final String unit, final double factor) {
		if (unit == null)
			throw new NullPointerException("unit");
		if (factor <= 0.0 || Double.isInfinite(factor) || Double.isNaN(factor))
			throw new IllegalArgumentException("conversion factor");
		this.factor = factor;
		this.unit = unit;
	}
	public boolean equals(Object o) {
		return this == o || !(o == null || getClass() != o.getClass()) &&
			unit.equals(((CustomUnit)o).getUnit());
	}
	/**
	 * Converts from the base unit to this unit.
	 *
	 * @param baseValue the value to convert in the base unit
	 * @return the value in this unit
	 */
	public double fromBase(final double baseValue) {
		return baseValue / factor;
	}
	/**
	 * Converts from the base unit to this unit.
	 *
	 * @param baseValue the value to convert in the base unit
	 * @return the value in this unit
	 */
	public EngineeringValue fromBase(final EngineeringValue baseValue) {
		// Tolerance is a % so it is not multiplied!
		return new EngineeringValue(baseValue.getValue() / factor, baseValue.getTolerance(),
			baseValue.getSigfigs(), unit);
	}
	/**
	 * Gets the conversion factor for this unit.
	 *
	 * @return the unit multiplicative conversion factor
	 */
	public double getFactor() {
		return factor;
	}
	/**
	 * Gets the unit name for this unit.
	 *
	 * @return the unit name
	 */
	public String getUnit() {
		return unit;
	}
	public int hashCode() {
		return unit.hashCode();
	}
	/**
	 * Converts from this unit to the base unit.
	 *
	 * @param unitValue the value to convert in this unit
	 * @return the value in the base unit
	 */
	public double toBase(final double unitValue) {
		return unitValue * factor;
	}
	/**
	 * Converts from this unit to the base unit.
	 *
	 * @param unitValue the value to convert in this unit
	 * @return the value in the base unit
	 */
	public EngineeringValue toBase(final EngineeringValue unitValue) {
		// Tolerance is a % so it is not multiplied!
		return new EngineeringValue(unitValue.getValue() * factor, unitValue.getTolerance(),
			unitValue.getSigfigs(), unit);
	}
	public String toString() {
		return unit;
	}
}