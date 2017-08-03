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

import android.app.Activity;
import android.util.SparseArray;
import android.view.View;
import com.stcarlso.goece.utility.EngineeringValue;

/**
 * A really simple class intended to reduce the massive number of repeated casts and
 * findValueById calls by marshalling ValueEntryBox items into a shared class.
 */
public class ValueBoxContainer extends SparseArray<AbstractEntryBox<? extends EngineeringValue>> {
	public ValueBoxContainer() {
		super(32);
	}
	/**
	 * Adds a control to this container.
	 *
	 * @param view the value entry box to add
	 */
	public void add(final View view) {
		if (view instanceof AbstractEntryBox) {
			// Only add valid items
			final AbstractEntryBox<?> box = (AbstractEntryBox<?>)view;
			put(box.getId(), box);
		}
	}
	/**
	 * Adds a series of controls to this container.
	 *
	 * @param parent the parent view
	 * @param id the integer IDs of controls to add
	 */
	public void add(final View parent, final int... id) {
		for (int viewID : id) {
			final View view = parent.findViewById(viewID);
			if (view != null)
				add(view);
		}
	}
	/**
	 * Gets the raw value of a value entry box.
	 *
	 * @param id the ID of the control to look up
	 * @return the raw value (magnitude for complex) entered into that control
	 */
	public double getRawValue(final int id) {
		final EngineeringValue value = getValue(id);
		final double ret;
		if (value == null)
			ret = Double.NaN;
		else
			ret = value.getValue();
		return ret;
	}
	/**
	 * Gets the value of a value entry box.
	 *
	 * @param id the ID of the control to look up
	 * @return the value entered into that control
	 */
	public EngineeringValue getValue(final int id) {
		final AbstractEntryBox<?> box = get(id);
		final EngineeringValue value;
		if (box != null)
			value = box.getValue();
		else
			value = null;
		return value;
	}
	/**
	 * Changes the value of a value entry box.
	 *
	 * @param id the ID of the control to change
	 * @param newValue the new value to put into that control; if NaN, the red error icon is
	 * shown instead
	 */
	public void setRawValue(final int id, final double newValue) {
		final AbstractEntryBox<?> box = get(id);
		if (box != null) {
			// Set up new value
			if (Double.isNaN(newValue))
				box.setError("");
			else {
				// All good
				box.setError(null);
				// Ugly but for backwards compatibility
				box.updateValue(newValue);
			}
		}
	}
	/**
	 * Changes the value of a value entry box. This override is meant for ComplexEntryBox but
	 * can be used on either instance. Note that get().setValue() will not work due to
	 * EngineeringValue not matching "capture of EngineeringValue"; get().updateValue()
	 * would but would require unpacking of the components
	 *
	 * @param id the ID of the control to change
	 * @param value the new value to put into that control, only the magnitude and phase are
	 * taken (units, tolerance, and significant figures are retained)
	 */
	public void setValue(final int id, final EngineeringValue value) {
		final AbstractEntryBox<?> box = get(id);
		if (box != null) {
			// Set up new value, EngineeringValue cannot have a NaN magnitude
			box.setError(null);
			box.updateValue(value.getValue(), value.getAngle());
		}
	}
	/**
	 * Configures all of the components added to this container so far.
	 *
	 * @param fragment the parent fragment which should own these components
	 */
	public void setupAll(final ChildFragment fragment) {
		for (int i = 0; i < size(); i++)
			fragment.setupValueEntryBox(valueAt(i));
	}
}
