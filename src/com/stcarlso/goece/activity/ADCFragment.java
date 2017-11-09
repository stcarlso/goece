/***********************************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Stephen Carlson
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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.AbstractEntryBox;
import com.stcarlso.goece.ui.ChildFragment;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.ui.ValueOutputField;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

/**
 * An activity which allows ADC values to be interpreted quickly into actual voltages.
 */
public class ADCFragment extends ChildFragment {
	/**
	 * Cached reference to the step size output field.
	 */
	private ValueOutputField stepSizeCtrl;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		recalculate(controls.get(R.id.guiAdcCount));
	}
	@Override
	protected String getTitle(Context parent) {
		return parent.getString(R.string.guiAdcCalc);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.adccalc, container, false);
		stepSizeCtrl = asValueField(view, R.id.guiAdcStep);
		// Register value entry boxes
		controls.add(view, R.id.guiAdcRes, R.id.guiAdcVrefN, R.id.guiAdcVrefP, R.id.guiAdcCount,
			R.id.guiAdcVoltage);
		controls.setupAll(this);
		return view;
	}
	@Override
	protected void recalculate(ValueGroup group) {
		final AbstractEntryBox<?> resEntry = controls.get(R.id.guiAdcRes);
		final AbstractEntryBox<?> countEntry = controls.get(R.id.guiAdcCount);
		final AbstractEntryBox<?> vRefEntry = controls.get(R.id.guiAdcVrefN);
		final AbstractEntryBox<?> voltEntry = controls.get(R.id.guiAdcVoltage);
		// Calculate the best mtach for the the resolution and the total quantity of counts
		final int res = resEntry.getIntValue(2.0, 32.0, getString(R.string.guiAdcBadBits));
		final double totalCounts = Math.pow(2.0, (double)res) - 1.0, vrefN = vRefEntry.
			getRawValue(), vrefP = controls.getRawValue(R.id.guiAdcVrefP);
		// Calculate the step size in volts
		final double vSpan = vrefP - vrefN, stepSize = vSpan / totalCounts;
		vRefEntry.setError(vSpan <= 0.0 ? getString(R.string.guiAdcBadSpan) : null);
		stepSizeCtrl.setValue(new EngineeringValue(stepSize, Units.VOLTAGE));
		switch (group.leastRecentlyUsed()) {
		case R.id.guiAdcVoltage:
			final int counts = countEntry.getIntValue(0.0, totalCounts, getString(R.string.
				guiAdcBadCount, totalCounts));
			// Generate voltage from counts
			voltEntry.updateValue(vrefN + counts * stepSize);
			break;
		case R.id.guiAdcCount:
			final double v = voltEntry.getRawValue(), vdelta = v - vrefN, newCounts;
			// Ensure voltage is in range
			if (v >= vrefN && v <= vrefP) {
				// Generate count from voltage
				if (vSpan <= 0.0)
					newCounts = 0.0;
				else
					newCounts = vdelta * totalCounts / vSpan;
				voltEntry.setError(null);
				countEntry.updateValue(newCounts);
			} else
				voltEntry.setError(getString(R.string.guiAdcBadVolt));
			break;
		default:
			// Invalid
			break;
		}
	}
	@Override
	protected void update(ValueGroup group) {
	}
}