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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Calculate the reactance of capacitors and inductors at a given frequency, and perform angle
 * and magnitude calculations of the complex impedance.
 */
public class ImpedanceActivity extends ChildActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.impedance);
		setupValueEntryBox(R.id.guiImpedCap);
		setupValueEntryBox(R.id.guiImpedFreq);
		setupValueEntryBox(R.id.guiImpedImp);
		setupValueEntryBox(R.id.guiImpedInd);
		setupValueEntryBox(R.id.guiImpedPha);
		setupValueEntryBox(R.id.guiImpedReact);
		setupValueEntryBox(R.id.guiImpedRes);
		loadPrefs();
		recalculate(findViewById(R.id.guiImpedRes));
	}
	public void recalculate(View source) {

	}
}