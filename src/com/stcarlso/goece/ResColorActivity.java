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
import android.view.View;
import android.widget.TextView;

/**
 * Tab for a resistor color code (PTH) value calculator.
 */
public class ResColorActivity extends ChildActivity {
	/**
	 * Shared code between color code and SMD to indicate standard/non-standard values.
	 *
	 * @param value the component value
	 * @param std the text box to update
	 * @return true if the component was a standard value, or false otherwise
	 */
	public static boolean checkEIATable(final EIAValue value, final TextView std) {
		final double res = value.getValue();
		// Is it standard?
		final EIATable.EIASeries series = value.getSeries();
		final boolean isStandard = EIATable.isEIAValue(res, series);
		final String tolStr = EngineeringValue.toleranceToString(value.getTolerance());
		if (isStandard) {
			// In standard series, say so
			std.setTextColor(Color.GREEN);
			std.setText(String.format("Standard %s%% value", tolStr));
		} else {
			// Not in standard series, indicate closest value
			final double closest = EIATable.nearestEIAValue(res, series), errorPct;
			// Calculate % error
			if (res <= 0.0)
				errorPct = 0.0;
			else
				errorPct = 100.0 * (closest - res) / res;
			// Display appropriate message
			std.setTextColor(Color.RED);
			std.setText(String.format("Nearest %s%% value is %s [%+.1f%%]", tolStr,
				new EIAValue(closest, series, 0.0, value.getUnits()), errorPct));
		}
		return isStandard;
	}

	/**
	 * The resistor power-of-10 multiplier for each possible 3rd (4th) band value.
	 */
	public static final double[] MULTIPLIER = new double[] {
		1.0, 10.0, 100.0, 1000.0, 1e4, 1e5, 1e6, 1e7, 1.0, 1.0, 1.0, 0.1, 0.01
	};
	/**
	 * The tolerance for each possible 4th (5th) band value.
	 */
	public static final double[] TOLERANCE = new double[] {
		0.0, Units.TOL_1P, Units.TOL_2P, 0.0, 0.0, 0.005, 0.0025, Units.TOL_P1, 0.0005,
		0.0, Units.TOL_20P, Units.TOL_5P, Units.TOL_10P
	};
	/**
	 * Keeps a copy of the band objects on screen.
	 */
	private final ColorBand[] bands;

	public ResColorActivity() {
		super();
		bands = new ColorBand[5];
	}
	public void recalculate(final View source) {
		final TextView output = (TextView)findViewById(R.id.guiResValue);
		final int tol = bands[4].getValue();
		// Calculate prefix
		int value = bands[0].getValue() * 10 + bands[1].getValue();
		if (bands[2].getValue() < 10)
			// 5 band
			value = value * 10 + bands[2].getValue();
		// Calculate EIA series
		final EIATable.EIASeries series;
		switch (tol) {
		case 2:
			// Red = 2%
			series = EIATable.EIASeries.E48;
			break;
		case 10:
			// None = 20%
			series = EIATable.EIASeries.E6;
			break;
		case 11:
			// Gold = 5%
			series = EIATable.EIASeries.E24;
			break;
		case 12:
			// Silver = 10%
			series = EIATable.EIASeries.E12;
			break;
		default:
			// Most permissive
			series = EIATable.EIASeries.E96;
			break;
		}
		// Calculate multiplier
		final EIAValue finalValue = new EIAValue(value *
			MULTIPLIER[bands[3].getValue()], series, TOLERANCE[tol]);
		output.setText(finalValue.toString());
		// In EIA series?
		checkEIATable(finalValue, (TextView)findViewById(R.id.guiResIsStandard));
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
		// Add click listeners
		for (ColorBand band : bands) {
			band.setOnCalculateListener(this);
			registerAdjustable(band);
		}
		loadPrefs();
		recalculate(bands[4]);
	}
}
