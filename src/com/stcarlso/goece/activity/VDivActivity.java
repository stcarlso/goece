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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.*;
import com.stcarlso.goece.utility.ECECalc;
import com.stcarlso.goece.utility.EIATable;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

import java.util.*;

/**
 * Allows computations of voltage dividers, and determination of resistor values to make a
 * certain ratio.
 */
public class VDivActivity extends ChildActivity implements View.OnClickListener {
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
	 * Reference to the load resistance enable toggle.
	 */
	private CheckBox isLoadCtrl;
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
	private DividerCandidate doCalculate(final DividerCandidate template) {
		final int[] values = EIATable.seriesValues(seriesCtrl.getSeries());
		final int maxIndex = 8 * values.length + 1;
		// All values are valid, who knows how lopsided the ratio could be
		final List<Double> candidate = new ArrayList<Double>(maxIndex);
		for (int i = 0; i < maxIndex; i++) {
			final double cv = ECECalc.ordinalResistor(i, values);
			candidate.add(cv);
		}
		// "One-direction" search only uses N time!
		int start = 1;
		DividerCandidate best = template.create(0.0, Double.POSITIVE_INFINITY), hi;
		for (double value : candidate) {
			hi = null;
			// Iterate through resistors until we pass the desired ratio
			while (start < candidate.size() && (hi = template.create(candidate.get(start),
					value)).getValue() >= template.getTarget())
				start++;
			// Try that value
			if (hi != null) {
				if (hi.compareTo(best) < 0) best = hi;
				// And the one just below it
				final DividerCandidate low = template.create(candidate.get(start - 1), value);
				if (low.compareTo(best) < 0) best = low;
			}
		}
		return best;
	}
	protected void loadCustomPrefs(SharedPreferences prefs) {
		loadPrefsCheckBox(prefs, R.id.guiDivIsLoad);
	}
	public void onClick(View v) {
		final AbstractEntryBox<?> loadR = controls.get(R.id.guiDivLoad);
		loadR.setEnabled(isLoadCtrl.isChecked());
		recalculate(loadR);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vdiv);
		currentCtrl = asTextView(R.id.guiDivCurrent);
		equivCtrl = asTextView(R.id.guiDivEquiv);
		isLoadCtrl = asCheckBox(R.id.guiDivIsLoad);
		powerCtrl = asTextView(R.id.guiDivPower);
		seriesCtrl = (ResSeriesSpinner)findViewById(R.id.guiDivResSeries);
		// Load controls and preferences
		controls.add(findViewById(R.id.guiDivTop));
		controls.add(findViewById(R.id.guiDivBottom));
		controls.add(findViewById(R.id.guiDivInput));
		controls.add(findViewById(R.id.guiDivOutput));
		controls.add(findViewById(R.id.guiDivLoad));
		controls.setupAll(this);
		seriesCtrl.setOnCalculateListener(this);
		registerAdjustable(seriesCtrl);
		loadPrefs();
		// Recalculate everything
		onClick(null);
	}
	protected void recalculate(ValueGroup group) {
		// Raw values
		final double vin = controls.getRawValue(R.id.guiDivInput);
		final double vout = controls.getRawValue(R.id.guiDivOutput);
		final double r1 = controls.getRawValue(R.id.guiDivTop);
		final double r2, rint = controls.getRawValue(R.id.guiDivBottom);
		final double rl;
		// Load resistance compensation
		if (isLoadCtrl.isChecked()) {
			rl = controls.getRawValue(R.id.guiDivLoad);
			r2 = ECECalc.parallelResistance(rint, rl);
		} else {
			rl = Double.POSITIVE_INFINITY;
			r2 = rint;
		}
		final double ratio = ECECalc.voltageDivide(r1, r2), tb, bd;
		final int id = group.leastRecentlyUsed();
		switch (id) {
		case R.id.guiDivInput:
			// Update input
			controls.setRawValue(R.id.guiDivInput, vout / ratio);
			break;
		case R.id.guiDivOutput:
			// Update output
			recalcVout();
			break;
		case R.id.guiDivTop:
		case R.id.guiDivBottom:
			if (vin <= vout) {
				// Set bottom to infinite and top to zero (get as close as we can)
				tb = 0.0;
				bd = Double.POSITIVE_INFINITY;
			} else if (vout <= 0.0 || vin <= 0.0) {
				// Set top to infinite and bottom to zero
				tb = Double.POSITIVE_INFINITY;
				bd = 0.0;
			} else {
				// Calculate new best values
				final DividerCandidate template = new DividerCandidate(r1, r2, vout / vin, rl);
				template.setVoltage(vin);
				final DividerCandidate cand = doCalculate(template);
				tb = cand.getR1();
				bd = cand.getR2();
			}
			// Update the screen
			controls.setRawValue(R.id.guiDivTop, tb);
			controls.setRawValue(R.id.guiDivBottom, bd);
			recalcVout();
			break;
		default:
			// Invalid
			break;
		}
		updateOutputs();
	}
	/**
	 * Recalculates Vout only. This is used to fine tune after resistor changes, and in Vin
	 * updates, so shared here.
	 */
	private void recalcVout() {
		final double vin = controls.getRawValue(R.id.guiDivInput);
		final double r1 = controls.getRawValue(R.id.guiDivTop);
		final double r2, rint = controls.getRawValue(R.id.guiDivBottom);
		final double rl = controls.getRawValue(R.id.guiDivLoad);
		// Load resistance compensation
		if (isLoadCtrl.isChecked())
			r2 = ECECalc.parallelResistance(rint, rl);
		else
			r2 = rint;
		final double ratio = ECECalc.voltageDivide(r1, r2);
		controls.setRawValue(R.id.guiDivOutput, vin * ratio);
	}
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		savePrefsCheckBox(prefs, R.id.guiDivIsLoad);
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
	 * Represents a pair of resistors that are a candidate for the best ratio match. This class
	 * was split from ResCandidate as inheritance was work for no gain...
	 */
	protected static class DividerCandidate implements Comparable<DividerCandidate> {
		/**
		 * The ideal current for a resistor divider, to break ties between the many equivalent
		 * ratios in a given decade. This will target 100 uA by default.
		 */
		public static final double IDEAL_CURRENT = 1E-4;
		/**
		 * The absolute maximum current that will be guessed flowing through a resistor divider.
		 * With load resistances marring an otherwise perfect match, we can get "closer and
		 * closer" by running the impedances down to the milliohm range. <b>No longer!</b> This
		 * limit is about 5 mA which is a lot for a voltage divider!
		 */
		public static final double MAX_CURRENT = 5E-3;

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
		 * The load resistance. Use infinity for none present.
		 */
		private final double rl;
		/**
		 * The input voltage. Used to calculate the target current!
		 */
		private double voltage;

		/**
		 * Creates a new candidate resistor divider pair.
		 *
		 * @param r1 the first resistor value
		 * @param r2 the second resistor value
		 * @param target the target division ratio to be achieved
		 * @param rl the load resistance to this divider, or Double.POSITIVE_INFINITY if none is
		 * present
		 */
		public DividerCandidate(double r1, double r2, double target, final double rl) {
			this.r1 = r1;
			this.r2 = r2;
			this.rl = rl;
			this.target = target;
		}
		public int compareTo(DividerCandidate other) {
			int test;
			if (getCurrent() > MAX_CURRENT)
				// Do not use this value
				test = 1;
			else if (other.getCurrent() > MAX_CURRENT)
				// Do not use that value
				test = -1;
			else {
				// By error
				test = Double.compare(Math.abs(getError()), Math.abs(other.getError()));
				if (test == 0)
					// Target current in the 100 uA range
					test = Double.compare(distanceFromIdeal(), other.distanceFromIdeal());
			}
			return test;
		}
		/**
		 * Creates a copy of this value with the same target, but a different pair of source
		 * values. Intended for better abstraction.
		 *
		 * @param r1n the first resistor value
		 * @param r2n the second resistor value
		 * @return a value of the same class as this one, but with the new r1 and r2 value
		 */
		public DividerCandidate create(double r1n, double r2n) {
			final DividerCandidate ret = new DividerCandidate(r1n, r2n, getTarget(), getLoad());
			ret.setVoltage(getVoltage());
			return ret;
		}
		/**
		 * Returns a number that is larger the farther away the resistor pair's current flow is
		 * away from the "ideal" current flow. Since 0.1 / 0.1 or 8.2 M / 8.2 M dividers are
		 * rarely useful, give something saner like 10K / 10K an edge when equal.
		 *
		 * @return the distance of this value from the ideal total
		 */
		private double distanceFromIdeal() {
			// We do not want to absolutely murder far-off totals, but since log is 1-1...
			return Math.abs(getCurrent() - IDEAL_CURRENT);
		}
		/**
		 * Gets the current that will be passed through this divider. Excludes the current
		 * flowing through the load resistance if present.
		 *
		 * @return the current flow through this divider
		 */
		public double getCurrent() {
			return getVoltage() / (getR1() + getR2());
		}
		/**
		 * Gets the relative error.
		 *
		 * @return the relative error, not as a percentage (0-1)
		 */
		public double getError() {
			// Calculate new ratio
			final double t = getTarget(), num = getValue() - t, error;
			// This regenerates the error with the loaded value
			if (t == 0.0)
				error = ECECalc.ieeeRound(num);
			else
				error = ECECalc.ieeeRound(num / t);
			return error;
		}
		/**
		 * Gets the load resistance.
		 *
		 * @return the load resistance, or Double.POSITIVE_INFINITY if none is present
		 */
		public double getLoad() {
			return rl;
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
		 * Gets the ratio of this divider, including the load resistance.
		 *
		 * @return the divider ratio
		 */
		public double getValue() {
			final double rint = ECECalc.parallelResistance(getLoad(), getR2());
			return ECECalc.voltageDivide(getR1(), rint);
		}
		/**
		 * Gets the voltage that will be split across this divider.
		 *
		 * @return the voltage input
		 */
		public double getVoltage() {
			return voltage;
		}
		/**
		 * Changes the voltage dropped by this divider.
		 *
		 * @param voltage the input voltage
		 */
		public void setVoltage(final double voltage) {
			this.voltage = voltage;
		}
	}
}
