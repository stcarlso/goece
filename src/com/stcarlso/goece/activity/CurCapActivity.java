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
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.AbstractEntryBox;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;
import com.stcarlso.goece.utility.ValueControl;

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
	 * The maximum valid value for the current flow for the IPC equation.
	 */
	private static final double IPC_MAX_CUR = 35.0;
	/**
	 * The maximum temperature rise for the IPC equation.
	 */
	private static final double IPC_MAX_TEMP = 100.0;
	/**
	 * The maximum trace width (mm) for the IPC equation.
	 */
	private static final double IPC_MAX_WIDTH = 10.16;
	/**
	 * The minimum temperature rise for the IPC equation.
	 */
	private static final double IPC_MIN_TEMP = 10.0;
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
	 * Converts mils^2 to mm^2 = 0.0254 ^ 2
	 */
	private static final double SQ_MILS_TO_MM = 0.00064516;

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
		controls.add(findViewById(R.id.guiCurTest));
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
		recalculate(findValueById(R.id.guiCurCurrent));
	}
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		recalculate(findValueById(R.id.guiCurCurrent));
	}
	public void onNothingSelected(AdapterView<?> parent) { }
	protected void recalculate(ValueGroup group) {
		final String name = group.getName();
		if (name.equals("outputs"))
			recalculateOutputs();
		else if (name.equals("inputs"))
			recalculateInputs();
		else if (name.equals("len"))
			// Update length information
			updateLength();
	}
	/**
	 * Recalculates all inputs.
	 */
	private void recalculateInputs() {
		final ValueGroup group = groups.get("inputs");
		final double resist = RESIST[materialsCtrl.getSelectedItemPosition()] / RESIST_CU;
		// Inputs need to be recalculated
		final AbstractEntryBox<?> tempRiseCtl = controls.get(R.id.guiCurTemp),
			currentCtrl = controls.get(R.id.guiCurCurrent);
		final double i = currentCtrl.getRawValue();
		switch (group.leastRecentlyUsed()) {
		case R.id.guiCurWidth:
			// Only available input in trace mode
			final double dt = tempRiseCtl.getRawValue();
			final double thick = controls.getRawValue(R.id.guiCurThickness);
			// From http://www.4pcb.com/trace-width-calculator.html
			// area = (i / (IPC_K * dt ^ IPC_B)) ^ IPC_C
			// Their formula puts it out in mils^2, we need mm^2
			final double areaMil = Math.pow((i / (IPC_K * Math.pow(dt, IPC_B))), IPC_C);
			// Scale by conductivity
			final double width = areaMil * SQ_MILS_TO_MM * resist / thick;
			currentCtrl.setError(null);
			tempRiseCtl.setError(null);
			if (!Double.isNaN(width))
				// If the user enters 0 degrees rise or 0 thickness, we could crash, fix it
				controls.setRawValue(R.id.guiCurWidth, width);
			// If the current is over 35 A, width over 10 mm, dt < 10 or dt > 100, or
			// thickness < 0.017 or thickness > 0.105, then warn the user
			if (i > IPC_MAX_CUR || width > IPC_MAX_WIDTH || dt < IPC_MIN_TEMP ||
					dt > IPC_MAX_TEMP || thick < 0.017 || thick > 0.105)
				controls.get(R.id.guiCurWidth).setError(getString(R.string.guiCurTraceWarn));
			break;
		case R.id.guiCurGauge:
		case R.id.guiCurXArea:
		case R.id.guiCurDiameter:
			// All of these are recalculated together
			double diameter;
			if (i < 17.5)
				// diameter = (4.181 + sqrt(4.181 * 4.181 - 4 * 21.555 * (0.41 - amps))) /
				//     (2 * 21.555)
				//  ~= sqrt(amps / 21.555 - 0.009615) + 0.09698
				diameter = Math.sqrt(i / 21.555 - 0.009615) + 0.09698;
			else
				// diameter = (-21.7 + sqrt(21.7 * 21.7 + 4 * (5.387 + amps))) / 2 else
				//  ~= sqrt(123.1125 + amps) - 10.85
				diameter = Math.sqrt(123.1125 + i) - 10.85;
			// Rescale for material
			diameter *= Math.sqrt(resist);
			controls.setRawValue(R.id.guiCurXArea, Math.PI * diameter * diameter * 0.25);
			controls.setRawValue(R.id.guiCurDiameter, diameter);
			controls.setRawValue(R.id.guiCurGauge, calculateAWG(diameter * 0.5));
			break;
		default:
			// Should not happen
			break;
		}
	}
	/**
	 * Recalculates all outputs.
	 */
	private void recalculateOutputs() {
		final ValueGroup group = groups.get("outputs");
		final double resist = RESIST[materialsCtrl.getSelectedItemPosition()] / RESIST_CU;
		final AbstractEntryBox<?> widthCtrl = controls.get(R.id.guiCurWidth);
		// Update all outputs
		final double width = widthCtrl.getRawValue(), pcbXArea = width * controls.
			getRawValue(R.id.guiCurThickness) / SQ_MILS_TO_MM, i, dt;
		widthCtrl.setError(null);
		switch (group.leastRecentlyUsed()) {
		case R.id.guiCurTemp:
			final AbstractEntryBox<?> curCtrl = controls.get(R.id.guiCurCurrent);
			// This can only be true in PCB trace mode
			// dt = (i / (area ^ (1 / IPC_C) * IPC_K)) ^ (1 / IPC_B)
			dt = Math.pow(curCtrl.getRawValue() / (Math.pow(pcbXArea / resist, 1.0 /
				IPC_C) * IPC_K), 1.0 / IPC_B);
			controls.setRawValue(R.id.guiCurTemp, dt);
			curCtrl.setError(null);
			if (dt < IPC_MIN_TEMP || dt > IPC_MAX_TEMP || width > IPC_MAX_WIDTH)
				// Warn the user if the temperature rise is extreme
				controls.get(R.id.guiCurTemp).setError(getString(R.string.guiCurCapWarn));
			break;
		case R.id.guiCurCurrent:
			boolean fault = false;
			if (wireSelCtrl.isChecked()) {
				// Curve fit from http://www.powerstream.com/Wire_Size.htm
				// amps = 21.554968 * dia ^ 2 - 4.180921 * dia + 0.410266 for dia < 1 mm
				// amps = 0.999231 * dia ^ 2 + 21.700890 * dia - 5.387531 for dia > 1 mm
				//  ~= dia ^ 2 + 21.7 * dia - 5.387
				final double diameter = controls.getRawValue(R.id.guiCurDiameter) /
					Math.sqrt(resist);
				if (diameter < 1.0)
					// Need to get small gauges right to better precision
					i = Math.max(21.555 * diameter * diameter - 4.181 * diameter + 0.41,
						0.0);
				else
					// Does really well at mid to high ampacities
					i = diameter * diameter + 21.7 * diameter - 5.387;
			} else {
				// i = area ^ (1 / IPC_C) * IPC_K * dt ^ IPC_B
				final AbstractEntryBox<?> tempCtrl = controls.get(R.id.guiCurTemp);
				dt = tempCtrl.getRawValue();
				i = Math.pow(pcbXArea / resist, 1.0 / IPC_C) * IPC_K * Math.pow(dt, IPC_B);
				// Clear temperature errors if present
				tempCtrl.setError(null);
				fault = width > IPC_MAX_WIDTH || dt < IPC_MIN_TEMP || dt > IPC_MAX_TEMP;
			}
			controls.setRawValue(R.id.guiCurCurrent, i);
			if (fault || i <= 0.0)
				// Warn the user if we go under 0 A
				controls.get(R.id.guiCurCurrent).setError(getString(R.string.guiCurCapWarn));
			break;
		default:
			// Should not happen
			break;
		}
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
			default:
				// Should not happen
				break;
			}
		}
	}
	/**
	 * When the length is changed, or something in the inputs is updated, recalculate the
	 * voltage drop, resistance, and power lost.
	 */
	private void updateLength() {
		final double resistivity = RESIST[materialsCtrl.getSelectedItemPosition()];
		final double current = controls.getRawValue(R.id.guiCurTest);
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
