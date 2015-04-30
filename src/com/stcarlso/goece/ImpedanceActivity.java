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

package com.stcarlso.goece;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

/**
 * Calculate the reactance of capacitors and inductors at a given frequency, and perform angle
 * and magnitude calculations of the complex impedance.
 */
public class ImpedanceActivity extends ChildActivity implements View.OnClickListener {
	/**
	 * Recalculates the bottom 3 fields, given the value of reactance.
	 *
	 * @param react the reactance value to use
	 */
	private void doBottomFromReactance(final double react) {
		final double r = ((ValueEntryBox)findViewById(R.id.guiImpedRes)).getRawValue();
		final double mag = Math.hypot(r, react), phase = Math.toDegrees(Math.atan2(react, r));
		// Update reactance
		setValueEntry(findViewById(R.id.guiImpedReact), react);
		// Impedance is magnitude of resistance and reactance
		setValueEntry(findViewById(R.id.guiImpedImp), mag);
		// Phase is direction of resistance and reactance (atan2)
		setValueEntry(findViewById(R.id.guiImpedPha), phase);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.impedance);
		setupValueEntryBox(R.id.guiImpedRes);
		setupValueEntryBox(R.id.guiImpedCap);
		setupValueEntryBox(R.id.guiImpedInd);
		setupValueEntryBox(R.id.guiImpedFreq);
		setupValueEntryBox(R.id.guiImpedPha);
		setupValueEntryBox(R.id.guiImpedReact);
		setupValueEntryBox(R.id.guiImpedImp);
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
		// Was the control on the bottom half or the top half?
		final int id = pushAdjustment(source);
		switch (id) {
		case R.id.guiImpedRes:
			// Resistance
			break;
		case R.id.guiImpedCap:
			// Capacitance
			break;
		case R.id.guiImpedInd:
			// Inductance
			break;
		case R.id.guiImpedFreq:
			// Frequency
			break;
		case R.id.guiImpedReact:
			// Reactance
			break;
		case R.id.guiImpedPha:
			// Phase
			break;
		case R.id.guiImpedImp:
			// Impedance
			break;
		default:
			// Invalid
			break;
		}
	}
}