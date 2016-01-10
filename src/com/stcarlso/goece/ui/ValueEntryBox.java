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

package com.stcarlso.goece.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.UIFunctions;

/**
 * A button with units that when clicked brings up a ValueEntryDialog.
 */
public class ValueEntryBox extends AbstractEntryBox<EngineeringValue> implements
		AbstractEntryDialog.OnCalculateListener {
	public ValueEntryBox(Context context) {
		super(context);
	}
	public ValueEntryBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ValueEntryBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	protected void init(final Context context, final AttributeSet attrs) {
		String units = "", desc = "Value", newGroup = "", willAffect = "";
		double iv = 0.0;
		int sf = 3;
		if (attrs != null) {
			// Read attributes for units
			final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ValueEntryBox, 0, 0);
			try {
				// Read the values and substitute defaults
				units = values.getString(R.styleable.ValueEntryBox_units);
				desc = values.getString(R.styleable.ValueEntryBox_description);
				iv = values.getFloat(R.styleable.ValueEntryBox_value, 0.0f);
				sf = values.getInt(R.styleable.ValueEntryBox_sigfigs, 3);
				newGroup = values.getString(R.styleable.ValueEntryBox_group);
				willAffect = values.getString(R.styleable.ValueEntryBox_affects);
			} catch (Exception e) {
				Log.e("ValueEntryBox", "Invalid attributes:", e);
			}
		} else
			// Probably not good
			Log.w("ValueEntryBox", "No units specified, defaulting to unitless!");
		group = newGroup;
		affects = willAffect;
		// Create value and set text
		description = desc;
		setValue(new EngineeringValue(iv, 0.0, sf, units));
		// Call superclass method only when the description and value are loaded
		super.init(context, attrs);
	}
	public void loadState(SharedPreferences prefs) {
		final String idS = UIFunctions.getTag(this);
		if (prefs.contains(idS)) {
			final double ld = Double.longBitsToDouble(prefs.getLong(idS, 0L));
			// Why floats? Why no doubles in preferences? Android you make me sad!
			if (!Double.isNaN(ld))
				updateValue(ld);
		}
	}
	public void saveState(SharedPreferences.Editor prefs) {
		prefs.putLong(UIFunctions.getTag(this), Double.doubleToLongBits(getRawValue()));
	}
	public void onClick(View v) {
		final String desc = getDescription();
		// Create popup
		final ValueEntryDialog mutate = ValueEntryDialog.create(value, desc);
		mutate.setOnCalculateListener(this);
		// Show it, popup will call oncalculate for us on OK
		mutate.show(UIFunctions.getActivity(this).getFragmentManager(), desc);
	}
	public void updateValue(final double rawValue) {
		setValue(getValue().newValue(rawValue));
	}
}
