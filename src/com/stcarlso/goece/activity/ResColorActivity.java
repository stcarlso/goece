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

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ColorBand;
import com.stcarlso.goece.ui.CopyListener;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.EIATable;
import com.stcarlso.goece.utility.EIAValue;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

/**
 * Tab for a resistor color code (PTH) value calculator.
 */
public class ResColorActivity extends ChildActivity {
	/**
	 * The resistor power-of-10 multiplier for each possible 3rd (4th) band value.
	 */
	protected static final double[] MULTIPLIER = new double[] {
		1.0, 10.0, 100.0, 1000.0, 1e4, 1e5, 1e6, 1e7, 1.0, 1.0, 1.0, 0.1, 0.01
	};
	/**
	 * The tolerance for each possible 4th (5th) band value.
	 */
	protected static final double[] TOLERANCE = new double[] {
		0.0, Units.TOL_1P, Units.TOL_2P, 0.0, 0.0, 0.005, 0.0025, Units.TOL_P1, 0.0005,
		0.0, Units.TOL_20P, Units.TOL_5P, Units.TOL_10P
	};

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
	 * Cached reference to the band objects on screen.
	 */
	private final ColorBand[] bandCtrl;
	/**
	 * Handles long presses on the output text box.
	 */
	private final CopyListener copyListener;
	/**
	 * Cached reference to the output text box.
	 */
	private TextView outputCtrl;
	/**
	 * Cached reference to the standard value box.
	 */
	private TextView stdCtrl;

	public ResColorActivity() {
		bandCtrl = new ColorBand[5];
		copyListener = new CopyListener(this, "Resistance");
	}
	public void recalculate(final ValueGroup group) {
		final int tol = bandCtrl[4].getValue();
		// Calculate prefix
		int value = bandCtrl[0].getValue() * 10 + bandCtrl[1].getValue();
		if (bandCtrl[2].getValue() < 10)
			// 5 band
			value = value * 10 + bandCtrl[2].getValue();
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
		final EIAValue finalValue = new EIAValue(value * MULTIPLIER[bandCtrl[3].getValue()],
			series, TOLERANCE[tol]);
		outputCtrl.setText(finalValue.toString());
		copyListener.setValue(finalValue);
		// In EIA series?
		checkEIATable(finalValue, stdCtrl);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rescolorcode);
		// Load band objects
		bandCtrl[0] = (ColorBand)findViewById(R.id.guiResBand1);
		bandCtrl[1] = (ColorBand)findViewById(R.id.guiResBand2);
		bandCtrl[2] = (ColorBand)findViewById(R.id.guiResBand3);
		bandCtrl[3] = (ColorBand)findViewById(R.id.guiResBand4);
		bandCtrl[4] = (ColorBand)findViewById(R.id.guiResBand5);
		outputCtrl = asTextView(R.id.guiResValue);
		outputCtrl.setOnLongClickListener(copyListener);
		stdCtrl = asTextView(R.id.guiResIsStandard);
		// Add click listeners
		for (ColorBand band : bandCtrl) {
			band.setOnCalculateListener(this);
			registerAdjustable(band);
		}
		// Display initial value
		loadPrefs();
		recalculate(bandCtrl[4]);
	}
	// All work is done in recalculate()
	protected void update(ValueGroup group) { }
}
