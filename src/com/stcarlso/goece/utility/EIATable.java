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

import java.util.*;

/**
 * Contains tables of the available EIA values and allows rounding to nearest/checking against
 * them.
 */
public final class EIATable {
	/**
	 * EIA E6 (20% tolerance) components
	 */
	private static final int[] E6_VALUES = new int[] {
		100, 150, 220, 330, 470, 680,
	};
	/**
	 * EIA E12 (10% tolerance) components
	 */
	private static final int[] E12_VALUES = new int[] {
		100, 120, 150, 180, 220, 270, 330, 390, 470, 560, 680, 820,
	};
	/**
	 * EIA E24 (5% tolerance) components
	 */
	private static final int[] E24_VALUES = new int[] {
		100, 110, 120, 130, 150, 160, 180, 200, 220, 240, 270, 300, 330, 360, 390, 430, 470,
		510, 560, 620, 680, 750, 820, 910,
	};
	/**
	 * EIA E48 (2% tolerance) components
	 */
	private static final int[] E48_VALUES = new int[] {
		100, 105, 110, 115, 121, 127, 133, 140, 147, 154, 162, 169, 178, 187,
		196, 205, 215, 226, 237, 249, 261, 274, 287, 301, 316, 332, 348, 365,
		383, 402, 422, 442, 464, 487, 511, 536, 562, 590, 619, 649, 681, 715,
		750, 787, 825, 866, 909, 953,
	};
	/**
	 * EIA E96 (1% tolerance) components
	 */
	private static final int[] E96_VALUES = new int[] {
		100, 102, 105, 107, 110, 113, 115, 118, 121, 124, 127, 130, 133, 137,
		140, 143, 147, 150, 154, 158, 162, 165, 169, 174, 178, 182, 187, 191,
		196, 200, 205, 210, 216, 221, 226, 232, 237, 243, 249, 255, 261, 267,
		274, 280, 287, 294, 301, 309, 316, 324, 332, 340, 348, 357, 365, 374,
		383, 392, 402, 412, 422, 432, 442, 453, 464, 475, 487, 499, 511, 523,
		536, 549, 562, 576, 590, 604, 619, 634, 649, 665, 681, 698, 715, 732,
		750, 768, 787, 806, 825, 845, 866, 887, 909, 931, 953, 976,
	};
	/**
	 * All EIA values composed in an easy to use array
	 */
	private static final int[][] EIA_VALUES = new int[][] {
		E6_VALUES, E12_VALUES, E24_VALUES, E48_VALUES, E96_VALUES,
	};

	/**
	 * Convert a code for a newfangled E96 SMD resistor to its proper significand.
	 *
	 * @param code the 2-digit code found on the resistor
	 * @return the 3-digit significand of the E96 value
	 */
	public static int e96SMDCode(final int code) {
		int value = 0;
		if (code >= 0 && code < E96_VALUES.length)
			value = E96_VALUES[code];
		return value;
	}
	/**
	 * Determines whether the component value is a standard EIA value.
	 *
	 * @param value the component value to check
	 * @param series the EIA series to check
	 * @return whether the value (within a small tolerance) is available in that series
	 */
	public static boolean isEIAValue(final double value, final EIASeries series) {
		return value >= 0.0 && Math.abs(nearestEIAValue(value, series) - value) <= value * 1E-5;
	}
	/**
	 * Convert an SMD EIA-96 letter suffix to the correct multiplier.
	 *
	 * @param value the letter suffixing the value
	 * @return the multiplier; note that the first digits have a new meaning too!
	 */
	public static double letterToMultiplier(final char value) {
		final double multiplier;
		switch (value) {
		case 'Z':
			multiplier = 0.001;
			break;
		case 'Y':
		case 'R':
			multiplier = 0.01;
			break;
		case 'X':
		case 'S':
			multiplier = 0.1;
			break;
		case 'A':
			multiplier = 1.0;
			break;
		case 'B':
		case 'H':
			multiplier = 10.;
			break;
		case 'C':
			multiplier = 100.;
			break;
		case 'D':
			multiplier = 1000.;
			break;
		case 'E':
			multiplier = 10000.;
			break;
		case 'F':
			multiplier = 100000.;
			break;
		default:
			// Unknown
			multiplier = 0.0;
			break;
		}
		return multiplier;
	}
	/**
	 * Calculates the nearest EIA standard value for the given component value.
	 *
	 * @param res the required component value
	 * @param series the EIA series to search
	 * @return whether the value (within a small tolerance) is available in that series
	 */
	public static double nearestEIAValue(final double res, final EIASeries series) {
		double closest = res;
		if (res > 0.) {
			final double denom = Math.pow(10., Math.floor(Math.log10(res)) - 2.0);
			final double significand = res / denom;
			// Round it off to the 0.1th place
			final int[] candidates = EIA_VALUES[series.ordinal()];
			final int value = (int)Math.round(significand), index =
				Arrays.binarySearch(candidates, value);
			// Exact matches need not go here
			if (index < 0) {
				final int intendedIndex = -index - 1;
				// Find the nearest above and below
				final int below, above;
				if (intendedIndex > 0)
					below = candidates[intendedIndex - 1];
				else
					below = candidates[candidates.length - 1] / 10;
				if (intendedIndex < candidates.length)
					above = candidates[intendedIndex];
				else
					above = 1000;
				// Closest significand
				final int closeSig = (above - value > value - below) ? below : above;
				closest = (double)closeSig * denom;
			}
		}
		return closest;
	}
	/**
	 * Returns the available values in the EIA series. These are integer values with the first
	 * three digits being significant.
	 *
	 * @param series the resistor series
	 * @return the values in that series
	 */
	public static int[] seriesValues(final EIASeries series) {
		final int[] src = EIA_VALUES[series.ordinal()], ret = new int[src.length];
		// Avoid some caller inadvertently modifying the originals
		System.arraycopy(src, 0, ret, 0, ret.length);
		return ret;
	}

	/**
	 * Available EIA component series for the methods in this class.
	 */
	public enum EIASeries {
		/**
		 * 20 %
		 */
		E6,
		/**
		 * 10 %
		 */
		E12,
		/**
		 * 5 %
		 */
		E24,
		/**
		 * 2 %
		 */
		E48,
		/**
		 * 1 %
		 */
		E96
	}
}
