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
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.*;
import com.stcarlso.goece.utility.ComplexValue;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

/**
 * Very simple Ohm's law activity. Everyone should know it, but this adds engineering value
 * goodness! (uA, mV, Gohm anyone?)
 */
public class OhmsLawActivity extends ChildActivity implements View.OnClickListener {
	/**
	 * Cached reference to the "DC" radio button (AC is the opposite)
	 */
	private RadioButton dcCtrl;
	/**
	 * Cached reference to the power factor label.
	 */
	private ValueOutputField powerFactorCtrl;

	@Override
	protected void loadCustomPrefs(SharedPreferences prefs) {
		super.loadCustomPrefs(prefs);
		loadPrefsCheckBox(prefs, R.id.guiOhmsSelAC);
		loadPrefsCheckBox(prefs, R.id.guiOhmsSelDC);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ohmslaw);
		// Update references
		dcCtrl = asRadioButton(R.id.guiOhmsSelDC);
		powerFactorCtrl = asValueField(R.id.guiOhmsPowerFactor);
		// Register value entry boxes
		controls.add(this, R.id.guiOhmsCurrentDC, R.id.guiOhmsResistanceDC,
			R.id.guiOhmsVoltageDC, R.id.guiOhmsCurrentAC, R.id.guiOhmsResistanceAC,
			R.id.guiOhmsVoltageAC, R.id.guiOhmsPowerDC, R.id.guiOhmsPowerAC);
		controls.setupAll(this);
		loadPrefs();
		// onClick handles initial calculations
		onClick(dcCtrl);
	}
	@Override
	public void onClick(View v) {
		final boolean isDC = dcCtrl.isChecked();
		// Show/hide controls and set enabled/disabled based on option
		// Volts
		pick(isDC, R.id.guiOhmsVoltageDC, R.id.guiOhmsVoltageAC);
		// Amps
		pick(isDC, R.id.guiOhmsCurrentDC, R.id.guiOhmsCurrentAC);
		// Ohms
		pick(isDC, R.id.guiOhmsResistanceDC, R.id.guiOhmsResistanceAC);
		// DC power
		final AbstractEntryBox<?> pdc = controls.get(R.id.guiOhmsPowerDC);
		pdc.setEnabled(isDC);
		pdc.setVisibility(isDC ? View.VISIBLE : View.GONE);
		// AC power
		final AbstractEntryBox<?> pac = controls.get(R.id.guiOhmsPowerAC);
		pac.setEnabled(!isDC);
		pac.setVisibility(isDC ? View.GONE : View.VISIBLE);
		powerFactorCtrl.setVisibility(isDC ? View.GONE : View.VISIBLE);
		// Recalculating...
		recalculate(findValueById(isDC ? R.id.guiOhmsVoltageDC : R.id.guiOhmsVoltageAC));
	}
	// Sets the visibility and enable flags of the AC and DC input boxes
	private void pick(final boolean isDC, final int dc, final int ac) {
		final AbstractEntryBox<?> edc = controls.get(dc);
		edc.setEnabled(isDC);
		edc.setVisibility(isDC ? View.VISIBLE : View.GONE);
		final AbstractEntryBox<?> eac = controls.get(ac);
		eac.setEnabled(!isDC);
		eac.setVisibility(isDC ? View.GONE : View.VISIBLE);
	}
	@Override
	public void recalculate(final ValueGroup group) {
		// Select DC or AC
		final boolean isDC = dcCtrl.isChecked();
		final int vID = isDC ? R.id.guiOhmsVoltageDC : R.id.guiOhmsVoltageAC;
		final int iID = isDC ? R.id.guiOhmsCurrentDC : R.id.guiOhmsCurrentAC;
		final int rID = isDC ? R.id.guiOhmsResistanceDC : R.id.guiOhmsResistanceAC;
		final int pID = isDC ? R.id.guiOhmsPowerDC : R.id.guiOhmsPowerAC;
		// Raw values
		final EngineeringValue v = controls.getValue(vID);
		final EngineeringValue i = controls.getValue(iID);
		final EngineeringValue r = controls.getValue(rID);
		final EngineeringValue p = controls.getValue(pID), power;
		final double pv = p.getValue();
		// Push onto stack
		final int id = group.mostRecentlyUsed();
		switch (id) {
		case R.id.guiOhmsVoltageDC:
		case R.id.guiOhmsVoltageAC:
			// Update current, resistance from voltage, power
			power = new ComplexValue(pv, 2 * v.getAngle() - p.getAngle());
			if (v.getValue() <= 0.0) {
				// That is impossible, if there is no voltage then there can be no power
				if (pv > 0.0)
					controls.setRawValue(pID, Double.NaN);
			} else {
				controls.setValue(iID, power.divide(v));
				controls.setValue(rID, v.multiply(v).divide(power));
			}
			break;
		case R.id.guiOhmsCurrentDC:
		case R.id.guiOhmsCurrentAC:
			// Update voltage, resistance from current, power
			power = new ComplexValue(pv, 2 * i.getAngle() + p.getAngle());
			if (i.getValue() <= 0.0) {
				// That is impossible, if there is no current then there can be no power
				if (pv > 0.0)
					controls.setRawValue(pID, Double.NaN);
			} else {
				controls.setValue(vID, power.divide(i));
				if (pv > 0.0)
					// Only touch the resistance if necessary
					controls.setValue(rID, power.divide(i.multiply(i)));
			}
			break;
		case R.id.guiOhmsResistanceDC:
		case R.id.guiOhmsResistanceAC:
			// Update voltage, current from resistance, power
			// Since the resistance and the power factor have the same magnitude, no further
			// information is available to absolutely set voltage and current phases
			power = p.multiply(r).pow(0.5);
			controls.setValue(vID, power);
			if (r.getValue() <= 0.0) {
				// Voltage was already zeroed out
				controls.setRawValue(iID, 0.0);
				// That is impossible, if there is no current then there can be no power
				if (pv > 0.0)
					controls.setRawValue(pID, Double.NaN);
			} else
				controls.setValue(iID, new ComplexValue(p.divide(r).pow(0.5).getValue(),
					power.getAngle() - r.getAngle()));
			break;
		case R.id.guiOhmsPowerDC:
		case R.id.guiOhmsPowerAC:
			// Update power from other values
			power = new ComplexValue(v.getValue() * i.getValue(), r.getAngle());
			controls.setValue(pID, power);
			updatePowerFactor();
			break;
		default:
			// Invalid
			break;
		}
	}
	@Override
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		super.saveCustomPrefs(prefs);
		savePrefsCheckBox(prefs, R.id.guiOhmsSelAC);
		savePrefsCheckBox(prefs, R.id.guiOhmsSelDC);
	}
	@Override
	protected void update(ValueGroup group) {
		// Select DC or AC
		final boolean isDC = dcCtrl.isChecked();
		final int vID = isDC ? R.id.guiOhmsVoltageDC : R.id.guiOhmsVoltageAC;
		final int iID = isDC ? R.id.guiOhmsCurrentDC : R.id.guiOhmsCurrentAC;
		final int rID = isDC ? R.id.guiOhmsResistanceDC : R.id.guiOhmsResistanceAC;
		// Raw values
		final EngineeringValue v = controls.getValue(vID);
		final EngineeringValue i = controls.getValue(iID);
		final EngineeringValue r = controls.getValue(rID);
		// Push onto stack
		final int id = group.leastRecentlyUsed();
		switch (id) {
		case R.id.guiOhmsVoltageDC:
		case R.id.guiOhmsVoltageAC:
			// Update voltage
			controls.setValue(vID, i.multiply(r));
			break;
		case R.id.guiOhmsCurrentDC:
		case R.id.guiOhmsCurrentAC:
			// Update current
			if (r.getValue() <= 0.0)
				controls.setRawValue(iID, Double.NaN);
			else
				controls.setValue(iID, v.divide(r));
			break;
		case R.id.guiOhmsResistanceDC:
		case R.id.guiOhmsResistanceAC:
			// Update resistance
			if (i.getValue() <= 0.0)
				controls.setRawValue(rID, Double.NaN);
			else
				controls.setValue(rID, v.divide(i));
			break;
		case R.id.guiOhmsPowerAC:
		case R.id.guiOhmsPowerDC:
			// Update the power factor box
			updatePowerFactor();
			break;
		default:
			// Invalid
			break;
		}
	}
	/**
	 * Updates the "power factor" text box. Only visible in AC mode.
	 */
	private void updatePowerFactor() {
		final int pID = dcCtrl.isChecked() ? R.id.guiOhmsPowerDC : R.id.guiOhmsPowerAC;
		final EngineeringValue power = controls.getValue(pID);
		final double powerFactor;
		// No NaN power factors
		if (power.getValue() > 0.0)
			powerFactor = Math.abs(power.getReal()) / power.getValue();
		else
			powerFactor = 0.0;
		// Power factor is unitless and has no tolerance
		powerFactorCtrl.setValue(new EngineeringValue(powerFactor));
	}
}
