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
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.CustomUnit;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.UIFunctions;

import java.util.*;

/**
 * A button with user settable units that when clicked brings up a CustomEntryDialog. The value
 * is always stored in a consistent base unit, but the unit used for display can be changed.
 */
public class CustomEntryBox extends AbstractEntryBox<EngineeringValue> implements
		AbstractEntryDialog.OnCalculateListener {
	/**
	 * Creates a list of custom units from the unit name and conversion factor array.
	 *
	 * @param units the custom unit names and conversion factors (name, factor, name, factor...)
	 * @return a list of CustomUnit objects populated with the right information
	 */
	private static Map<String, CustomUnit> createUnits(final String[] units) {
		final Map<String, CustomUnit> ret = new LinkedHashMap<String, CustomUnit>(units.length);
		for (int i = 0; i < units.length - 1; i += 2) {
			final String name = units[i], factorS = units[i + 1];
			try {
				// Convert to double and instantiate unit
				final double factor = Double.parseDouble(factorS);
				if (name.length() > 0)
					ret.put(name, new CustomUnit(name, factor));
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
	protected Map<String, CustomUnit> customUnits;
	/**
	 * The unit used for display. If null or invalid, the base unit is used.
	 */
	protected CustomUnit displayUnit;

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
		displayUnit = null;
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
			final String unit = prefs.getString(idS + "_unit", null);
			if (unit != null)
				// null if no match found
				displayUnit = customUnits.get(unit);
			// Why floats? Why no doubles in preferences? Android you make me sad!
			if (!Double.isNaN(ld))
				updateValue(ld);
		}
	}
	public void saveState(SharedPreferences.Editor prefs) {
		final String idS = UIFunctions.getTag(this);
		prefs.putLong(idS, Double.doubleToLongBits(getRawValue()));
		// Only store unit if necessary
		if (displayUnit == null)
			prefs.remove(idS + "_unit");
		else
			prefs.putString(idS + "_unit", displayUnit.getUnit());
	}
	public void onClick(View v) {
		final String desc = getDescription();
		// Create popup
		final CustomEntryDialog mutate = CustomEntryDialog.create(value, desc);
		mutate.setOnCalculateListener(this);
		if (customUnits != null) {
			// Add custom units
			for (CustomUnit unit : customUnits.values())
				mutate.addCustomUnit(unit);
			// Preselect the right unit
			if (displayUnit != null)
				mutate.setSelectedUnit(displayUnit);
		}
		// Show it, popup will call oncalculate for us on OK
		mutate.show(UIFunctions.getActivity(this).getFragmentManager(), desc);
	}
	public void onValueChange(EngineeringValue newValue) {
		final EngineeringValue oldValue = value;
		if (newValue != null) {
			final String newUnits = newValue.getUnits();
			if (customUnits == null || oldValue.getUnits().equals(newUnits))
				// No sense in storing "mm" if the base unit is mm!
				displayUnit = null;
			else
				displayUnit = customUnits.get(newUnits);
			// Copy the value only, always update the text in case the unit changed
			setValue(oldValue.newValue(newValue.getValue()));
			updateText();
			// Call listener if needed
			if (!newValue.equals(oldValue))
				callOnCalculateListener();
		}
	}
	/**
	 * Update the button text.
	 */
	protected void updateText() {
		final EngineeringValue ev = getValue();
		final Spanned desc = Html.fromHtml(getDescription());
		final SpannableStringBuilder text = new SpannableStringBuilder();
		// Try to find the display unit, if we fail use the default unit
		final CustomUnit unit = displayUnit;
		final String displayVal;
		double dv = ev.getValue();
		if (unit != null)
			dv = unit.fromBase(dv);
		text.append(desc);
		// Fix the display value if infinity is needed
		if (Double.isInfinite(dv))
			displayVal = (dv > 0.0) ? "\u221E" : "-\u221E";
		else
			displayVal = getResources().getString(R.string.viewRaw, dv);
		// Italicize the name
		text.setSpan(new StyleSpan(Typeface.ITALIC), 0, desc.length(),
			Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		text.append('\n');
		text.append(displayVal);
		text.append(' ');
		text.append(Html.fromHtml((unit != null) ? unit.getUnit() : ev.getUnits()));
		setText(text);
	}
	public void updateValue(final double rawValue) {
		setValue(getValue().newValue(rawValue));
	}
}
