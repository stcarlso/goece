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
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

/**
 * Calculate the current capacity of wires and PCB traces.
 */
public class CurCapActivity extends ChildActivity implements AdapterView.OnItemSelectedListener,
		View.OnClickListener {
	/**
	 * Constant used for AWG calculation.
	 */
	private static final double AWG_MULT = 39.0 / Math.log(92.0);
	/**
	 * Constant B used for trace width calculation.
	 */
	private static final double IPC_B = 0.44;
	/**
	 * Constant (1/c) used for trace width calculation.
	 */
	private static final double IPC_C = 1.0 / 0.725;
	/**
	 * Constant B used for trace width calculation. Halved for internal layers.
	 */
	private static final double IPC_K = 0.048;
	/**
	 * Lookup table for diameter (mm) for NEC
	 */
	private static final double[] NEC_DIA = new double[] {
		11.68, 10.40, 9.27, 8.25, 7.35, 6.54, 5.83, 5.19, 4.62, 4.11, 3.67, 3.26, 2.91, 2.59,
		2.30, 2.05, 1.83, 1.63, 1.45, 1.29, 1.15, 1.02, 0.91, 0.81, 0.72, 0.65, 0.57, 0.51,
		0.45, 0.40, 0.36, 0.32, 0.29, 0.25, 0.23, 0.20, 0.18, 0.16, 0.14, 0.13, 0.11, 0.10,
		0.09, 0.08
	};
	private static final double[] NEC_AMPS = new double[] {
		380.00, 328.00, 283.00, 245.00, 211.00, 181.00, 158.00, 135.00, 118.00, 101.00, 89.00,
		73.00, 64.00, 55.00, 47.00, 41.00, 35.00, 32.00, 28.00, 22.00, 19.00, 16.00, 14.00,
		11.00, 9.00, 7.00, 4.70, 3.50, 2.70, 2.20, 1.70, 1.40, 1.20, 0.86, 0.70, 0.53, 0.43,
		0.33, 0.27, 0.21, 0.17, 0.13, 0.11, 0.09
	};
	/**
	 * Resistivity of copper.
	 */
	private static final double RESIST_CU = 1.68E-8;
	/**
	 * Resistivity of each of the items in the materials list.
	 */
	private static final double[] RESIST = new double[] {
		2.82E-8, RESIST_CU, 1.43E-7, 2.44E-8, 1.59E-8
	};

	/**
	 * Calculates the AWG wire whose radius is at least the specified value.
	 *
	 * @param radius the radius in millimeters
	 * @return the smallest radius AWG code which is greater than or equal to the input radius
	 */
	private static double calculateAWG(final double radius) {
		return Math.ceil(36.0 - AWG_MULT * Math.log(radius / 0.0635));
	}

	/**
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;
	/**
	 * Cached reference to the length analysis output box.
	 */
	private TextView lengthOutCtrl;
	/**
	 * Cached reference to the materials selection list.
	 */
	private Spinner materialsCtrl;
	/**
	 * Cached radio button to select wire capacity
	 */
	private RadioButton wireSelCtrl;

	public CurCapActivity() {
		controls = new ValueBoxContainer();
	}
	protected void loadCustomPrefs(SharedPreferences prefs) {
		loadPrefsSpinner(prefs, R.id.guiCurMaterials);
		loadPrefsCheckBox(prefs, R.id.guiCurUseWire);
		loadPrefsCheckBox(prefs, R.id.guiCurUseTrace);
	}
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.curcap);
		lengthOutCtrl = (TextView)findViewById(R.id.guiCurLenInfo);
		materialsCtrl = (Spinner)findViewById(R.id.guiCurMaterials);
		materialsCtrl.setOnItemSelectedListener(this);
		wireSelCtrl = (RadioButton)findViewById(R.id.guiCurUseWire);
		// Load controls and preferences
		controls.add(findViewById(R.id.guiCurCurrent));
		controls.add(findViewById(R.id.guiCurTemp));
		controls.add(findViewById(R.id.guiCurGauge));
		controls.add(findViewById(R.id.guiCurDiameter));
		controls.add(findViewById(R.id.guiCurThickness));
		controls.add(findViewById(R.id.guiCurWidth));
		controls.add(findViewById(R.id.guiCurXArea));
		controls.add(findViewById(R.id.guiCurLength));
		controls.setupAll(this);
		loadPrefs();
		// Recalculate everything
		onClick(wireSelCtrl);
	}
	public void onClick(View v) {
		final boolean isWire = wireSelCtrl.isChecked();
		// Turn off trace params and temp rise in wire mode (future work to allow wire temp?)
		controls.get(R.id.guiCurTemp).setEnabled(!isWire);
		controls.get(R.id.guiCurWidth).setEnabled(!isWire);
		controls.get(R.id.guiCurThickness).setEnabled(!isWire);
		// Turn on wire params in wire mode
		controls.get(R.id.guiCurGauge).setEnabled(isWire);
		controls.get(R.id.guiCurDiameter).setEnabled(isWire);
		controls.get(R.id.guiCurXArea).setEnabled(isWire);
		recalculate(controls.get(isWire ? R.id.guiCurGauge : R.id.guiCurWidth));
	}
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		recalculate(controls.get(R.id.guiCurCurrent));
	}
	public void onNothingSelected(AdapterView<?> parent) { }
	protected void recalculate(ValueGroup group) {
		final String name = group.getName();
		final double resist = RESIST[materialsCtrl.getSelectedItemPosition()];
		if (name.equals("outputs")) {
			// Update all outputs
			switch (group.leastRecentlyUsed()) {
			case R.id.guiCurTemp:
				// This can only be true in PCB trace mode
				break;
			case R.id.guiCurCurrent:
				break;
			default:
				// Should not happen
				throw new IllegalStateException("Need new case in recalculate() for outputs");
			}
		} else if (name.equals("inputs")) {
			// Inputs need to be recalculated
			switch (group.leastRecentlyUsed()) {
			case R.id.guiCurWidth:
				// Only available input in trace mode
				final double tempRise = controls.getRawValue(R.id.guiCurTemp);
				final double i = controls.getRawValue(R.id.guiCurCurrent);
				final double thick = controls.getRawValue(R.id.guiCurThickness);
				// From http://www.4pcb.com/trace-width-calculator.html
				// Their formula puts it out in mils^2, we need mm^2
				final double areaMil = Math.pow((i / (IPC_K * Math.pow(tempRise, IPC_B))),
					IPC_C);
				// Scale by conductivity
				final double width = areaMil * 0.0254 * 0.0254 * resist / (RESIST_CU * thick);
				controls.setRawValue(R.id.guiCurWidth, width);
				// If the current is over 35 A, width over 10 mm, dt < 10 or dt > 100, or
				// thickness < 0.017 or thickness > 0.105, then warn the user
				if (i > 35.0 || width > 10.16 || tempRise < 10.0 || tempRise > 100.0 ||
						thick < 0.017 || thick > 0.105)
					controls.get(R.id.guiCurWidth).setError(getResources().getString(R.string.
						guiCurTraceWarn));
				break;
			case R.id.guiCurGauge:
			case R.id.guiCurXArea:
			case R.id.guiCurDiameter:
				// All of these are recalculated together
				// Maybe try a curve fit from http://www.powerstream.com/Wire_Size.htm
				// TODO Right now we cheat and use 500 cmils per amp
				final double area = controls.getRawValue(R.id.guiCurCurrent) * 0.5 * 0.5067;
				final double radius = Math.sqrt(area / Math.PI);
				controls.setRawValue(R.id.guiCurXArea, area);
				controls.setRawValue(R.id.guiCurDiameter, radius * 2);
				controls.setRawValue(R.id.guiCurGauge, calculateAWG(radius));
				break;
			default:
				// Should not happen
				throw new IllegalStateException("Need new case in recalculate() for inputs");
			}
		} else if (name.equals("len"))
			// Update length information
			updateLength();
	}
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		savePrefsSpinner(prefs, R.id.guiCurMaterials);
		savePrefsCheckBox(prefs, R.id.guiCurUseTrace);
		savePrefsCheckBox(prefs, R.id.guiCurUseWire);
	}
	protected void update(ValueGroup group) {
		// Update before recalculate
		final String name = group.getName();
		if (name.equals("inputs")) {
			// All fields in the inputs
			final double radius;
			final int id = group.mostRecentlyUsed();
			switch (id) {
			case R.id.guiCurGauge:
				// Radius in mm = 0.0635 * exp((36 - gauge) * ln(92) / 39)
				radius = 0.0635 * Math.exp((36.0 - controls.getRawValue(id)) / AWG_MULT);
				controls.setRawValue(R.id.guiCurDiameter, radius * 2.0);
				// Area = pi * radius ^ 2
				controls.setRawValue(R.id.guiCurXArea, Math.PI * radius * radius);
				break;
			case R.id.guiCurDiameter:
				// Direct (mm)
				radius = controls.getRawValue(id) / 2.0;
				// AWG = round up (36 - 39 * ln(radius / 0.0635) / ln(92))
				controls.setRawValue(R.id.guiCurGauge, calculateAWG(radius));
				controls.setRawValue(R.id.guiCurXArea, Math.PI * radius * radius);
				break;
			case R.id.guiCurXArea:
				// Radius in mm = sqrt(area / pi)
				radius = Math.sqrt(controls.getRawValue(id) / Math.PI);
				controls.setRawValue(R.id.guiCurDiameter, radius * 2.0);
				controls.setRawValue(R.id.guiCurGauge, calculateAWG(radius));
				break;
			case R.id.guiCurWidth:
				// Nothing to do here
				break;
			default:
				// Should not happen
				throw new IllegalStateException("Need new case in update() for inputs");
			}
		}
	}
	/**
	 * When the length is changed, or something in the inputs is updated, recalculate the
	 * voltage drop, resistance, and power lost.
	 */
	private void updateLength() {
		final double resistivity = RESIST[materialsCtrl.getSelectedItemPosition()];
		final double current = controls.getRawValue(R.id.guiCurCurrent);
		// Remember that X-area is in mm^2
		final double ohmperm;
		if (wireSelCtrl.isChecked())
			// Wire X-area
			ohmperm = resistivity * 1E6 / controls.getRawValue(R.id.guiCurXArea);
		else
			// Trace X-area
			ohmperm = resistivity * 1E6 / (controls.getRawValue(R.id.guiCurThickness) *
				controls.getRawValue(R.id.guiCurWidth));
		final double ohm = controls.getRawValue(R.id.guiCurLength) * ohmperm;
		final double voltDrop = ohm * current;
		// Generate engineering values
		final EngineeringValue resistance = new EngineeringValue(ohm, Units.RESISTANCE);
		final EngineeringValue power = new EngineeringValue(current * voltDrop, Units.POWER);
		final EngineeringValue voltage = new EngineeringValue(voltDrop, Units.VOLTAGE);
		// Update field
		lengthOutCtrl.setText(String.format("Resistance: %s\r\nPower loss: %s\r\n" +
			"Voltage drop: %s", resistance, power, voltage));
	}
}
