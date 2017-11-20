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
import com.stcarlso.goece.utility.*;

/**
 * An activity for designing Pierce crystal oscillator circuits.
 */
public class OscDesignFragment extends ChildFragment {
	/**
	 * Cached reference to the output capacitance text box.
	 */
	private ValueOutputField clCtrl;
	/**
	 * Cached reference to the standard value output text box.
	 */
	private TextView stdCtrl;
	/**
	 * Cached reference to the transconductance text box.
	 */
	private ValueOutputField transconCtrl;

	@Override
	protected String getTitle(Context parent) {
		return parent.getString(R.string.guiOscDesign);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		recalculate(controls.get(R.id.guiOscFrequency));
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.oscdesign, container, false);
		// Update references
		clCtrl = asValueField(view, R.id.guiOscCL);
		stdCtrl = asTextView(view, R.id.guiOscIsStandard);
		transconCtrl = asValueField(view, R.id.guiOscTranscon);
		// Register value entry boxes
		controls.add(view, R.id.guiOscFrequency, R.id.guiOscLoadCap, R.id.guiOscShuntCap,
			R.id.guiOscPinCap, R.id.guiOscESR);
		controls.setupAll(this);
		return view;
	}
	@Override
	protected void recalculate(ValueGroup group) {
		final double f = controls.getRawValue(R.id.guiOscFrequency);
		final double cRated = controls.getRawValue(R.id.guiOscLoadCap);
		final double c0 = controls.getRawValue(R.id.guiOscShuntCap);
		final double cStray = controls.getRawValue(R.id.guiOscPinCap);
		final double esr = controls.getRawValue(R.id.guiOscESR);
		// Capacitance must be non-zero
		if (cStray >= cRated || cRated < 1.0E-12 || c0 < 1.0E-12)
			controls.get(R.id.guiOscCL).setError(getString(R.string.guiOscBadCap));
		else {
			final double cl = (cRated - cStray) * 2.0;
			clCtrl.setValue(new EngineeringValue(cl, Units.CAPACITANCE));
			// ST AN2867
			final double ctotal = (cRated + c0) * f;
			final double gm = (16.0 * Math.PI * Math.PI) * ctotal * ctotal * esr;
			// Transconductance display
			transconCtrl.setValue(new EngineeringValue(gm, Units.CURRENT + "/" +
				Units.VOLTAGE));
			// Standard capacitor values are E24
			UIFunctions.checkEIATable(new EIAValue(cl, EIATable.EIASeries.E24,
				Units.CAPACITANCE), stdCtrl);
		}
	}
	@Override
	protected void update(ValueGroup group) { }
}
