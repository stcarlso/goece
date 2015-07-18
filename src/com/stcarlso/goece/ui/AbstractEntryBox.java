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
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.stcarlso.goece.R;
import com.stcarlso.goece.activity.ECEActivity;
import com.stcarlso.goece.utility.*;

/**
 * Skeleton class for a button with units that when clicked brings up a dialog.
 */
public abstract class AbstractEntryBox<T extends EngineeringValue> extends Button implements
		View.OnClickListener, ValueControl {
	/**
	 * Why android why?
	 */
	protected Activity activity;
	/**
	 * When this box is changed, this field is used to determine which group is affected.
	 */
	protected String affects;
	/**
	 * Listens for long presses and copies the value.
	 */
	protected CopyListener copyListener;
	/**
	 * The description requested for this value box ("Leakage Current", "Turn-On Voltage", ...)
	 */
	protected String description;
	/**
	 * The group assigned to this value entry box. All members in a group are LRUed to
	 * determine which one is changed when the group is affected.
	 */
	protected String group;
	/**
	 * The listener to be called each time the user changes this value.
	 */
	protected Calculatable listener;
	/**
	 * The value stored in this box.
	 */
	protected T value;

	public AbstractEntryBox(Context context) {
		super(context);
		init(context, null);
	}
	public AbstractEntryBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public AbstractEntryBox(Context context, AttributeSet attrs, int defStyle) {
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
	 * Returns the raw magnitude entered in this value box.
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
	public T getValue() {
		return value;
	}
	/**
	 * Intended to be overridden by the subclasses to parse their unique attributes. Does some
	 * common work under the hood though.
	 *
	 * @param context the owning application context
	 * @param attrs the attributes of this element
	 */
	protected void init(final Context context, final AttributeSet attrs) {
		activity = null;
		// Initialize click listeners
		listener = null;
		setOnClickListener(this);
		setMaxLines(2);
		// Get rid of the all-caps on lollipop material design devices which interferes with the
		// color, resize, and superscript/subscript on these buttons
		setTransformationMethod(null);
	}
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof ECESavedState) {
			final ECESavedState<T> ecess = (ECESavedState<T>)state;
			super.onRestoreInstanceState(ecess.getSuperState());
			// Attempt to restore our state
			onValueChange(ecess.getValue());
		} else
			super.onRestoreInstanceState(state);
	}
	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable parentState = super.onSaveInstanceState();
		return new ECESavedState<T>(parentState, value);
	}
	public void onValueChange(T newValue) {
		final T oldValue = value;
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
	 * Changes the currently selected value. Does not fire the calculation listener but updates
	 * the button text.
	 *
	 * @param newValue the new value
	 */
	public void setValue(final T newValue) {
		if (newValue != null && (value == null || !value.equals(newValue))) {
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
		final CharSequence desc = Html.fromHtml(getDescription()), val = getValue().toString();
		final SpannableStringBuilder text = new SpannableStringBuilder();
		text.append(desc);
		text.append('\n');
		text.append(val);
		// Make the name a bit smaller
		setText(text);
	}
}
