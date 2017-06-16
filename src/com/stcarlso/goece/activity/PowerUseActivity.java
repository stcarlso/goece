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

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

/**
 * An activity for calculating average power use and the resulting battery life.
 */
public class PowerUseActivity extends ChildActivity implements View.OnClickListener {
	/**
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;
	/**
	 * Cached reference to the average current draw text view.
	 */
	private TextView currentDraw;
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

	public PowerUseActivity() {
		controls = new ValueBoxContainer();
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.poweruse);
		// Update references
		currentDraw = asTextView(R.id.guiPwrDraw);
		idleEnableCtrl = asCheckBox(R.id.guiPwrIdleEna);
		runEnableCtrl = asCheckBox(R.id.guiPwrRunEna);
		sleepEnableCtrl = asCheckBox(R.id.guiPwrSleepEna);
		// Register value entry boxes
		controls.add(findViewById(R.id.guiPwrCapacity));
		controls.add(findViewById(R.id.guiPwrRunCur));
		controls.add(findViewById(R.id.guiPwrRunTime));
		controls.add(findViewById(R.id.guiPwrIdleCur));
		controls.add(findViewById(R.id.guiPwrIdleTime));
		controls.add(findViewById(R.id.guiPwrSleepCur));
		controls.add(findViewById(R.id.guiPwrSleepTime));
		controls.add(findViewById(R.id.guiPwrDuration));
		controls.setupAll(this);
		loadPrefs();
		// Initial calculations
		onClick(runEnableCtrl);
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
		currentDraw.setText(getString(R.string.guiPwrIAvg, new EngineeringValue(i,
			Units.CURRENT).toString()));
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