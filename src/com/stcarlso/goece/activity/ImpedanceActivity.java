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
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;

/**
 * Calculate the reactance of capacitors and inductors at a given frequency, and perform angle
 * and magnitude calculations of the complex impedance.
 */
public class ImpedanceActivity extends ChildActivity implements View.OnClickListener {
	/**
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;
	/**
	 * Cached radio button to select capacitance (inductance is always the opposite!)
	 */
	private RadioButton capSelCtrl;

	public ImpedanceActivity() {
		controls = new ValueBoxContainer();
	}
	/**
	 * Recalculates the bottom 3 fields, given the value of reactance.
	 *
	 * @param react the reactance value to use
	 */
	private void doOutputs(final double react) {
		final double r = controls.getRawValue(R.id.guiImpedRes);
		// atan2 handles infinite arguments, hypot should be fine too
		final double mag = Math.hypot(r, react), phase = Math.toDegrees(Math.atan2(react, r));
		controls.setRawValue(R.id.guiImpedReact, react);
		// Impedance is magnitude of resistance and reactance
		controls.setRawValue(R.id.guiImpedImp, mag);
		// Phase is direction of resistance and reactance (atan2)
		controls.setRawValue(R.id.guiImpedPha, phase);
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
		capSelCtrl = asRadioButton(R.id.guiImpedSelCap);
		// Resistance is always futile!
		controls.add(findViewById(R.id.guiImpedRes));
		controls.add(findViewById(R.id.guiImpedCap));
		controls.add(findViewById(R.id.guiImpedInd));
		controls.add(findViewById(R.id.guiImpedFreq));
		controls.add(findViewById(R.id.guiImpedPha));
		controls.add(findViewById(R.id.guiImpedReact));
		controls.add(findViewById(R.id.guiImpedImp));
		controls.setupAll(this);
		loadPrefs();
		// "Click" the check box (does not matter which one, the state is set by isChecked)
		onClick(capSelCtrl);
	}
	public void onClick(View v) {
		// One of the radio buttons was clicked; enable appropriate input
		final boolean isCap = capSelCtrl.isChecked();
		controls.get(R.id.guiImpedCap).setEnabled(isCap);
		controls.get(R.id.guiImpedInd).setEnabled(!isCap);
		recalculate(controls.get(isCap ? R.id.guiImpedCap : R.id.guiImpedInd));
	}
	protected void recalculate(ValueGroup source) {
		// Capacitance or inductance?
		final boolean isCap = capSelCtrl.isChecked();
		final double c = controls.getRawValue(R.id.guiImpedCap);
		final double l = controls.getRawValue(R.id.guiImpedInd);
		final double f = controls.getRawValue(R.id.guiImpedFreq);
		final double y = controls.getRawValue(R.id.guiImpedReact);
		// Was the control on the bottom half or the top half?
		final int id = source.leastRecentlyUsed();
		switch (id) {
		case R.id.guiImpedCap:
			// Capacitance
			controls.setRawValue(R.id.guiImpedCap, 1.0 / (y * f));
			break;
		case R.id.guiImpedInd:
			// Inductance
			controls.setRawValue(R.id.guiImpedInd, y / f);
			break;
		case R.id.guiImpedFreq:
			// Frequency
			if (isCap)
				// Set from capacitance
				controls.setRawValue(R.id.guiImpedFreq, 1.0 / (y * c));
			else
				// Set from inductance
				controls.setRawValue(R.id.guiImpedFreq, y / l);
			break;
		case R.id.guiImpedReact:
		case R.id.guiImpedPha:
		case R.id.guiImpedImp:
			// Reactance, phase, or impedance (recalculate all outputs)
			if (isCap)
				doOutputs(1.0 / (c * f));
			else
				doOutputs(l * f);
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
	protected void update(ValueGroup group) {
		// Output group needs to stay in sync (TODO replace with angle and phase control)
		if ("outputs".equals(group.getName())) {
			final double r = controls.getRawValue(R.id.guiImpedRes);
			final double z = controls.getRawValue(R.id.guiImpedImp);
			final double theta = controls.getRawValue(R.id.guiImpedPha);
			final double y = controls.getRawValue(R.id.guiImpedReact);
			// Find the control which was changed
			switch (group.mostRecentlyUsed()) {
			case R.id.guiImpedPha:
				// Use the phase
				doOutputs(r * Math.tan(Math.toRadians(theta)));
				break;
			case R.id.guiImpedImp:
				// Use the impedance
				doOutputs(Math.sqrt(z * z - r * r));
				break;
			case R.id.guiImpedReact:
				// Use the reactance
				doOutputs(y);
				break;
			default:
				// Invalid
				break;
			}
		}
	}
}