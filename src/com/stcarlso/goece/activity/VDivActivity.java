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
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ResSeriesSpinner;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

/**
 * Allows computations of voltage dividers, and determination of resistor values to make a
 * certain ratio.
 */
public class VDivActivity extends ChildActivity {
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
}
