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
 * Represents a complex value in engineering notation, with the units, suffix, significand, and
 * number of significant figures stored. Optional tolerance is also carried.
 */
public class ComplexValue extends EngineeringValue implements Serializable {
	private static final long serialVersionUID = -7587317370710347618L;

	/**
	 * The phase angle in degrees.
	 */
	protected final double angle;
	/**
	 * Cached imaginary part.
	 */
	private transient double imag;
	/**
	 * Cached real part.
	 */
	private transient double real;

	/**
	 * Create an engineering value with no units, no tolerance, and 3 significant figures.
	 *
	 * @param mag the raw magnitude value
	 * @param phase the phase angle in degrees
	 */
	public ComplexValue(final double mag, final double phase) {
		this(mag, phase, "");
	}
	/**
	 * Creates a new engineering value with the specified units and 3 significant figures.
	 *
	 * @param mag the raw magnitude value
	 * @param phase the phase angle in degrees
	 * @param units the units to assign to this value
	 */
	public ComplexValue(final double mag, final double phase, final String units) {
		this(mag, phase, 0.0, units);
	}
	/**
	 * Creates a new engineering value with the specified tolerance and units. The value will
	 * have 3 significant figures.
	 *
	 * @param mag the raw magnitude value
	 * @param phase the phase angle in degrees
	 * @param tolerance the tolerance of this value (0.1 = 10%, 0.01 = 1%) or 0 to suppress
	 * @param units the units to assign to this value
	 */
	public ComplexValue(final double mag, final double phase, final double tolerance, final String units) {
		this(mag, phase, tolerance, 3, units);
	}
	/**
	 * Create a new engineering value based on an existing value's tolerance, significant
	 * figures, and units, but with a new raw value and phase angle.
	 *
	 * @param mag the raw magnitude value
	 * @param phase the phase angle in degrees
	 * @param template the template value where units, tolerance, and significant figures are
	 * copied
	 */
	public ComplexValue(final double mag, final double phase, final ComplexValue template) {
		this(mag, phase, template.getTolerance(), template.getSigfigs(), template.getUnits());
	}
	/**
	 * Creates a new engineering value with the specified tolerance, precision, and units.
	 *
	 * @param mag the raw magnitude value
	 * @param phase the phase angle in degrees
	 * @param tolerance the tolerance of this value (0.1 = 10%, 0.01 = 1%) or 0 to suppress
	 * @param sigfigs the number of significant figures
	 * @param units the units to assign to this value
	 */
	public ComplexValue(final double mag, final double phase, final double tolerance,
						final int sigfigs, final String units) {
		super(Math.abs(mag), tolerance, sigfigs, units);
		final double angleRad = Math.toRadians(phase);
		// Compensate for negative magnitude
		double phaseNormal = phase;
		if (mag < 0.0)
			phaseNormal += 180.0;
		// Normalize to [0, 360)
		phaseNormal %= 360.0;
		if (phaseNormal < 0.0) phaseNormal += 360.0;
		angle = phaseNormal;
		real = ECECalc.ieeeRound(mag * Math.cos(angleRad));
		imag = ECECalc.ieeeRound(mag * Math.sin(angleRad));
	}
	@Override
	public EngineeringValue add(final EngineeringValue other) {
		return newRectangularValue(getReal() + other.getReal(),
			getImaginary() + other.getImaginary());
	}
	@Override
	public EngineeringValue divide(final EngineeringValue other) {
		final double divisor = other.getValue();
		if (divisor == 0.0)
			throw new ArithmeticException("Complex-valued division by zero");
		return newValue(getValue() / divisor, getAngle() - other.getAngle());
	}
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final ComplexValue value = (ComplexValue)o;
		return Double.compare(value.getAngle(), getAngle()) == 0 && getUnits().equals(
			value.getUnits()) && Double.compare(value.getValue(), getValue()) == 0;
	}
	@Override
	public double getAngle() {
		return angle;
	}
	@Override
	public double getImaginary() {
		return imag;
	}
	@Override
	public double getReal() {
		return real;
	}
	public int hashCode() {
		final long temp = Double.doubleToLongBits(getAngle());
		return 31 * (int)(temp ^ (temp >>> 32)) + super.hashCode();
	}
	@Override
	public EngineeringValue multiply(final EngineeringValue other) {
		return newValue(getValue() * other.getValue(), getAngle() + other.getAngle());
	}
	/**
	 * Convenience method to copy the metadata of this value into a new object.
	 *
	 * @param newReal the new real component
	 * @param newImag the new imaginary component
	 * @return a new instance with the specified value, but units and tolerance from this object
	 */
	public ComplexValue newRectangularValue(final double newReal, final double newImag) {
		final double newMag = Math.hypot(newReal, newImag);
		double newPhase = Math.toDegrees(Math.atan2(newImag, newReal));
		// [-180, 180) to [0, 360)
		if (newPhase < 0.0)
			newPhase += 360.0;
		return new ComplexValue(newMag, newPhase, this);
	}
	/**
	 * Convenience method to copy the metadata of this value into a new object.
	 *
	 * @param newMag the new raw magnitude value
	 * @param newPhase the new phase angle in degrees
	 * @return a new instance with the specified value, but units and tolerance from this object
	 */
	public ComplexValue newValue(final double newMag, final double newPhase) {
		return new ComplexValue(newMag, newPhase, this);
	}
	@Override
	public EngineeringValue pow(final double exponent) {
		// Complex values have more than one of these -- return the first
		EngineeringValue ret;
		if (exponent == 0.0)
			ret = newValue(1.0);
		else {
			// Non-trivial case
			final double absExponent = Math.abs(exponent);
			ret = newValue(Math.pow(getValue(), absExponent), getAngle() * absExponent);
			if (exponent < 0.0)
				// Handle negative exponents correctly
				ret = newValue(1.0, 0.0).divide(ret);
		}
		return ret;
	}
	@Override
	public EngineeringValue subtract(final EngineeringValue other) {
		return newRectangularValue(getReal() - other.getReal(),
			getImaginary() - other.getImaginary());
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
		format.append(" @ %.1f\u00B0");
		return String.format(format.toString(), getSIPrefix(), getUnits(), getAngle());
	}
}
