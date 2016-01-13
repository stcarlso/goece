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

import android.os.Bundle;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.AbstractEntryBox;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.EngineeringValue;

/**
 * An activity for converting between delta and wye impedance networks. Complex impedances can
 * also be used.
 */
public class DeltaWyeActivity extends ChildActivity {
	/**
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;

	public DeltaWyeActivity() {
		controls = new ValueBoxContainer();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deltawye);
		// Register value entry boxes
		controls.add(findViewById(R.id.guiDelDelta1));
		controls.add(findViewById(R.id.guiDelDelta2));
		controls.add(findViewById(R.id.guiDelDelta3));
		controls.add(findViewById(R.id.guiDelWye1));
		controls.add(findViewById(R.id.guiDelWye2));
		controls.add(findViewById(R.id.guiDelWye3));
		controls.setupAll(this);
		loadPrefs();
	}
	protected void recalculate(ValueGroup group) {
		final EngineeringValue ra, rb, rc, r1, r2, r3;
		// Shared variables
		final AbstractEntryBox<?> d1 = controls.get(R.id.guiDelDelta1);
		final AbstractEntryBox<?> d2 = controls.get(R.id.guiDelDelta2);
		final AbstractEntryBox<?> d3 = controls.get(R.id.guiDelDelta3);
		final AbstractEntryBox<?> w1 = controls.get(R.id.guiDelWye1);
		final AbstractEntryBox<?> w2 = controls.get(R.id.guiDelWye2);
		final AbstractEntryBox<?> w3 = controls.get(R.id.guiDelWye3);
		switch (group.leastRecentlyUsed()) {
		case R.id.guiDelDelta1:
		case R.id.guiDelDelta2:
		case R.id.guiDelDelta3:
			// Delta from wye
			r1 = w1.getValue();
			r2 = w2.getValue();
			r3 = w3.getValue();
			final EngineeringValue rp = r1.multiply(r2).add(r2.multiply(r3)).add(
				r3.multiply(r1));
			// Divide by "opposite"
			ra = rp.divide(r1);
			d1.updateValue(ra.getValue(), ra.getAngle());
			rb = rp.divide(r2);
			d2.updateValue(rb.getValue(), rb.getAngle());
			rc = rp.divide(r3);
			d3.updateValue(rc.getValue(), rc.getAngle());
			break;
		case R.id.guiDelWye1:
		case R.id.guiDelWye2:
		case R.id.guiDelWye3:
			// Wye from delta
			ra = d1.getValue();
			rb = d2.getValue();
			rc = d3.getValue();
			final EngineeringValue rd = ra.add(rb).add(rc);
			// Divide by "opposite"
			r1 = rb.multiply(rc).divide(rd);
			w1.updateValue(r1.getValue(), r1.getAngle());
			r2 = ra.multiply(rc).divide(rd);
			w2.updateValue(r2.getValue(), r2.getAngle());
			r3 = ra.multiply(rb).divide(rd);
			w3.updateValue(r3.getValue(), r3.getAngle());
			break;
		default:
			// Invalid
			break;
		}
	}
	protected void update(ValueGroup group) { }
}