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
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.CustomUnit;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.IgnoreOnClickListener;

import java.util.*;

/**
 * A dialog box similar to ValueEntryDialog, but which supplies a list of custom units.
 * The returned value is always kept in a consistent unit.
 */
public class CustomEntryDialog extends AbstractEntryDialog implements
		AdapterView.OnItemSelectedListener {
	/**
	 * Creates a custom entry dialog.
	 *
	 * @param value the current value
	 * @param desc the description of the value to be entered
	 * @return the dialog object for event attachment and eventual show()
	 */
	public static CustomEntryDialog create(final EngineeringValue value, final String desc) {
		final CustomEntryDialog dialog;
		if (value != null) {
			// Create dialog and set description/value
			dialog = new CustomEntryDialog();
			dialog.setDescription(desc);
			dialog.setValue(value);
		} else
			dialog = null;
		return dialog;
	}

	/**
	 * List of custom unit options.
	 * If none are provided, only the base unit will be available.
	 */
	protected final List<CustomUnit> customUnits;
	/**
	 * The initially selected custom unit. If null or invalid, the base unit is selected.
	 */
	protected CustomUnit initialUnit;

	public CustomEntryDialog() {
		super();
		customUnits = new ArrayList<CustomUnit>(8);
		initialUnit = null;
	}
	/**
	 * Adds a custom unit option to the choices in this dialog.
	 *
	 * @param unit the custom unit to add
	 */
	public void addCustomUnit(final CustomUnit unit) {
		customUnits.add(unit);
	}
	protected double getEnteredValue() {
		double raw = Double.parseDouble(valueEntry.getText().toString());
		final CustomUnit unit = getSelectedUnit();
		if (unit != null)
			raw = unit.toBase(raw);
		return raw;
	}
	/**
	 * Returns the currently selected unit for entry.
	 *
	 * @return the currently selected custom unit in the combo box, or null if the default unit
	 * is selected
	 */
	public CustomUnit getSelectedUnit() {
		final int index = unitSelect.getSelectedItemPosition();
		final CustomUnit unit;
		// Slot 0 contains the default unit
		if (index > 0 && index <= customUnits.size())
			unit = customUnits.get(index - 1);
		else
			unit = null;
		return unit;
	}
	/**
	 * Builds a list of unit choices from the current value and custom user units.
	 *
	 * @return a list of the currently valid unit choices
	 */
	private CharSequence[] getUnitChoices() {
		final CharSequence[] ret = new CharSequence[customUnits.size() + 1];
		ret[0] = Html.fromHtml(getValue().getUnits());
		// Iterate and add all custom choices
		int i = 1;
		for (CustomUnit unit : customUnits)
			ret[i++] = Html.fromHtml(unit.getUnit());
		return ret;
	}
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Activity act = getActivity();
		final AlertDialog.Builder builder = new AlertDialog.Builder(act);
		// Load layout
		final View dialog = View.inflate(act, R.layout.customentry, null);
		builder.setView(dialog);
		// Create array adapter
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(act,
			android.R.layout.simple_spinner_item, getUnitChoices());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Assign to dropdown
		unitSelect = (Spinner)dialog.findViewById(R.id.guiCustomUnit);
		unitSelect.setAdapter(adapter);
		unitSelect.setOnItemSelectedListener(this);
		// If not found, -1 + 1 = 0
		if (initialUnit != null)
			unitSelect.setSelection(customUnits.indexOf(initialUnit) + 1);
		else
			unitSelect.setSelection(0);
		// Load text, allow editing of a few more sigfigs than usual
		valueEntry = (EditText)dialog.findViewById(R.id.guiCustom);
		valueEntry.setText(act.getString(R.string.editRaw, value.getValue()));
		valueEntry.selectAll();
		builder.setTitle(Html.fromHtml(desc));
		// Create OK and Cancel buttons
		builder.setPositiveButton(R.string.ok, this);
		builder.setNegativeButton(R.string.cancel, new IgnoreOnClickListener());
		restoreState(savedInstanceState);
		// Create dialog, show keyboard
		final Dialog window = builder.create();
		window.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return window;
	}
	public void onClick(DialogInterface dialog, int which) {
		// Load value and unit
		if (valueEntry != null && unitSelect != null)
			try {
				// Set return units according to the units selected
				String unitOut = value.getUnits();
				final CustomUnit unit = getSelectedUnit();
				if (unit != null)
					unitOut = unit.getUnit();
				// Load the new value
				value = new EngineeringValue(getEnteredValue(), value.getTolerance(), unitOut);
				// If we fail, do not close the dialog
				callOnCalculateListener();
				dismiss();
			} catch (NumberFormatException ignore) { }
	}
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Only trigger if the field value is selected
		if (valueEntry.getSelectionStart() == 0 && valueEntry.getSelectionEnd() ==
				valueEntry.getText().length()) {
			// Convert value to the new unit choice
			double raw = getValue().getValue();
			final CustomUnit unit = getSelectedUnit();
			if (unit != null)
				raw = unit.fromBase(raw);
			// Update contents
			valueEntry.setText(getActivity().getString(R.string.editRaw, raw));
			valueEntry.selectAll();
		}
	}
	public void onNothingSelected(AdapterView<?> parent) {
		// This is not really possible with the units spinner
	}
	/**
	 * Selects a unit for entry. If null or an invalid unit is supplied, the base unit will be
	 * selected.
	 *
	 * @param unit the unit to select
	 */
	public void setSelectedUnit(final CustomUnit unit) {
		initialUnit = unit;
	}
}