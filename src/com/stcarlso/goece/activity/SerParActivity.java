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

package com.stcarlso.goece.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ResSeriesSpinner;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.ECECalc;
import com.stcarlso.goece.utility.EIAValue;
import com.stcarlso.goece.utility.ResCandidate;
import com.stcarlso.goece.utility.UIFunctions;

import java.util.*;

/**
 * Activity to allow construction of unusual resistor values from available parts placed in
 * a series or parallel combination.
 */
public class SerParActivity extends ChildActivity {
	/**
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;
	/**
	 * Cached reference to the parallel resistor error output box.
	 */
	private TextView parOutCtrl;
	/**
	 * Reference to resistor series to use (1%, 5%, ...)
	 */
	private ResSeriesSpinner seriesCtrl;
	/**
	 * Cached reference to the series resistor error output box.
	 */
	private TextView serOutCtrl;
	/**
	 * Cached reference to whether the resistor is a standard value.
	 */
	private TextView stdCtrl;

	public SerParActivity() {
		controls = new ValueBoxContainer();
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serpar);
		parOutCtrl = asTextView(R.id.guiSerParallelOut);
		seriesCtrl = (ResSeriesSpinner)findViewById(R.id.guiSerResSeries);
		serOutCtrl = asTextView(R.id.guiSerSeriesOut);
		stdCtrl = asTextView(R.id.guiSerIsStandard);
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
		final double serErr = ECECalc.ieeeRound(serCand.getError());
		if (serErr == 0.0)
			// Perfect match
			serOutCtrl.setText(serCand.toString());
		else
			// Small difference
			serOutCtrl.setText(String.format("%s [%+.1f%%]", serCand, 100.0 * serErr));
		// Parallel
		final double r3 = controls.getRawValue(R.id.guiSerParallel1);
		final double r4 = controls.getRawValue(R.id.guiSerParallel2);
		final ResCandidate parCand = new ParallelResCandidate(r3, r4, target);
		final double parErr = ECECalc.ieeeRound(parCand.getError());
		if (parErr == 0.0)
			// Perfect match
			parOutCtrl.setText(parCand.toString());
		else
			// Small difference
			parOutCtrl.setText(String.format("%s [%+.1f%%]", parCand, 100.0 * parErr));
		// Overall fit
		final EIAValue finalValue = new EIAValue(target, seriesCtrl.getSeries());
		UIFunctions.checkEIATable(finalValue, stdCtrl);
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
				final double cv = ECECalc.ordinalResistor(i, values);
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
		 * Creates a new candidate resistor pair.
		 *
		 * @param r1 the first resistor value
		 * @param r2 the second resistor value
		 * @param target the target value to be achieved
		 */
		protected ParallelResCandidate(final double r1, final double r2, final double target) {
			super(r1, r2, ECECalc.parallelResistance(r1, r2), target);
		}
		public ResCandidate create(double r1, double r2) {
			return new ParallelResCandidate(r1, r2, getTarget());
		}
		protected void populateValues(int[] values, List<Double> candidates) {
			final int maxIndex = 8 * values.length + 1;
			for (int i = maxIndex - 1; i >= 0; i--) {
				final double cv = ECECalc.ordinalResistor(i, values);
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