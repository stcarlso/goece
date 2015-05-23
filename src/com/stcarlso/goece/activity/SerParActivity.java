/**
 * ********************************************************************************************
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
 * ********************************************************************************************
 */

package com.stcarlso.goece.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ResSeriesSpinner;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.EIATable;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

import java.util.*;

/**
 * Activity to allow construction of unusual resistor values from available parts placed in
 * a series or parallel combination.
 */
public class SerParActivity extends ChildActivity {
	/**
	 * Resistors get an "ordinal" value for a particular iteration that goes from 0 to 8 *
	 * values.length to allow triangle iteration.
	 *
	 * @param index the ordinal index
	 * @param values the 3-digit values from EIATable.seriesValues
	 * @return that resistor value (index 0 is 0 ohms)
	 */
	protected static double ordinalValue(final int index, final int[] values) {
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
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;
	/**
	 * Reference to parallel resistor output info.
	 */
	private TextView parOutCtrl;
	/**
	 * Reference to resistor series to use (1%, 5%, ...)
	 */
	private ResSeriesSpinner seriesCtrl;
	/**
	 * Reference to series resistor output info.
	 */
	private TextView serOutCtrl;

	public SerParActivity() {
		controls = new ValueBoxContainer();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serpar);
		parOutCtrl = asTextView(R.id.guiSerParallelOut);
		seriesCtrl = (ResSeriesSpinner)findViewById(R.id.guiSerResSeries);
		serOutCtrl = asTextView(R.id.guiSerSeriesOut);
		// Load controls and preferences
		controls.add(findViewById(R.id.guiSerTarget));
		controls.add(findViewById(R.id.guiSerSeries1));
		controls.add(findViewById(R.id.guiSerSeries2));
		controls.add(findViewById(R.id.guiSerParallel1));
		controls.add(findViewById(R.id.guiSerParallel2));
		controls.setupAll(this);
		seriesCtrl.setOnCalculateListener(this);
		registerAdjustable(seriesCtrl);
		loadPrefs();
		// Recalculate everything
		updateErrors();
	}
	/**
	 * Recalculates the closest match of parallel resistors from the user specified series,
	 * using the target value.
	 */
	private void calculateParallel() {
		final ResCandidate best = doCalculate(new ParallelResCandidate(0.0, 0.0,
			controls.getRawValue(R.id.guiSerTarget)));
		// Display it
		controls.setRawValue(R.id.guiSerParallel1, best.getR1());
		controls.setRawValue(R.id.guiSerParallel2, best.getR2());
		updateErrors();
	}
	/**
	 * Recalculates the closest match of series resistors from the user specified series,
	 * using the target value.
	 */
	private void calculateSeries() {
		final ResCandidate best = doCalculate(new SeriesResCandidate(0.0, 0.0,
			controls.getRawValue(R.id.guiSerTarget)));
		// Display it
		controls.setRawValue(R.id.guiSerSeries1, best.getR1());
		controls.setRawValue(R.id.guiSerSeries2, best.getR2());
		updateErrors();
	}
	/**
	 * Searches for the best resistor pair matching the template. The resistor series specified
	 * in the UI is used.
	 *
	 * @param template the template specifying series or parallel and the target value
	 * @return the best matching candidate pair
	 */
	private ResCandidate doCalculate(final ResCandidate template) {
		double value;
		final double[] candidate = template.generateValues(seriesCtrl.getSeries());
		// "One-direction" search only uses N time!
		int end = candidate.length - 1;
		final double first = candidate[0];
		ResCandidate best = template.create(first, first), low;
		for (int i = 0; i <= end; i++) {
			low = null;
			value = candidate[i];
			// Iterate down to just below it
			while (end >= i && !(low = template.create(value, candidate[end])).possible())
				end--;
			// Try that value
			if (low != null) {
				if (low.compareTo(best) < 0) best = low;
				if (end < candidate.length - 1) {
					// And the one just above it
					final ResCandidate hi = template.create(value, candidate[end + 1]);
					if (hi.compareTo(best) < 0) best = hi;
				}
			}
		}
		return best;
	}
	protected void recalculate(ValueGroup group) {
		final int id = group.leastRecentlyUsed();
		switch (id) {
		case R.id.guiSerTarget:
			final ResCandidate cand;
			// Build the input value
			switch (groups.get("outputs").mostRecentlyUsed()) {
			case R.id.guiSerSeries1:
			case R.id.guiSerSeries2:
				// Series resistor values were changed
				cand = new SeriesResCandidate(controls.getRawValue(R.id.guiSerSeries1),
					controls.getRawValue(R.id.guiSerSeries2), 0.0);
				controls.setRawValue(R.id.guiSerTarget, cand.getValue());
				calculateParallel();
				break;
			case R.id.guiSerParallel1:
			case R.id.guiSerParallel2:
				// Parallel resistor values were changed
				cand = new ParallelResCandidate(controls.getRawValue(R.id.guiSerParallel1),
					controls.getRawValue(R.id.guiSerParallel2), 0.0);
				controls.setRawValue(R.id.guiSerTarget, cand.getValue());
				calculateSeries();
				break;
			default:
				// Invalid
				break;
			}
			break;
		case R.id.guiSerSeries1:
		case R.id.guiSerSeries2:
		case R.id.guiSerParallel1:
		case R.id.guiSerParallel2:
			// Calculate resistances (both)
			calculateSeries();
			calculateParallel();
			break;
		default:
			// Invalid
			break;
		}
	}
	protected void update(ValueGroup group) {
	}
	/**
	 * Update the display value and % error for the series and parallel values (no value box
	 * changes).
	 */
	private void updateErrors() {
		final double target = controls.getRawValue(R.id.guiSerTarget);
		// Series
		final double r1 = controls.getRawValue(R.id.guiSerSeries1);
		final double r2 = controls.getRawValue(R.id.guiSerSeries2);
		final ResCandidate serCand = new SeriesResCandidate(r1, r2, target);
		serOutCtrl.setText(String.format("%s [%+.1f%%]", serCand, 100.0 * serCand.getError()));
		// Parallel
		final double r3 = controls.getRawValue(R.id.guiSerParallel1);
		final double r4 = controls.getRawValue(R.id.guiSerParallel2);
		final ResCandidate parCand = new ParallelResCandidate(r3, r4, target);
		parOutCtrl.setText(String.format("%s [%+.1f%%]", parCand, 100.0 * parCand.getError()));
	}

	/**
	 * Represents a pair of resistors that are a candidate for the best series/parallel match.
	 */
	protected abstract static class ResCandidate implements Comparable<ResCandidate> {
		/**
		 * Relative error value (so 1% = 0.01).
		 */
		private final double error;
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
			final double absError = value - target;
			this.r1 = r1;
			this.r2 = r2;
			this.target = target;
			this.value = value;
			// Do not divide by zero
			if (target == 0.0)
				error = absError;
			else
				error = absError / target;
		}
		public int compareTo(ResCandidate other) {
			// Compare using absolute value of error for quick drill down
			int ret = Double.compare(Math.abs(error), Math.abs(other.error));
			if (ret == 0) {
				// If the same, give the advantage to the side with r1 == r2
				if (Double.compare(getR1(), getR2()) == 0)
					ret = -1;
				else if (Double.compare(other.getR1(), other.getR2()) == 0)
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
			final long temp = Double.doubleToLongBits(Math.abs(error));
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

	/**
	 * Represents a pair of resistors that are a candidate for the best series match.
	 */
	protected static class SeriesResCandidate extends ResCandidate {
		/**
		 * Creates a new candidate resistor pair.
		 *
		 * @param r1 the first resistor value
		 * @param r2 the second resistor value
		 * @param target the target value to be achieved
		 */
		protected SeriesResCandidate(final double r1, final double r2, final double target) {
			super(r1, r2, r1 + r2, target);
		}
		public ResCandidate create(double r1, double r2) {
			return new SeriesResCandidate(r1, r2, getTarget());
		}
		protected void populateValues(int[] values, List<Double> candidates) {
			final int maxIndex = 8 * values.length + 1;
			for (int i = 0; i < maxIndex; i++) {
				final double cv = ordinalValue(i, values);
				// Include the value that fails
				candidates.add(cv);
				if (!possible(cv)) break;
			}
		}
		public boolean possible(double candidate) {
			// If value > candidate, impossible to make a series since series increases R...
			return candidate <= getTarget();
		}
	}

	/**
	 * Represents a pair of resistors that are a candidate for the best parallel match.
	 */
	protected static class ParallelResCandidate extends ResCandidate {
		/**
		 * Calculates the equivalent resistance of two resistors.
		 *
		 * @param r1 the first resistor value
		 * @param r2 the second resistor value
		 * @return the equivalent parallel resistance
		 */
		private static double parallel(final double r1, final double r2) {
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
		 * Creates a new candidate resistor pair.
		 *
		 * @param r1 the first resistor value
		 * @param r2 the second resistor value
		 * @param target the target value to be achieved
		 */
		protected ParallelResCandidate(final double r1, final double r2, final double target) {
			super(r1, r2, parallel(r1, r2), target);
		}
		public ResCandidate create(double r1, double r2) {
			return new ParallelResCandidate(r1, r2, getTarget());
		}
		protected void populateValues(int[] values, List<Double> candidates) {
			final int maxIndex = 8 * values.length + 1;
			for (int i = maxIndex - 1; i >= 0; i--) {
				final double cv = ordinalValue(i, values);
				// Include the value that fails
				candidates.add(cv);
				if (!possible(cv)) break;
			}
		}
		public boolean possible(double candidate) {
			// If value < candidate, impossible to make a parallel since parallel decreases R...
			return candidate >= getTarget();
		}
	}
}