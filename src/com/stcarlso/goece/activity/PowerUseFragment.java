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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildFragment;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.ui.ValueOutputField;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

/**
 * An activity for calculating average power use and the resulting battery life.
 */
public class PowerUseFragment extends ChildFragment implements View.OnClickListener {
	/**
	 * Cached reference to the average current draw text view.
	 */
	private ValueOutputField currentDraw;
	/**
	 * Cached reference to the "Idle" check box.
	 */
	private CheckBox idleEnableCtrl;
	/**
	 * Cached reference to the "Run" check box.
	 */
	private CheckBox runEnableCtrl;
	/**
	 * Cached reference to the "Sleep" check box.
	 */
	private CheckBox sleepEnableCtrl;

	@Override
	protected String getTitle(Context parent) {
		return parent.getString(R.string.guiPowerUse);
	}
	@Override
	public void onClick(View v) {
		// Enable controls as needed
		final boolean idleEna = idleEnableCtrl.isChecked(), runEna = runEnableCtrl.isChecked(),
			sleepEna = sleepEnableCtrl.isChecked();
		// Run
		controls.get(R.id.guiPwrRunCur).setEnabled(runEna);
		controls.get(R.id.guiPwrRunTime).setEnabled(runEna);
		// Idle
		controls.get(R.id.guiPwrIdleCur).setEnabled(idleEna);
		controls.get(R.id.guiPwrIdleTime).setEnabled(idleEna);
		// Sleep
		controls.get(R.id.guiPwrSleepCur).setEnabled(sleepEna);
		controls.get(R.id.guiPwrSleepTime).setEnabled(sleepEna);
		// Recalculate output value
		recalculate(controls.get(R.id.guiPwrCapacity));
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Initial calculations
		onClick(runEnableCtrl);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.poweruse, container, false);
		// Update references
		currentDraw = asValueField(view, R.id.guiPwrDraw);
		idleEnableCtrl = asCheckBox(view, R.id.guiPwrIdleEna);
		runEnableCtrl = asCheckBox(view, R.id.guiPwrRunEna);
		sleepEnableCtrl = asCheckBox(view, R.id.guiPwrSleepEna);
		// Register value entry boxes
		controls.add(view, R.id.guiPwrCapacity, R.id.guiPwrRunCur, R.id.guiPwrRunTime,
			R.id.guiPwrIdleCur, R.id.guiPwrIdleTime, R.id.guiPwrSleepCur, R.id.guiPwrSleepTime,
			R.id.guiPwrDuration);
		controls.setupAll(this);
		// Register events
		idleEnableCtrl.setOnClickListener(this);
		runEnableCtrl.setOnClickListener(this);
		sleepEnableCtrl.setOnClickListener(this);
		return view;
	}
	/**
	 * Calculates the average current draw for one period of operation.
	 *
	 * @return the average current consumed
	 */
	protected double calculateIAvg() {
		double totalTime = 0.0, totalEnergy = 0.0, t;
		if (runEnableCtrl.isChecked()) {
			// Run
			t = totalTime = controls.getRawValue(R.id.guiPwrRunTime);
			totalEnergy = controls.getRawValue(R.id.guiPwrRunCur) * t;
		}
		if (idleEnableCtrl.isChecked()) {
			// Run
			t = controls.getRawValue(R.id.guiPwrIdleTime);
			totalTime += t;
			totalEnergy += controls.getRawValue(R.id.guiPwrIdleCur) * t;
		}
		if (sleepEnableCtrl.isChecked()) {
			// Run
			t = controls.getRawValue(R.id.guiPwrSleepTime);
			totalTime += t;
			totalEnergy += controls.getRawValue(R.id.guiPwrSleepCur) * t;
		}
		// Do not divide by zero
		if (totalTime <= 0.0)
			t = 0.0;
		else
			t = totalEnergy / totalTime;
		return t;
	}
	@Override
	protected void recalculate(ValueGroup group) {
		final double i = calculateIAvg();
		// Display average current value
		currentDraw.setValue(new EngineeringValue(i, Units.CURRENT));
		switch (group.leastRecentlyUsed()) {
		case R.id.guiPwrCapacity:
			// Calculate capacity based on desired runtime
			final double rt = controls.getRawValue(R.id.guiPwrDuration);
			controls.setRawValue(R.id.guiPwrCapacity, rt * i / 3600.0);
			break;
		case R.id.guiPwrDuration:
			// Calculate runtime based on available capacity (user responsible for derating!)
			final double cap = controls.getRawValue(R.id.guiPwrCapacity);
			if (i <= 0.0)
				controls.setRawValue(R.id.guiPwrDuration, 0.0);
			else
				controls.setRawValue(R.id.guiPwrDuration, cap * 3600.0 / i);
			break;
		default:
			// Invalid
			break;
		}
	}
	@Override
	protected void update(ValueGroup group) { }
}