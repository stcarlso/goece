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

package com.stcarlso.goece.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.ComplexValue;
import com.stcarlso.goece.utility.UIFunctions;

/**
 * A button with units that when clicked brings up a ComplexEntryDialog.
 *
 * Yes, there is duplicate code with ValueEntryBox. No, since it is mostly boilerplate, it will
 * not be fixed.
 */
public class ComplexEntryBox extends AbstractEntryBox<ComplexValue> implements
		ComplexEntryDialog.OnCalculateListener {
	/**
	 * Extended description information for complex value fields.
	 */
	private ComplexEntryDialog.ComplexEntryDescriptions complexDesc;

	public ComplexEntryBox(Context context) {
		super(context);
	}
	public ComplexEntryBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ComplexEntryBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	/**
	 * Returns the raw magnitude entered in this value box.
	 *
	 * @return the result of getValue() on the current value
	 */
	public double getRawMagnitude() {
		return value.getValue();
	}
	/**
	 * Returns the phase entered in this value box.
	 *
	 * @return the result of getAngle() on the current value
	 */
	public double getRawAngle() {
		return value.getAngle();
	}
	/**
	 * Gets the current value entered in this value box.
	 *
	 * @return the current value
	 */
	public ComplexValue getValue() {
		return value;
	}
	@Override
	protected void init(final Context context, final AttributeSet attrs) {
		String units = "", desc = "Value", newGroup = "", willAffect = "", reDesc = null,
			imDesc = null, magDesc = null, phaDesc = null;
		double iv = 0.0, ip = 0.0;
		int sf = 3;
		if (attrs != null) {
			// Read attributes for units
			final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ComplexEntryBox, 0, 0);
			try {
				// Read the values and substitute defaults
				units = values.getString(R.styleable.ComplexEntryBox_units);
				desc = values.getString(R.styleable.ComplexEntryBox_description);
				iv = values.getFloat(R.styleable.ComplexEntryBox_value, 0.0f);
				ip = values.getFloat(R.styleable.ComplexEntryBox_phase, 0.0f);
				sf = values.getInt(R.styleable.ComplexEntryBox_sigfigs, 3);
				newGroup = values.getString(R.styleable.ComplexEntryBox_group);
				willAffect = values.getString(R.styleable.ComplexEntryBox_affects);
				// Read descriptions
				reDesc = values.getString(R.styleable.ComplexEntryBox_realDesc);
				imDesc = values.getString(R.styleable.ComplexEntryBox_imagDesc);
				magDesc = values.getString(R.styleable.ComplexEntryBox_magDesc);
				phaDesc = values.getString(R.styleable.ComplexEntryBox_phaDesc);
			} catch (Exception e) {
				Log.e("ComplexEntryBox", "Invalid attributes:", e);
			}
		} else
			// Probably not good
			Log.w("ComplexEntryBox", "No units specified, defaulting to unitless!");
		group = newGroup;
		affects = willAffect;
		// Create value and set text
		description = desc;
		complexDesc = new ComplexEntryDialog.ComplexEntryDescriptions(magDesc, phaDesc,
			reDesc, imDesc, desc);
		setValue(new ComplexValue(iv, ip, 0.0, sf, units));
		// Call superclass method only when the description and value are loaded
		super.init(context, attrs);
	}
	public void loadState(SharedPreferences prefs) {
		final String idS = UIFunctions.getTag(this);
		if (prefs.contains(idS + "_mag") && prefs.contains(idS + "_pha")) {
			final double mag = Double.longBitsToDouble(prefs.getLong(idS + "_mag", 0L));
			final double pha = Double.longBitsToDouble(prefs.getLong(idS + "_pha", 0L));
			// Why floats? Why no doubles in preferences? Android you make me sad!
			if (!Double.isNaN(mag) && !Double.isNaN(pha))
				updateValue(mag, pha);
		}
	}
	public void saveState(SharedPreferences.Editor prefs) {
		final String tag = UIFunctions.getTag(this);
		prefs.putLong(tag + "_mag", Double.doubleToLongBits(getRawMagnitude()));
		prefs.putLong(tag + "_pha", Double.doubleToLongBits(getRawAngle()));
	}
	public void onClick(View v) {
		final String desc = getDescription();
		// Create popup
		final ComplexEntryDialog mutate = ComplexEntryDialog.create(getValue(), complexDesc);
		mutate.setOnCalculateListener(this);
		// Show it, popup will call oncalculate for us on OK
		mutate.show(UIFunctions.getActivity(this).getFragmentManager(), desc);
	}
	@Override
	public void updateValue(final double rawMag, final double rawPhase) {
		setValue(getValue().newValue(rawMag, rawPhase));
	}
	public void updateValue(final double rawValue) {
		updateValue(rawValue, getValue().getAngle());
	}
}
