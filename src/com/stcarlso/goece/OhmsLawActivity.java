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

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Very simple Ohm's law activity. Everyone should know it, but this adds engineering value
 * goodness! (uA, mV, Gohm anyone?)
 */
public class OhmsLawActivity extends ChildActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ohmslaw);
		// Register value entry boxes
		setupValueEntryBox(R.id.guiOhmsCurrent);
		setupValueEntryBox(R.id.guiOhmsResistance);
		setupValueEntryBox(R.id.guiOhmsVoltage);
		loadPrefs();
		recalculate(findViewById(R.id.guiOhmsVoltage));
	}
	public void recalculate(final View source) {
		final ValueEntryBox volts = (ValueEntryBox)findViewById(R.id.guiOhmsVoltage);
		final ValueEntryBox ohms = (ValueEntryBox)findViewById(R.id.guiOhmsResistance);
		final ValueEntryBox amps = (ValueEntryBox)findViewById(R.id.guiOhmsCurrent);
		// Raw values
		final double v = volts.getRawValue(), i = amps.getRawValue(), r = ohms.getRawValue();
		// Push onto stack
		final int id = pushAdjustment(source);
		switch (id) {
		case R.id.guiOhmsVoltage:
			// Update voltage
			setValueEntry(volts, i * r);
			updatePower(i * i * r);
			amps.setError(null);
			ohms.setError(null);
			break;
		case R.id.guiOhmsCurrent:
			// Update current
			if (r > 0.0) {
				setValueEntry(amps, v / r);
				updatePower(v * v / r);
			} else {
				setErrorEntry(amps, R.string.guiOhmsResError);
				updatePower(Double.NaN);
			}
			volts.setError(null);
			ohms.setError(null);
			break;
		case R.id.guiOhmsResistance:
			// Update resistance
			if (i > 0.0) {
				setValueEntry(ohms, v / i);
				updatePower(v * i);
			} else {
				setErrorEntry(ohms, R.string.guiOhmsCurError);
				updatePower(Double.NaN);
			}
			volts.setError(null);
			amps.setError(null);
			break;
		default:
			// Invalid
			break;
		}
	}
	/**
	 * Update the text area with the power dissipated.
	 *
	 * @param power the calculated power in W
	 */
	private void updatePower(final double power) {
		final String label = getString(R.string.power), data;
		final TextView view = (TextView)findViewById(R.id.guiOhmsPower);
		// Power overwhelming
		if (Double.isNaN(power)) {
			data = "Overwhelming!";
			view.setError("Power overwhelming!");
		} else {
			view.setError(null);
			data = new EngineeringValue(power, Units.POWER).toString();
		}
		view.setText(label + ": " + data);
	}
}
