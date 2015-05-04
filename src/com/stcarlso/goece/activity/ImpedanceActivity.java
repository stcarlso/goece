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
import android.widget.RadioButton;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.InAndOutActivity;
import com.stcarlso.goece.ui.ValueEntryBox;

/**
 * Calculate the reactance of capacitors and inductors at a given frequency, and perform angle
 * and magnitude calculations of the complex impedance.
 */
public class ImpedanceActivity extends InAndOutActivity implements View.OnClickListener {
	/**
	 * Recalculates 2 of the bottom 3 fields from the updated value of the just changed field.
	 */
	private void doBottomFromLRU() {
		final double r = ((ValueEntryBox)findViewById(R.id.guiImpedRes)).getRawValue();
		final ValueEntryBox imp = (ValueEntryBox)findViewById(R.id.guiImpedImp);
		final ValueEntryBox pha = (ValueEntryBox)findViewById(R.id.guiImpedPha);
		final ValueEntryBox react = (ValueEntryBox)findViewById(R.id.guiImpedReact);
		switch (mostRecentlyChanged()) {
		case R.id.guiImpedPha:
			// Use the phase
			doBottomFromReactance(r * Math.tan(Math.toRadians(pha.getRawValue())),
				R.string.guiImpedImpError);
			break;
		case R.id.guiImpedImp:
			// Use the impedance
			final double z = imp.getRawValue();
			doBottomFromReactance(Math.sqrt(z * z - r * r), R.string.guiImpedImpError);
			break;
		case R.id.guiImpedReact:
			// Use the reactance
			doBottomFromReactance(react.getRawValue(), R.string.guiImpedReactError);
			break;
		default:
			// Invalid!
			break;
		}
	}
	/**
	 * Recalculates the bottom 3 fields, given the value of reactance.
	 *
	 * @param react the reactance value to use
	 * @param errorID the string to display if the values are invalid
	 */
	private void doBottomFromReactance(final double react, final int errorID) {
		final double r = ((ValueEntryBox)findViewById(R.id.guiImpedRes)).getRawValue();
		final double mag = Math.hypot(r, react), phase = Math.toDegrees(Math.atan2(react, r));
		// Update reactance
		setValueEntry(findViewById(R.id.guiImpedReact), react, errorID);
		// Impedance is magnitude of resistance and reactance
		setValueEntry(findViewById(R.id.guiImpedImp), mag, errorID);
		// Phase is direction of resistance and reactance (atan2)
		setValueEntry(findViewById(R.id.guiImpedPha), phase, errorID);
	}
	@Override
	protected void loadCustomPrefs(SharedPreferences prefs) {
		super.loadCustomPrefs(prefs);
		loadPrefsCheckBox(prefs, R.id.guiImpedSelCap);
		loadPrefsCheckBox(prefs, R.id.guiImpedSelInd);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.impedance);
		// Resistance is always futile!
		setupValueEntryBox(R.id.guiImpedRes);
		setupValueEntryBox(R.id.guiImpedCap, true);
		setupValueEntryBox(R.id.guiImpedInd, true);
		setupValueEntryBox(R.id.guiImpedFreq, true);
		setupValueEntryBox(R.id.guiImpedPha, false);
		setupValueEntryBox(R.id.guiImpedReact, false);
		setupValueEntryBox(R.id.guiImpedImp, false);
		loadPrefs();
		// "Click" the check box (does not matter which one, the state is set by isChecked)
		onClick(findViewById(R.id.guiImpedSelCap));
	}
	public void onClick(View v) {
		// One of the radio buttons was clicked; enable appropriate input
		final boolean isCap = ((RadioButton)findViewById(R.id.guiImpedSelCap)).isChecked();
		findViewById(R.id.guiImpedCap).setEnabled(isCap);
		findViewById(R.id.guiImpedInd).setEnabled(!isCap);
		recalculate(findViewById(isCap ? R.id.guiImpedCap : R.id.guiImpedInd));
	}
	public void recalculate(View source) {
		// Capacitance or inductance?
		final boolean isCap = ((RadioButton)findViewById(R.id.guiImpedSelCap)).isChecked();
		final ValueEntryBox cap = (ValueEntryBox)findViewById(R.id.guiImpedCap);
		final ValueEntryBox ind = (ValueEntryBox)findViewById(R.id.guiImpedInd);
		final ValueEntryBox freq = (ValueEntryBox)findViewById(R.id.guiImpedFreq);
		final ValueEntryBox react = (ValueEntryBox)findViewById(R.id.guiImpedReact);
		// Was the control on the bottom half or the top half?
		final int id = pushAdjustment(source);
		switch (id) {
		case R.id.guiImpedCap:
			// Capacitance
			doBottomFromLRU();
			if (freq.getRawValue() == 0.0)
				setErrorEntry(cap, R.string.guiImpedFreqError);
			else
				setValueEntry(react, 1.0 / (react.getRawValue() * freq.getRawValue()),
					R.string.guiImpedReactError);
			break;
		case R.id.guiImpedInd:
			// Inductance
			doBottomFromLRU();
			setValueEntry(freq, react.getRawValue() / freq.getRawValue(),
				R.string.guiImpedFreqError);
			break;
		case R.id.guiImpedFreq:
			// Frequency
			doBottomFromLRU();
			if (isCap)
				// Set from capacitance
				setValueEntry(freq, 1.0 / (react.getRawValue() * cap.getRawValue()),
					R.string.guiImpedReactError);
			else
				// Set from inductance
				setValueEntry(freq, react.getRawValue() / ind.getRawValue(),
					R.string.guiImpedIndError);
			break;
		case R.id.guiImpedReact:
		case R.id.guiImpedPha:
		case R.id.guiImpedImp:
			// Reactance, phase, or impedance (recalculate all outputs)
			if (isCap) {
				if (cap.getRawValue() == 0.0)
					doBottomFromReactance(Double.NaN, R.string.guiImpedCapError);
				else
					doBottomFromReactance(1.0 / (cap.getRawValue() * freq.getRawValue()),
						R.string.guiImpedFreqError);
			} else
				doBottomFromReactance(ind.getRawValue() * freq.getRawValue(), 0);
			break;
		default:
			// Invalid
			break;
		}
	}
	@Override
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		super.saveCustomPrefs(prefs);
		savePrefsCheckBox(prefs, R.id.guiImpedSelCap);
		savePrefsCheckBox(prefs, R.id.guiImpedSelInd);
	}
}