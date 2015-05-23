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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.activity.ECEActivity;
import com.stcarlso.goece.activity.ResColorActivity;
import com.stcarlso.goece.utility.EIATable;
import com.stcarlso.goece.utility.EIAValue;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Units;

/**
 * Represents a dialog box which can accept ECE values in scientific notation.
 */
public class ValueEntryDialog extends DialogFragment implements
		DialogInterface.OnClickListener, View.OnKeyListener {
	/**
	 * The EIA series that are used for the display helpers. There must be one text view per
	 * entry!
	 */
	private static final EIATable.EIASeries[] SERIES = {
		EIATable.EIASeries.E96, EIATable.EIASeries.E24,
		EIATable.EIASeries.E12, EIATable.EIASeries.E6
	};
	/**
	 * Creates and shows a value entry dialog.
	 *
	 * @param value the current value
	 * @param desc the description of the value to be entered
	 * @return the dialog object for event attachment and eventual show()
	 */
	public static ValueEntryDialog create(final EngineeringValue value, final String desc) {
		final ValueEntryDialog dialog;
		if (value != null) {
			// Create dialog and set description/value
			dialog = new ValueEntryDialog();
			dialog.setDescription(desc);
			dialog.setValue(value);
		} else
			dialog = null;
		return dialog;
	}

	/**
	 * The title of this dialog describing the value to be entered.
	 */
	private String desc;
	/**
	 * Reference to the drop-down list of unit selections.
	 */
	private Spinner unitSelect;
	/**
	 * The optional listener to be fired when the value is changed.
	 */
	private OnCalculateListener listener;
	/**
	 * Reference to the edit box containing the user's new value.
	 */
	private EditText valueEntry;
	/**
	 * The last successfully entered or set value.
	 */
	private EngineeringValue value;

	public ValueEntryDialog() {
		desc = "Enter new value";
		unitSelect = null;
		valueEntry = null;
		listener = null;
		value = new EngineeringValue(0.0);
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
	private double getEnteredValue() {
		return EngineeringValue.valueFromSigExp(Double.parseDouble(valueEntry.getText().
			toString()), unitSelect.getSelectedItemPosition());
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
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		updateValidValues(v.getRootView());
		return false;
	}
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Activity act = getActivity();
		final AlertDialog.Builder builder = new AlertDialog.Builder(act);
		// Load layout
		final View dialog = act.getLayoutInflater().inflate(R.layout.valueentry, null);
		builder.setView(dialog);
		// Populate the unit choices, skip the last (invalid) choice
		final String[] prefix = EngineeringValue.ENGR_NAMES,
			unitList = new String[prefix.length - 1];
		for (int i = 0; i < unitList.length; i++)
			unitList[i] = prefix[i] + value.getUnits();
		// Create array adapter
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(act,
			android.R.layout.simple_spinner_item, unitList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Assign to dropdown
		unitSelect = (Spinner)dialog.findViewById(R.id.guiValueExp);
		unitSelect.setAdapter(adapter);
		unitSelect.setSelection(value.getSIPrefixCode());
		// Load text, allow editing of a few more sigfigs than usual
		valueEntry = (EditText)dialog.findViewById(R.id.guiValue);
		valueEntry.setOnKeyListener(this);
		valueEntry.setText(value.significandToString(6));
		valueEntry.selectAll();
		updateValidValues(dialog);
		ECEActivity.initShowSoftKeyboard(valueEntry);
		builder.setTitle(Html.fromHtml(desc));
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
	 * Updates some labels beneath the text box for feedback on valid 1%, 5%, 10%, and 20%
	 * values. Only shows for Units.RESISTANCE, Units.CAPACITANCE, and Units.INDUCTANCE.
	 *
	 * @param view the parent layout view
	 */
	protected void updateValidValues(final View view) {
		final String units = getValue().getUnits();
		final TextView[] pct = new TextView[SERIES.length];
		// Check if units are approved for display
		final boolean use = Units.RESISTANCE.equals(units) || Units.CAPACITANCE.equals(units) ||
			Units.INDUCTANCE.equals(units);
		pct[0] = (TextView)view.findViewById(R.id.guiValid1Pct);
		pct[1] = (TextView)view.findViewById(R.id.guiValid5Pct);
		pct[2] = (TextView)view.findViewById(R.id.guiValid10Pct);
		pct[3] = (TextView)view.findViewById(R.id.guiValid20Pct);
		// Update visibility
		for (TextView tv : pct)
			tv.setVisibility(use ? View.VISIBLE : View.GONE);
		if (use)
			try {
				// Parse the value, do nothing if not yet valid
				final double entry = getEnteredValue();
				// For each one, grab the index and check it
				for (int i = 0; i < pct.length; i++)
					// Display nearest value
					ResColorActivity.checkEIATable(new EIAValue(entry, SERIES[i], units),
						pct[i]);
			} catch (NumberFormatException ignore) { }
	}

	/**
	 * Since ValueEntryDialog is usually created and left for dead, the Calculatable listener
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
