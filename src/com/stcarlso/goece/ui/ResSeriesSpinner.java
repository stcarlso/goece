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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.*;

/**
 * A combo box wrapper which allows selection of a resistor series.
 */
public class ResSeriesSpinner extends Spinner implements ValueControl,
		AdapterView.OnItemSelectedListener {
	/**
	 * Map selections of the combo box to a resistor value series.
	 */
	private static final EIATable.EIASeries[] SERIES = new EIATable.EIASeries[] {
		EIAValue.E96, EIAValue.E24, EIAValue.E12, EIAValue.E6
	};

	/**
	 * When this spinner is changed, this field is used to determine which group is affected.
	 */
	private String affects;
	/**
	 * The group assigned to this spinner. All members in a group are LRUed to determine which
	 * one is changed when the group is affected.
	 */
	private String group;
	/**
	 * The listener to be called each time the user changes this value.
	 */
	private Calculatable listener;

	public ResSeriesSpinner(Context context) {
		super(context, Spinner.MODE_DROPDOWN);
		init(context, null);
	}
	public ResSeriesSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// We believe that the adapter will pick the right style
		init(context, attrs);
	}
	public ResSeriesSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle, Spinner.MODE_DROPDOWN);
		init(context, attrs);
	}
	/**
	 * Fires the recalculate method of the attached listener, if it exists.
	 */
	protected void callOnCalculateListener() {
		if (listener != null)
			listener.recalculate(this);
	}
	public String getAffects() {
		return affects;
	}
	public String getGroup() {
		return group;
	}
	/**
	 * Gets the currently selected EIA series value. The resources currently allow 1%, 5%, 10%,
	 * and 20% tolerances.
	 *
	 * @return the EIA series selected
	 */
	public EIATable.EIASeries getSeries() {
		return SERIES[getSelectedItemPosition()];
	}
	private void init(final Context context, final AttributeSet attrs) {
		final ArrayAdapter<CharSequence> adapter;
		String newGroup = "", willAffect = "";
		if (attrs != null) {
			// Read attributes for affects and group
			final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ValueTextBox, 0, 0);
			try {
				// Read the values and substitute defaults
				newGroup = values.getString(R.styleable.ResSeriesSpinner_group);
				willAffect = values.getString(R.styleable.ResSeriesSpinner_affects);
			} catch (Exception e) {
				Log.e("ResSeriesSpinner", "Invalid attributes:", e);
			}
		}
		group = newGroup;
		affects = willAffect;
		listener = null;
		if (isInEditMode()) {
			// Sample data
			adapter = new ArrayAdapter<CharSequence>(context, android.R.layout.
				simple_spinner_item);
			adapter.add("X%");
		} else {
			// Load the options, if we have a context
			adapter = ArrayAdapter.createFromResource(context, R.array.guiSerResSeries,
				android.R.layout.simple_spinner_item);
			setOnItemSelectedListener(this);
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		setAdapter(adapter);
		if (!isInEditMode())
			setSelection(1, false);
	}
	public void loadState(SharedPreferences prefs) {
		final String tag = UIFunctions.getTag(this);
		// Only change if the preferences are initialized
		if (prefs.contains(tag))
			setSelection(prefs.getInt(tag, 1), false);
	}
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		callOnCalculateListener();
	}
	public void onNothingSelected(AdapterView<?> parent) {
		callOnCalculateListener();
	}
	public void saveState(SharedPreferences.Editor prefs) {
		prefs.putInt(UIFunctions.getTag(this), getSelectedItemPosition());
	}
	/**
	 * Changes the listener fired when the value is changed and recalculation is required.
	 *
	 * @param listener the listener to be fired
	 */
	public void setOnCalculateListener(final Calculatable listener) {
		this.listener = listener;
	}
}
