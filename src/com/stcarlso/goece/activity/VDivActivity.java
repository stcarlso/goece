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

package com.stcarlso.goece.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ResSeriesSpinner;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.ResCandidate;
import com.stcarlso.goece.utility.Units;

import java.util.*;

/**
 * Allows computations of voltage dividers, and determination of resistor values to make a
 * certain ratio.
 */
public class VDivActivity extends ChildActivity {
	/**
	 * Calculates the voltage division ratio between two series-connected resistors.
	 *
	 * @param r1 the top resistor value
	 * @param r2 the bottom resistor value
	 * @return the ratio of the voltage in between to the voltage supplied
	 */
	public static double vdiv(final double r1, final double r2) {
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

	/**
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;
	/**
	 * Reference to the current flowing through the divider.
	 */
	private TextView currentCtrl;
	/**
	 * Reference to the equivalent resistance of this pair.
	 */
	private TextView equivCtrl;
	/**
	 * Reference to the power dissipated by the divider.
	 */
	private TextView powerCtrl;
	/**
	 * Reference to resistor series to use (1%, 5%, ...)
	 */
	private ResSeriesSpinner seriesCtrl;

	public VDivActivity() {
		controls = new ValueBoxContainer();
	}
	/**
	 * Searches for the best resistor pair matching the template. The resistor series specified
	 * in the UI is used.
	 *
	 * @param template the template specifying series or parallel and the target value
	 * @return the best matching candidate pair
	 */
	private ResCandidate doCalculate(final ResCandidate template) {
		final double[] candidate = template.generateValues(seriesCtrl.getSeries());
		// "One-direction" search only uses N time!
		int start = 1;
		ResCandidate best = template.create(0.0, Double.POSITIVE_INFINITY), hi;
		for (double value : candidate) {
			hi = null;
			// Iterate through resistors until we pass the desired ratio
			while (start < candidate.length && (hi = template.create(value,
					candidate[start])).possible())
				start++;
			// Try that value
			if (hi != null) {
				if (hi.compareTo(best) < 0) best = hi;
				// And the one just below it
				final ResCandidate low = template.create(value, candidate[start - 1]);
				if (low.compareTo(best) < 0) best = low;
			}
		}
		return best;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vdiv);
		currentCtrl = asTextView(R.id.guiDivCurrent);
		equivCtrl = asTextView(R.id.guiDivEquiv);
		powerCtrl = asTextView(R.id.guiDivPower);
		seriesCtrl = (ResSeriesSpinner)findViewById(R.id.guiDivResSeries);
		// Load controls and preferences
		controls.add(findViewById(R.id.guiDivTop));
		controls.add(findViewById(R.id.guiDivBottom));
		controls.add(findViewById(R.id.guiDivInput));
		controls.add(findViewById(R.id.guiDivOutput));
		controls.setupAll(this);
		seriesCtrl.setOnCalculateListener(this);
		registerAdjustable(seriesCtrl);
		loadPrefs();
		// Recalculate everything
		updateOutputs();
	}
	protected void recalculate(ValueGroup group) {
		// Raw values
		final double vin = controls.getRawValue(R.id.guiDivInput);
		final double vout = controls.getRawValue(R.id.guiDivOutput);
		final double r1 = controls.getRawValue(R.id.guiDivTop);
		final double r2 = controls.getRawValue(R.id.guiDivBottom);
		final double ratio = vdiv(r1, r2), vr = vout / vin;
		final int id = group.leastRecentlyUsed();
		switch (id) {
		case R.id.guiDivInput:
			// Update input
			controls.setRawValue(R.id.guiDivInput, vout / ratio);
			break;
		case R.id.guiDivOutput:
			// Update output
			controls.setRawValue(R.id.guiDivOutput, vin * ratio);
			break;
		case R.id.guiDivTop:
		case R.id.guiDivBottom:
			if (vin <= vout) {
				// Set bottom to infinite and top to zero (get as close as we can)
				controls.setRawValue(R.id.guiDivTop, 0.0);
				controls.setRawValue(R.id.guiDivBottom, Double.POSITIVE_INFINITY);
			} else if (vout <= 0.0 || vin <= 0.0) {
				// Set top to infinite and bottom to zero
				controls.setRawValue(R.id.guiDivTop, Double.POSITIVE_INFINITY);
				controls.setRawValue(R.id.guiDivBottom, 0.0);
			} else {
				// Update resistor ladder
				final ResCandidate cand = doCalculate(new DividerCandidate(r1, r2, vr));
				controls.setRawValue(R.id.guiDivTop, cand.getR1());
				controls.setRawValue(R.id.guiDivBottom, cand.getR2());
			}
			break;
		default:
			// Invalid
			break;
		}
		updateOutputs();
	}
	protected void update(ValueGroup group) {
	}
	/**
	 * Updates the current, equivalent resistance, and power outputs
	 */
	private void updateOutputs() {
		final double r1 = controls.getRawValue(R.id.guiDivTop);
		final double r2 = controls.getRawValue(R.id.guiDivBottom);
		final double v = controls.getRawValue(R.id.guiDivInput);
		// R total = R1 + R2, current = V / R, power = V * V / R
		final double er = r1 + r2, current = v / er;
		equivCtrl.setText("Equivalent resistance: " + new EngineeringValue(er,
			Units.RESISTANCE));
		currentCtrl.setText("Current flow: " + new EngineeringValue(current, Units.CURRENT));
		powerCtrl.setText("Power dissipation: " + new EngineeringValue(current * v,
			Units.POWER));
	}

	/**
	 * Represents a pair of resistors that are a candidate for the best ratio match.
	 */
	protected static class DividerCandidate extends ResCandidate {
		/**
		 * The total resistance ideal for a divider, to break ties between the many equivalent
		 * ratios in a given decade.
		 */
		public static final double IDEAL_TOTAL = 50000.0;

		/**
		 * Creates a new candidate resistor pair.
		 *
		 * @param r1 the first resistor value
		 * @param r2 the second resistor value
		 * @param target the target value to be achieved
		 */
		public DividerCandidate(double r1, double r2, double target) {
			super(r1, r2, Math.scalb(Math.rint(Math.scalb(vdiv(r1, r2), 20)), -20), target);
		}
		public int compareTo(ResCandidate other) {
			int test = super.compareTo(other);
			if (test == 0 && (other instanceof DividerCandidate)) {
				final DividerCandidate o = (DividerCandidate)other;
				// Target resistance in the 10K range, we want smaller distances to go in front
				test = Double.compare(distanceFromIdeal(), o.distanceFromIdeal());
			}
			return test;
		}
		public ResCandidate create(double r1, double r2) {
			return new DividerCandidate(r1, r2, getTarget());
		}
		/**
		 * Returns a number that is larger the farther away the resistor pair's total value is
		 * away from the "ideal" total value. Since 0.1 / 0.1 or 8.2 M / 8.2 M dividers are
		 * rarely useful, give something saner like 10K / 10K an edge when equal.
		 *
		 * @return the distance of this value from the ideal total
		 */
		private double distanceFromIdeal() {
			// We do not want to absolutely murder far-off totals, but since log is 1-1...
			return Math.abs(getR1() + getR2() - IDEAL_TOTAL);
		}
		protected void populateValues(int[] values, List<Double> candidates) {
			final int maxIndex = 8 * values.length + 1;
			// All values are valid, who knows how lopsided the ratio could be
			for (int i = 0; i < maxIndex; i++) {
				final double cv = ordinalValue(i, values);
				candidates.add(cv);
			}
		}
		public boolean possible(double candidate) {
			// Use our R1 and R2, see if we can increase R2 without going over
			return getValue() <= getTarget();
		}
	}
}
