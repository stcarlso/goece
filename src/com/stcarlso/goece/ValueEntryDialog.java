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

package com.stcarlso.goece;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Represents a dialog box which can accept ECE values in scientific notation.
 */
public class ValueEntryDialog extends DialogFragment implements
		DialogInterface.OnClickListener {
	/**
	 * Creates and shows a value entry dialog.
	 *
	 * @param value the current value
	 * @param desc the description of the value to be entered
	 * @return the dialog object for event attachment and eventual show()
	 */
	public static ValueEntryDialog create(final EngineeringValue value, final String desc) {
		final ValueEntryDialog dialog = new ValueEntryDialog();
		dialog.setDescription(desc);
		dialog.setValue(value);
		return dialog;
	}

	/**
	 * The title of this dialog describing the value to be entered.
	 */
	private String desc;
	/**
	 * The last successfully entered or set value.
	 */
	private EngineeringValue value;

	public ValueEntryDialog() {
		desc = "Enter new value";
		value = new EngineeringValue(0.0);
	}
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
		dismiss();
	}
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Activity act = getActivity();
		final AlertDialog.Builder builder = new AlertDialog.Builder(act);
		// Load layout
		final View dialog = act.getLayoutInflater().inflate(R.layout.valueentry, null);
		builder.setView(dialog);
		// Populate the unit choices
		final String[] prefix = EngineeringValue.ENGR_NAMES,
			unitList = new String[prefix.length];
		for (int i = 0; i < prefix.length; i++)
			unitList[i] = prefix[i] + value.getUnits();
		// Create array adapter
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(act,
			android.R.layout.simple_spinner_item, unitList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Assign to dropdown
		final Spinner dropdown = (Spinner)dialog.findViewById(R.id.guiValueExp);
		dropdown.setAdapter(adapter);
		dropdown.setSelection(value.getSIPrefixCode());
		// Load text
		final EditText realValue = (EditText)dialog.findViewById(R.id.guiValue);
		realValue.setText(Double.toString(value.getSignificand()));
		ECEActivity.initShowSoftKeyboard(realValue);
		builder.setTitle(desc);
		// Create OK and Cancel buttons
		builder.setPositiveButton(R.string.ok, this);
		builder.setNegativeButton(R.string.cancel, new IgnoreOnClickListener());
		return builder.create();
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
	 * Presets the value in this dialog box.
	 *
	 * @param value the current value
	 */
	protected void setValue(final EngineeringValue value) {
		if (value != null)
			this.value = value;
	}
}
