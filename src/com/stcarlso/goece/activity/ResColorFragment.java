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
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.*;
import com.stcarlso.goece.utility.EIATable;
import com.stcarlso.goece.utility.EIAValue;
import com.stcarlso.goece.utility.UIFunctions;
import com.stcarlso.goece.utility.Units;

/**
 * An activity for calculating resistor color code (PTH) values.
 */
public class ResColorFragment extends ChildFragment {
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
	 * Cached reference to the band objects on screen.
	 */
	private final ColorBand[] bandCtrl;
	/**
	 * Cached reference to the output text box.
	 */
	private ValueOutputField outputCtrl;
	/**
	 * Cached reference to the standard value box.
	 */
	private TextView stdCtrl;

	public ResColorFragment() {
		bandCtrl = new ColorBand[5];
	}
	@Override
	protected String getTitle(Context parent) {
		return parent.getString(R.string.guiColorCode);
	}
	@Override
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
		outputCtrl.setValue(finalValue);
		// In EIA series?
		UIFunctions.checkEIATable(finalValue, stdCtrl);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Display initial value
		loadPrefs();
		recalculate(bandCtrl[4]);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.rescolorcode, container, false);
		// Load band objects
		bandCtrl[0] = (ColorBand)view.findViewById(R.id.guiResBand1);
		bandCtrl[1] = (ColorBand)view.findViewById(R.id.guiResBand2);
		bandCtrl[2] = (ColorBand)view.findViewById(R.id.guiResBand3);
		bandCtrl[3] = (ColorBand)view.findViewById(R.id.guiResBand4);
		bandCtrl[4] = (ColorBand)view.findViewById(R.id.guiResBand5);
		outputCtrl = asValueField(view, R.id.guiResValue);
		stdCtrl = asTextView(view, R.id.guiResIsStandard);
		// Add click listeners
		for (ColorBand band : bandCtrl) {
			band.setOnCalculateListener(this);
			registerAdjustable(band);
		}
		return view;
	}
	// All work is done in recalculate()
	@Override
	protected void update(ValueGroup group) { }
}
