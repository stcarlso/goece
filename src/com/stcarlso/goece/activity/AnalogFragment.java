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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.MenuFragment;

/**
 * Fragment which displays all items on the Analog tab.
 */
public class AnalogFragment extends MenuFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.analog, container, false);
		// Configure all buttons
		setButtonEvent(view, R.id.guiColorCode, ResColorActivity.class);
		setButtonEvent(view, R.id.guiSMDResistor, SMDResistorActivity.class);
		setButtonEvent(view, R.id.guiOhmsLaw, OhmsLawActivity.class);
		setButtonEvent(view, R.id.guiImpedance, ImpedanceActivity.class);
		setButtonEvent(view, R.id.guiSerPar, SerParActivity.class);
		setButtonEvent(view, R.id.guiVDiv, VDivActivity.class);
		setButtonEvent(view, R.id.guiCurCap, CurCapActivity.class);
		setButtonEvent(view, R.id.guiPcbWidth, PcbTraceActivity.class);
		setButtonEvent(view, R.id.guiPowerUse, PowerUseActivity.class);
		return view;
	}
}