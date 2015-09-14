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
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.UIFunctions;

import java.util.*;

/**
 * A button with user settable units that when clicked brings up a CustomEntryDialog.
 */
public class CustomEntryBox extends AbstractEntryBox<EngineeringValue> implements
		AbstractEntryDialog.OnCalculateListener {
	/**
	 * Creates a list of custom units from the unit name and conversion factor array.
	 *
	 * @param units the custom unit names and conversion factors (name, factor, name, factor...)
	 * @return a list of CustomUnit objects populated with the right information
	 */
	private static List<CustomEntryDialog.CustomUnit> createUnits(final String[] units) {
		final List<CustomEntryDialog.CustomUnit> ret = new LinkedList<CustomEntryDialog.
			CustomUnit>();
		for (int i = 0; i < units.length - 1; i += 2) {
			final String name = units[i], factorS = units[i + 1];
			try {
				// Convert to double and instantiate unit
				final double factor = Double.parseDouble(factorS);
				if (name.length() > 0)
					ret.add(new CustomEntryDialog.CustomUnit(name, factor));
				else
					Log.w("CustomEntryBox", "Empty unit name!");
			} catch (RuntimeException e) {
				Log.w("CustomEntryBox", "Invalid conversion factor: " + factorS);
			}
		}
		return ret;
	}

	/**
	 * List of custom unit options.
	 * If none are provided, only the base unit will be available.
	 */
	protected List<CustomEntryDialog.CustomUnit> customUnits;

	public CustomEntryBox(Context context) {
		super(context);
	}
	public CustomEntryBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public CustomEntryBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	protected void init(final Context context, final AttributeSet attrs) {
		String units = "", desc = "Value", newGroup = "", willAffect = "";
		double iv = 0.0;
		super.init(context, attrs);
		customUnits = null;
		if (attrs != null) {
			// Read attributes for units
			final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.CustomEntryBox, 0, 0);
			try {
				// Read the values and substitute defaults
				units = values.getString(R.styleable.CustomEntryBox_units);
				desc = values.getString(R.styleable.CustomEntryBox_description);
				iv = values.getFloat(R.styleable.CustomEntryBox_value, 0.0f);
				newGroup = values.getString(R.styleable.CustomEntryBox_group);
				willAffect = values.getString(R.styleable.CustomEntryBox_affects);
				// Extract string arrays from resources
				final int customUnitsID = values.getResourceId(R.styleable.
					CustomEntryBox_customUnits, 0);
				if (customUnitsID != 0)
					customUnits = createUnits(getResources().getStringArray(customUnitsID));
			} catch (Exception e) {
				Log.e("CustomEntryBox", "Invalid attributes:", e);
			}
		} else
			// Probably not good
			Log.w("CustomEntryBox", "No units specified, defaulting to unitless!");
		group = newGroup;
		affects = willAffect;
		// Create value and set text
		description = desc;
		setValue(new EngineeringValue(iv, 0.0, units));
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
		if (activity != null) {
			final String desc = getDescription();
			// Create popup
			final CustomEntryDialog mutate = CustomEntryDialog.create(value, desc);
			mutate.setOnCalculateListener(this);
			if (customUnits != null)
				// Add custom units
				for (CustomEntryDialog.CustomUnit unit : customUnits)
					mutate.addCustomUnit(unit);
			// Show it, popup will call oncalculate for us on OK
			mutate.show(activity.getFragmentManager(), desc);
		}
	}
	/**
	 * Update the button text.
	 */
	protected void updateText() {
		final EngineeringValue ev = getValue();
		// Get text
		final CharSequence desc = Html.fromHtml(getDescription());
		final CharSequence val = Html.fromHtml(getResources().getString(R.string.viewRaw,
			ev.getValue(), ev.getUnits()));
		final SpannableStringBuilder text = new SpannableStringBuilder();
		text.append(desc);
		text.append('\n');
		text.append(val);
		// Make the name a bit smaller
		setText(text);
	}
	public void updateValue(final double rawValue) {
		setValue(getValue().newValue(rawValue));
	}
}
