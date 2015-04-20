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

import android.graphics.Color;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Tab for a resistor color code (PTH) value calculator.
 */
public class ResColorCodeActivity extends ChildActivity implements
		NumberPicker.OnValueChangeListener {
	/**
	 * Shared code between color code and SMD to indicate standard/non-standard values.
	 *
	 * @param value the resistor value
	 * @param series the EIA series to check
	 * @param isStandard the text box to update
	 * @return true if the resistor was a standard value, or false otherwise
	 */
	public static boolean checkEIATable(final double value, final EIAResistorTable.EIASeries
			series, final TextView isStandard) {
		final boolean std = EIAResistorTable.isEIAValue(value, series);
		if (std) {
			// In standard series, say so
			isStandard.setTextColor(Color.GREEN);
			isStandard.setText("Standard " + series.toString() + " value");
		} else {
			// Not in standard series, indicate closest value
			final double closest = EIAResistorTable.nearestEIAValue(value, series), errorPct;
			// Calculate % error
			if (value <= 0.0)
				errorPct = 0.0;
			else
				errorPct = 100.0 * (closest - value) / value;
			// Display appropriate message
			isStandard.setTextColor(Color.RED);
			isStandard.setText(String.format("Nearest %s value is %s [%+.1f%%]",
				series.toString(), ECEActivity.formatResistance(closest), errorPct));
		}
		return std;
	}


	/**
	 * The resistor power-of-10 multiplier for each possible 3rd (4th) band value.
	 */
	public static final double[] MULTIPLIER = new double[] {
		1.0, 10.0, 100.0, 1000.0, 1e4, 1e5, 1e6, 1e7, 1.0, 1.0, 1.0, 0.1, 0.01
	};
	public static final String[] TOLERANCE = new String[] {
		"", "1", "2", "", "", "0.5", "0.25", "0.1", "0.05", "", "20", "5", "10"
	};
	/**
	 * Keeps a copy of the band objects on screen.
	 */
	private final ColorBand[] bands;

	public ResColorCodeActivity() {
		super();
		bands = new ColorBand[5];
	}
	public void onValueChange(NumberPicker src, int oldValue, int newValue) {
		recalculate();
	}
	/**
	 * Recalculates the resistor value and checks to see if it is actually available in that
	 * EIA tolerance series.
	 */
	public void recalculate() {
		final TextView output = (TextView)findViewById(R.id.guiResValue);
		final int tol = bands[4].getValue();
		// Calculate prefix
		int value = bands[0].getValue() * 10 + bands[1].getValue();
		if (bands[2].getValue() < 10)
			// 5 band
			value = value * 10 + bands[2].getValue();
		// Calculate multiplier and display
		final double finalValue = MULTIPLIER[bands[3].getValue()] * (double)value;
		output.setText(ECEActivity.formatResistance(finalValue) + " " + ECEActivity.P_M_SYMBOL +
			TOLERANCE[tol] + "%");
		// Calculate EIA series
		final EIAResistorTable.EIASeries series;
		switch (tol) {
		case 2:
			// Red = 2%
			series = EIAResistorTable.EIASeries.E48;
			break;
		case 10:
			// None = 20%
			series = EIAResistorTable.EIASeries.E6;
			break;
		case 11:
			// Gold = 5%
			series = EIAResistorTable.EIASeries.E24;
			break;
		case 12:
			// Silver = 10%
			series = EIAResistorTable.EIASeries.E12;
			break;
		default:
			// Most permissive
			series = EIAResistorTable.EIASeries.E96;
			break;
		}
		// In EIA series?
		checkEIATable(finalValue, series, (TextView)findViewById(R.id.guiResIsStandard));
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rescolorcode);
		// Find band objects
		bands[0] = (ColorBand)findViewById(R.id.guiResBand1);
		bands[1] = (ColorBand)findViewById(R.id.guiResBand2);
		bands[2] = (ColorBand)findViewById(R.id.guiResBand3);
		bands[3] = (ColorBand)findViewById(R.id.guiResBand4);
		bands[4] = (ColorBand)findViewById(R.id.guiResBand5);
		recalculate();
		// Add click listeners
		for (ColorBand band : bands)
			band.setOnValueChangedListener(this);
	}
}
