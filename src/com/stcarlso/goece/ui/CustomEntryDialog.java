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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.IgnoreOnClickListener;

import java.util.*;

/**
 * A dialog box similar to ValueEntryDialog, but which supplies a list of custom units.
 * The returned value is always kept in a consistent unit.
 */
public class CustomEntryDialog extends AbstractEntryDialog {
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

	public CustomEntryDialog() {
		super();
		customUnits = new ArrayList<CustomUnit>(8);
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
		// If unit index is 0, return directly, otherwise convert value
		final int index = unitSelect.getSelectedItemPosition();
		if (index > 0 && index <= customUnits.size())
			raw = customUnits.get(index - 1).toBase(raw);
		return raw;
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
		unitSelect.setSelection(0);
		// Load text, allow editing of a few more sigfigs than usual
		valueEntry = (EditText)dialog.findViewById(R.id.guiCustom);
		valueEntry.setText(act.getString(R.string.editRaw, value.getValue()));
		valueEntry.selectAll();
		builder.setTitle(Html.fromHtml(desc));
		// Create OK and Cancel buttons
		builder.setPositiveButton(R.string.ok, this);
		builder.setNegativeButton(R.string.cancel, new IgnoreOnClickListener());
		// Create dialog, show keyboard
		final Dialog window = builder.create();
		window.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return window;
	}

	/**
	 * Denotes a custom unit which can be used for this dialog. Stores the unit name and the
	 * conversion factor (the custom value is <i>multiplied</i> by the factor).
	 */
	public static class CustomUnit {
		/**
		 * The multiplicative conversion factor for this unit.
		 */
		public final double factor;
		/**
		 * The unit name shown in the dropdown and entry box.
		 */
		public final String unit;

		/**
		 * Creates a new custom unit.
		 *
		 * @param unit the unit name
		 * @param factor the unit conversion factor
		 */
		public CustomUnit(final String unit, final double factor) {
			if (unit == null)
				throw new NullPointerException("unit");
			if (factor <= 0.0 || Double.isInfinite(factor) || Double.isNaN(factor))
				throw new IllegalArgumentException("conversion factor");
			this.factor = factor;
			this.unit = unit;
		}
		public boolean equals(Object o) {
			return this == o || !(o == null || getClass() != o.getClass()) &&
				unit.equals(((CustomUnit)o).unit);
		}
		/**
		 * Converts from the base unit to this unit.
		 *
		 * @param baseValue the value to convert in the base unit
		 * @return the value in this unit
		 */
		public double fromBase(final double baseValue) {
			return baseValue / factor;
		}
		/**
		 * Converts from the base unit to this unit.
		 *
		 * @param baseValue the value to convert in the base unit
		 * @return the value in this unit
		 */
		public EngineeringValue fromBase(final EngineeringValue baseValue) {
			// Tolerance is a % so it is not multiplied!
			return new EngineeringValue(baseValue.getValue() / factor, baseValue.getTolerance(),
				baseValue.getSigfigs(), unit);
		}
		/**
		 * Gets the conversion factor for this unit.
		 *
		 * @return the unit multiplicative conversion factor
		 */
		public double getFactor() {
			return factor;
		}
		/**
		 * Gets the unit name for this unit.
		 *
		 * @return the unit name
		 */
		public String getUnit() {
			return unit;
		}
		public int hashCode() {
			return unit.hashCode();
		}
		/**
		 * Converts from this unit to the base unit.
		 *
		 * @param unitValue the value to convert in this unit
		 * @return the value in the base unit
		 */
		public double toBase(final double unitValue) {
			return unitValue * factor;
		}
		/**
		 * Converts from this unit to the base unit.
		 *
		 * @param unitValue the value to convert in this unit
		 * @return the value in the base unit
		 */
		public EngineeringValue toBase(final EngineeringValue unitValue) {
			// Tolerance is a % so it is not multiplied!
			return new EngineeringValue(unitValue.getValue() * factor, unitValue.getTolerance(),
				unitValue.getSigfigs(), unit);
		}
		public String toString() {
			return unit;
		}
	}
}