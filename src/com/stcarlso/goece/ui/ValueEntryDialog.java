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
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.*;

/**
 * Represents a dialog box which can accept ECE values in scientific notation.
 */
public class ValueEntryDialog extends AbstractEntryDialog implements View.OnKeyListener {
	/**
	 * The EIA series that are used for the display helpers. There must be one text view per
	 * entry!
	 */
	private static final EIATable.EIASeries[] SERIES = {
		EIATable.EIASeries.E96, EIATable.EIASeries.E24,
		EIATable.EIASeries.E12, EIATable.EIASeries.E6
	};
	/**
	 * Creates a value entry dialog.
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

	@Override
	protected double getEnteredValue() {
		// Reconstruct from units and value, fairly simple
		return EngineeringValue.valueFromSigExp(Double.parseDouble(valueEntry.getText().
			toString()), unitSelect.getSelectedItemPosition());
	}
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		updateValidValues(v.getRootView());
		return false;
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Activity act = getActivity();
		final AlertDialog.Builder builder = new AlertDialog.Builder(act);
		int flags = InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER;
		// Load layout
		final View dialog = View.inflate(act, R.layout.valueentry, null);
		builder.setView(dialog);
		if (negative)
			flags |= InputType.TYPE_NUMBER_FLAG_SIGNED;
		// Create array adapter
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(act,
			android.R.layout.simple_spinner_item, EngineeringValue.buildUnitChoices(
			value.getUnits()));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Assign to dropdown
		unitSelect = (Spinner)dialog.findViewById(R.id.guiValueExp);
		unitSelect.setAdapter(adapter);
		unitSelect.setSelection(value.getSIPrefixCode());
		// Load text, allow editing of a few more sigfigs than usual
		valueEntry = (EditText)dialog.findViewById(R.id.guiValue);
		valueEntry.setInputType(flags);
		valueEntry.setOnKeyListener(this);
		valueEntry.setText(value.significandToString(6));
		valueEntry.selectAll();
		updateValidValues(dialog);
		builder.setTitle(UIFunctions.fromHtml(desc));
		// Create OK and Cancel buttons
		builder.setPositiveButton(R.string.ok, this);
		builder.setNegativeButton(R.string.cancel, new IgnoreOnClickListener());
		restoreState(savedInstanceState);
		// Create dialog, show keyboard
		final Dialog window = builder.create();
		window.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return window;
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
					UIFunctions.checkEIATable(new EIAValue(entry, SERIES[i], units),
						pct[i]);
			} catch (NumberFormatException ignore) { }
	}

}
