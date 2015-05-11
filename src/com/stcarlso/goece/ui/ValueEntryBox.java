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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.stcarlso.goece.R;
import com.stcarlso.goece.activity.ECEActivity;
import com.stcarlso.goece.utility.Calculatable;
import com.stcarlso.goece.utility.ECESavedState;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.ValueControl;

/**
 * A button with units that when clicked brings up a ValueEntryDialog.
 */
public class ValueEntryBox extends Button implements View.OnClickListener, ValueControl,
		ValueEntryDialog.OnCalculateListener {
	/**
	 * Why android why?
	 */
	private Activity activity;
	/**
	 * When this box is changed, this field is used to determine which group is affected.
	 */
	private String affects;
	/**
	 * Listens for long presses and copies the value.
	 */
	private CopyListener copyListener;
	/**
	 * The description requested for this value box ("Leakage Current", "Turn-On Voltage", ...)
	 */
	private String description;
	/**
	 * The group assigned to this value entry box. All members in a group are LRUed to
	 * determine which one is changed when the group is affected.
	 */
	private String group;
	/**
	 * The listener to be called each time the user changes this value.
	 */
	private Calculatable listener;
	/**
	 * The value stored in this box.
	 */
	private EngineeringValue value;

	public ValueEntryBox(Context context) {
		super(context);
		init(context, null);
	}
	public ValueEntryBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public ValueEntryBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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
	/**
	 * Gets the description of this value box.
	 *
	 * @return the short description (shown on the button)
	 */
	public String getDescription() {
		return description;
	}
	public String getGroup() {
		return group;
	}
	/**
	 * Returns the raw value entered in this value box.
	 *
	 * @return the result of getValue() on the current value
	 */
	public double getRawValue() {
		return value.getValue();
	}
	/**
	 * Gets the current value entered in this value box.
	 *
	 * @return the current value
	 */
	public EngineeringValue getValue() {
		return value;
	}
	private void init(final Context context, final AttributeSet attrs) {
		String units = "", desc = "Value", newGroup = "", willAffect = "";
		double iv = 0.0;
		int sf = 3;
		activity = null;
		// Initialize click listeners
		listener = null;
		setOnClickListener(this);
		setMaxLines(2);
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
	}
	public void loadState(SharedPreferences prefs) {
		final String idS = ECEActivity.getTag(this);
		if (prefs.contains(idS)) {
			final double ld = Double.longBitsToDouble(prefs.getLong(idS, 0L));
			// Why floats? Why no doubles in preferences? Android you make me sad!
			if (!Double.isNaN(ld))
				updateValue(ld);
		}
	}
	public void saveState(SharedPreferences.Editor prefs) {
		prefs.putLong(ECEActivity.getTag(this), Double.doubleToLongBits(getRawValue()));
	}
	public void onClick(View v) {
		if (activity != null) {
			final String desc = getDescription();
			// Create popup
			final ValueEntryDialog mutate = ValueEntryDialog.create(value, desc);
			mutate.setOnCalculateListener(this);
			// Show it, popup will call oncalculate for us on OK
			mutate.show(activity.getFragmentManager(), desc);
		}
	}
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof ECESavedState) {
			final ECESavedState<EngineeringValue> ecess = (ECESavedState<EngineeringValue>)state;
			super.onRestoreInstanceState(ecess.getSuperState());
			// Attempt to restore our state
			final EngineeringValue v = ecess.getValue(), oldValue = value;
			if (v != null) {
				setValue(v);
				if (!v.equals(oldValue))
					callOnCalculateListener();
			}
		} else
			super.onRestoreInstanceState(state);
	}
	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable parentState = super.onSaveInstanceState();
		return new ECESavedState<EngineeringValue>(parentState, value);
	}
	public void onValueChange(EngineeringValue newValue) {
		final EngineeringValue oldValue = value;
		if (newValue != null) {
			setValue(newValue);
			// Call listener if needed
			if (!newValue.equals(oldValue))
				callOnCalculateListener();
		}
	}
	/**
	 * Changes the listener fired when the value is changed and recalculation is required.
	 *
	 * @param listener the listener to be fired
	 */
	public void setOnCalculateListener(final Calculatable listener) {
		this.listener = listener;
	}
	/**
	 * Changes the description of this value box. Usually set in the style, but it could be
	 * dynamic...
	 *
	 * @param description the description to be shown on the button
	 */
	public void setDescription(String description) {
		if (description != null) {
			this.description = description;
			updateText();
		}
	}
	/**
	 * Changes the parent activity of this box. Required because Dialogs need a fragment manager
	 * to be shown, and context is not reliably the correct parent Activity... why Android why?
	 *
	 * @param activity the parent activity
	 */
	public void setParentActivity(final Activity activity) {
		if (activity != null) {
			this.activity = activity;
			// Update the copy listener
			copyListener = new CopyListener(activity, getDescription());
			copyListener.setValue(getValue());
			setOnLongClickListener(copyListener);
		}
	}
	/**
	 * Changes the currently selected index. Does not fire the calculation listener but updates
	 * the button text.
	 *
	 * @param newValue the new value
	 */
	public void setValue(final EngineeringValue newValue) {
		if (newValue != value) {
			value = newValue;
			if (copyListener != null)
				copyListener.setValue(newValue);
			updateText();
		}
	}
	/**
	 * Update the button text.
	 */
	protected void updateText() {
		// Get text
		final String dest = getDescription(), val = getValue().toString();
		final SpannableStringBuilder text = new SpannableStringBuilder();
		text.append(dest);
		text.append('\n');
		text.append(val);
		// Make the name a bit smaller
		text.setSpan(new RelativeSizeSpan(0.8f), 0, dest.length(),
			Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		setText(text);
	}
	/**
	 * Changes the raw value of this value entry box, keeping all other engineering parameters
	 * the same.
	 *
	 * @param rawValue the new raw value to show in this entry box
	 */
	public void updateValue(final double rawValue) {
		setValue(getValue().newValue(rawValue));
	}
}
