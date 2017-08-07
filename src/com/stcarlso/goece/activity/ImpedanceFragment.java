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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildFragment;
import com.stcarlso.goece.ui.ComplexEntryBox;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.ComplexValue;

/**
 * Calculate the reactance of capacitors and inductors at a given frequency, and perform angle
 * and magnitude calculations of the complex impedance.
 */
public class ImpedanceFragment extends ChildFragment implements View.OnClickListener {
	/**
	 * Cached radio button to select capacitance (inductance is always the opposite!)
	 */
	private RadioButton capSelCtrl;

	@Override
	protected void loadCustomPrefs(SharedPreferences prefs) {
		super.loadCustomPrefs(prefs);
		loadPrefsCheckBox(prefs, R.id.guiImpedSelCap);
		loadPrefsCheckBox(prefs, R.id.guiImpedSelInd);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// "Click" the check box (does not matter which one, the state is set by isChecked)
		onClick(capSelCtrl);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.impedance, container, false);
		capSelCtrl = asRadioButton(view, R.id.guiImpedSelCap);
		// Resistance is always futile!
		controls.add(view, R.id.guiImpedRes, R.id.guiImpedCap, R.id.guiImpedInd,
			R.id.guiImpedFreq, R.id.guiImpedImp);
		controls.setupAll(this);
		// Register events
		capSelCtrl.setOnClickListener(this);
		asRadioButton(view, R.id.guiImpedSelInd).setOnClickListener(this);
		return view;
	}
	@Override
	public void onClick(View v) {
		// One of the radio buttons was clicked; enable appropriate input
		final boolean isCap = capSelCtrl.isChecked();
		controls.get(R.id.guiImpedCap).setEnabled(isCap);
		controls.get(R.id.guiImpedInd).setEnabled(!isCap);
		recalculate(findValueById(isCap ? R.id.guiImpedCap : R.id.guiImpedInd));
	}
	@Override
	protected void recalculate(ValueGroup source) {
		final ComplexEntryBox imped = (ComplexEntryBox)findValueById(R.id.guiImpedImp);
		// Capacitance or inductance?
		final boolean isCap = capSelCtrl.isChecked();
		final double r = controls.getRawValue(R.id.guiImpedRes);
		final double c = controls.getRawValue(R.id.guiImpedCap);
		final double l = controls.getRawValue(R.id.guiImpedInd);
		final double f = controls.getRawValue(R.id.guiImpedFreq);
		final ComplexValue z = imped.getValue();
		double y = z.getImaginary();
		// Was the control on the bottom half or the top half?
		final int id = source.leastRecentlyUsed();
		switch (id) {
		case R.id.guiImpedCap:
			// Capacitance
			controls.setRawValue(R.id.guiImpedRes, z.getReal());
			controls.setRawValue(R.id.guiImpedCap, 1.0 / (y * f));
			break;
		case R.id.guiImpedInd:
			// Inductance
			controls.setRawValue(R.id.guiImpedRes, z.getReal());
			controls.setRawValue(R.id.guiImpedInd, y / f);
			break;
		case R.id.guiImpedFreq:
			// Frequency
			controls.setRawValue(R.id.guiImpedRes, z.getReal());
			if (isCap)
				// Set from capacitance
				controls.setRawValue(R.id.guiImpedFreq, 1.0 / (y * c));
			else
				// Set from inductance
				controls.setRawValue(R.id.guiImpedFreq, y / l);
			break;
		case R.id.guiImpedImp:
			// Impedance
			if (isCap)
				y = 1.0 / (c * f);
			else
				y = l * f;
			imped.setValue(z.newRectangularValue(r, y));
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
	@Override
	protected void update(ValueGroup group) {
		// Now that output group has only one element, everything is OK
	}
}