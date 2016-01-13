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

/**
 * A class which uses Brent's method to solve univariate equations.
 */
public class EquationSolver {
	/**
	 * The default tolerance used if not specified.
	 */
	public static final double DEFAULT_TOLERANCE = 1e-13;
	/**
	 * The maximum number of iterations to perform before failing.
	 */
	public static final int MAX_ITERATIONS = 256;

	/**
	 * The Equation to be solved.
	 */
	protected final Equation equation;
	/**
	 * The tolerance to be used when solving the function.
	 */
	protected final double tolerance;

	/**
	 * Creates a new EquationSolver with the default tolerance.
	 *
	 * @param equation the Equation to be solved
	 */
	public EquationSolver(final Equation equation) {
		this(equation, DEFAULT_TOLERANCE);
	}
	/**
	 * Creates a new EquationSolver with the default tolerance.
	 *
	 * @param equation the Equation to be solved
	 */
	public EquationSolver(final Equation equation, final double tolerance) {
		if (equation == null)
			throw new NullPointerException("equation");
		if (tolerance <= 0.0)
			throw new IllegalArgumentException("tolerance out of range");
		this.equation = equation;
		this.tolerance = tolerance;
	}
	/**
	 * Executes Brent's method to solve the equation.
	 *
	 * @param low the minimum X value
	 * @param high the maximum X value
	 * @param fLow the function value at low
	 * @param fHigh the function value at high
	 * @return the solution X value, or NaN if the maximum iteration count is exceeded
	 */
	protected double brent(final double low, final double high, final double fLow,
						   final double fHigh) {
		boolean solved = false;
		// a = contra interval, b = current guess, c = last guess, d = amount to move
		// f[abc] = value of function there
		double a = low, fa = fLow, b = high, fb = fHigh, c = a, fc = fa, d = b - a, e = d;
		for (int i = 0; !solved && i < MAX_ITERATIONS; i++) {
			if (Math.abs(fc) < Math.abs(fb)) {
				a = b;
				b = c;
				c = a;
				fa = fb;
				fb = fc;
				fc = fa;
			}
			final double tol = 2 * getTolerance() * Math.abs(b) + getTolerance();
			final double m = 0.5 * (c - b);
			if (Math.abs(m) <= tol || isValidSolution(b, fb))
				// Done
				solved = true;
			else if (Math.abs(e) < tol || Math.abs(fa) <= Math.abs(fb))
				// Force bisection.
				e = d = m;
			else {
				double s = fb / fa, p, q;
				// The equality test (a == c) is intentional,
				// it is part of the original Brent's method and
				// it should NOT be replaced by proximity test.
				if (Double.compare(a, c) == 0) {
					// Linear interpolation.
					p = 2 * m * s;
					q = 1 - s;
				} else {
					// Inverse quadratic interpolation.
					final double r = fb / fc;
					q = fa / fc;
					p = s * (2 * m * q * (q - r) - (b - a) * (r - 1));
					q = (q - 1) * (r - 1) * (s - 1);
				}
				if (p > 0)
					q = -q;
				else
					p = -p;
				s = e;
				e = d;
				if (p >= 1.5 * m * q - Math.abs(tol * q) || p >= Math.abs(0.5 * s * q))
					// Inverse quadratic interpolation gives a value
					// in the wrong direction, or progress is slow.
					// Fall back to bisection.
					e = d = m;
				else
					d = p / q;
			}
			a = b;
			fa = fb;
			if (Math.abs(d) > tol)
				b += d;
			else if (m > 0)
				b += tol;
			else
				b -= tol;
			fb = eval(b);
			if ((fb > 0 && fc > 0) || (fb <= 0 && fc <= 0)) {
				c = a;
				fc = fa;
				d = b - a;
				e = d;
			}
		}
		if (!solved)
			// Exceeded maximum iterations
			b = Double.NaN;
		return b;
	}
	/**
	 * Convenience method to evaluate the equation.
	 *
	 * @param x the x coordinate to evaluate
	 * @return the value returned from the equation's evaluate method for that value of x
	 */
	public double eval(double x) {
		return equation.eval(x);
	}
	/**
	 * Retrieves the tolerance that this function will try to achieve from zero when solving
	 * an equation. Alternatively, if the solution value changes by less than this fraction of
	 * its current value, the solution will be considered valid.
	 *
	 * @return the tolerance used for solving
	 */
	public double getTolerance() {
		return tolerance;
	}
	/**
	 * Checks to see if the function value is a valid solution.
	 *
	 * @param x the value of x
	 * @return true if the value is a valid solution according to the current tolerance
	 * settings, or false otherwise
	 */
	protected boolean isValidSolution(final double x) {
		return isValidSolution(x, Double.NaN, eval(x));
	}
	/**
	 * Checks to see if the function value is a valid solution.
	 *
	 * @param x the value of x
	 * @param fx the function evaluated at x (to avoid recomputing if already done)
	 * @return true if the value is a valid solution according to the current tolerance
	 * settings, or false otherwise
	 */
	protected boolean isValidSolution(final double x, final double fx) {
		return isValidSolution(x, Double.NaN, eval(x));
	}
	/**
	 * Checks to see if the function value is a valid solution.
	 *
	 * @param x the value of x
	 * @param oldX the value of x in the previous iteration, or Double.NaN to disable checking
	 * relative tolerance
	 * @param fx the function evaluated at x (to avoid recomputing if already done)
	 * @return true if the value is a valid solution according to the current tolerance
	 * settings, or false otherwise
	 */
	protected boolean isValidSolution(final double x, final double oldX, final double fx) {
		return Math.abs(fx) < getTolerance() || (!Double.isNaN(oldX) && Math.abs((x -
			oldX) / x) < getTolerance());
	}
	/**
	 * Attempts to find a solution for the function inside the specified interval.
	 *
	 * @param min the minimum value for a solution
	 * @param max the maximum value for a solution
	 * @return the solution, or Double.NaN if none can be found in the specified interval
	 */
	public double solve(final double min, final double max) {
		return solve(min, max, 0.5 * (min + max));
	}
	/**
	 * Attempts to find a solution for the function inside the specified interval.
	 *
	 * @param min the minimum value for a solution
	 * @param max the maximum value for a solution
	 * @param guess a first stab at the answer
	 * @return the solution, or Double.NaN if none can be found in the specified interval
	 */
	public double solve(final double min, final double max, final double guess) {
		double answer;
		// Well behaved values only
		if (Double.isInfinite(min) || Double.isNaN(min))
			throw new IllegalArgumentException("min");
		if (Double.isInfinite(max) || Double.isNaN(max))
			throw new IllegalArgumentException("max");
		if (Double.isInfinite(guess) || Double.isNaN(guess))
			throw new IllegalArgumentException("guess");
		// Must be ordered
		if (min >= max)
			throw new IllegalArgumentException("min >= max");
		if (guess <= min || guess >= max)
			throw new IllegalArgumentException("guess must be inside interval");
		// Trivial cases
		final double fMin = eval(min), fMax = eval(max), fGuess = eval(guess);
		if (isValidSolution(min, fMin))
			answer = min;
		else if (isValidSolution(max, fMax))
			answer = max;
		else if (isValidSolution(guess, fGuess))
			answer = guess;
		else if (fMin * fGuess < 0.0)
			// Lower half
			answer = brent(min, guess, fMin, fGuess);
		else if (fMax * fGuess < 0.0)
			// Upper half
			answer = brent(guess, max, fGuess, fMax);
		else
			// No sign change detected
			answer = Double.NaN;
		return answer;
	}
	public String toString() {
		return equation.toString();
	}
}