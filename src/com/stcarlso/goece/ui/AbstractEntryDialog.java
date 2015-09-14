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

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Spinner;
import com.stcarlso.goece.utility.EngineeringValue;

/**
 * Abstract parent of ValueEntryDialog and CustomEntryDialog containing shared logic.
 */
public abstract class AbstractEntryDialog extends DialogFragment implements
		DialogInterface.OnClickListener {
	/**
	 * The title of this dialog describing the value to be entered.
	 */
	protected String desc;
	/**
	 * The optional listener to be fired when the value is changed.
	 */
	protected OnCalculateListener listener;
	/**
	 * Reference to the drop-down list of unit selections.
	 */
	protected Spinner unitSelect;
	/**
	 * Reference to the edit box containing the user's new value.
	 */
	protected EditText valueEntry;
	/**
	 * The last successfully entered or set value.
	 */
	protected EngineeringValue value;

	/**
	 * Initializes an empty dialog. Use the subclass factory methods instead.
	 */
	protected AbstractEntryDialog() {
		valueEntry = null;
		listener = null;
		value = new EngineeringValue(0.0);
		desc = "Enter new value";
		unitSelect = null;
	}
	/**
	 * Fires the recalculate method of the attached listener, if it exists.
	 */
	protected void callOnCalculateListener() {
		if (listener != null)
			listener.onValueChange(getValue());
	}
	/**
	 * Returns the value currently entered in the fields.
	 *
	 * @return the value entered by the user
	 * @throws NumberFormatException if the value in the numeric field is not valid
	 */
	protected abstract double getEnteredValue();
	/**
	 * Get the value entered by the user; if no value was entered, or if Cancel was selected,
	 * the last preset value is returned.
	 *
	 * @return the value entered in this dialog
	 */
	public EngineeringValue getValue() {
		return value;
	}
	public void onClick(DialogInterface dialog, int which) {
		// Load value and unit
		if (valueEntry != null && unitSelect != null)
			try {
				// Load the new value
				value = value.newValue(getEnteredValue());
				// If we fail, do not close the dialog
				callOnCalculateListener();
				dismiss();
			} catch (NumberFormatException ignore) { }
	}
	/**
	 * Changes the description of this dialog box.
	 *
	 * @param desc A short description of the value to be entered
	 */
	protected void setDescription(final String desc) {
		this.desc = desc;
	}
	/**
	 * Changes the listener fired when the value is changed and recalculation is required.
	 *
	 * @param listener the listener to be fired
	 */
	public void setOnCalculateListener(final OnCalculateListener listener) {
		this.listener = listener;
	}
	/**
	 * Presets the value in this dialog box.
	 *
	 * @param value the current value
	 */
	protected void setValue(final EngineeringValue value) {
		if (value != null)
			this.value = value;
	}

	/**
	 * Since AbstractEntryDialog is usually created and left for dead, the Calculatable listener
	 * will do us no good as the receiver will have no records of our existence (sob). So we
	 * use a different listener that includes the EngineeringValue entered.
	 */
	public interface OnCalculateListener {
		/**
		 * Invoked when the value is changed.
		 *
		 * @param newValue the new value
		 */
		void onValueChange(EngineeringValue newValue);
	}
}
