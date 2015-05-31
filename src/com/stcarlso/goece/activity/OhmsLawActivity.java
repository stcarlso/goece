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
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;

/**
 * Very simple Ohm's law activity. Everyone should know it, but this adds engineering value
 * goodness! (uA, mV, Gohm anyone?)
 */
public class OhmsLawActivity extends ChildActivity {
	/**
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;

	public OhmsLawActivity() {
		controls = new ValueBoxContainer();
	}
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ohmslaw);
		// Register value entry boxes
		controls.add(findViewById(R.id.guiOhmsCurrent));
		controls.add(findViewById(R.id.guiOhmsResistance));
		controls.add(findViewById(R.id.guiOhmsVoltage));
		controls.add(findViewById(R.id.guiOhmsPower));
		controls.setupAll(this);
		loadPrefs();
		recalculate(findValueById(R.id.guiOhmsVoltage));
	}
	public void recalculate(final ValueGroup group) {
		// Raw values
		final double v = controls.getRawValue(R.id.guiOhmsVoltage);
		final double i = controls.getRawValue(R.id.guiOhmsCurrent);
		final double r = controls.getRawValue(R.id.guiOhmsResistance);
		final double p = controls.getRawValue(R.id.guiOhmsPower);
		// Push onto stack
		final int id = group.mostRecentlyUsed();
		switch (id) {
		case R.id.guiOhmsVoltage:
			// Update current, resistance from voltage, power
			controls.setRawValue(R.id.guiOhmsCurrent, p / v);
			controls.setRawValue(R.id.guiOhmsResistance, v * v / p);
			break;
		case R.id.guiOhmsCurrent:
			// Update voltage, resistance from current, power
			controls.setRawValue(R.id.guiOhmsVoltage, p / i);
			controls.setRawValue(R.id.guiOhmsResistance, p / (i * i));
			break;
		case R.id.guiOhmsResistance:
			// Update voltage, current from resistance, power
			controls.setRawValue(R.id.guiOhmsVoltage, Math.sqrt(p * r));
			controls.setRawValue(R.id.guiOhmsCurrent, Math.sqrt(p / r));
			break;
		case R.id.guiOhmsPower:
			// Calculate power
			controls.setRawValue(R.id.guiOhmsPower, v * i);
			break;
		default:
			// Invalid
			break;
		}
	}
	protected void update(ValueGroup group) {
		// Raw values
		final double v = controls.getRawValue(R.id.guiOhmsVoltage);
		final double i = controls.getRawValue(R.id.guiOhmsCurrent);
		final double r = controls.getRawValue(R.id.guiOhmsResistance);
		// Push onto stack
		final int id = group.leastRecentlyUsed();
		switch (id) {
		case R.id.guiOhmsVoltage:
			// Update voltage
			controls.setRawValue(R.id.guiOhmsVoltage, i * r);
			break;
		case R.id.guiOhmsCurrent:
			// Update current
			controls.setRawValue(R.id.guiOhmsCurrent, v / r);
			break;
		case R.id.guiOhmsResistance:
			// Update resistance
			controls.setRawValue(R.id.guiOhmsResistance, v / i);
			break;
		default:
			// Invalid
			break;
		}
	}
}
