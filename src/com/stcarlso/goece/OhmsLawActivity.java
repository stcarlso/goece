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

/**
 * Very simple Ohm's law activity. Everyone should know it, but this adds engineering value
 * goodness! (uA, mV, Gohm anyone?)
 */
public class OhmsLawActivity extends ChildActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ohmslaw);
		setupValueEntryBox(R.id.guiOhmsCurrent);
		setupValueEntryBox(R.id.guiOhmsResistance);
		setupValueEntryBox(R.id.guiOhmsVoltage);
	}
	public void recalculate() {
		// TODO Update correct field!
		final ValueEntryBox v = (ValueEntryBox)findViewById(R.id.guiOhmsVoltage);
		final ValueEntryBox i = (ValueEntryBox)findViewById(R.id.guiOhmsCurrent);
		final ValueEntryBox r = (ValueEntryBox)findViewById(R.id.guiOhmsResistance);
		final EngineeringValue volts = v.getValue(), ohms = r.getValue();
		if (ohms.getValue() > 0.0)
			i.setValue(new EngineeringValue(volts.getValue() / ohms.getValue(),
				i.getValue().getUnits()));
	}
}
