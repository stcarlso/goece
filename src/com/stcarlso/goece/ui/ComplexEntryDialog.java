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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.ComplexValue;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.IgnoreOnClickListener;
import com.stcarlso.goece.utility.UIFunctions;

/**
 * Represents a dialog box which can accept ECE values in scientific notation for real and
 * imaginary parts. Values can also be entered using magnitude and phase.
 */
public class ComplexEntryDialog extends DialogFragment implements
		DialogInterface.OnClickListener, View.OnClickListener {
	/**
	 * Creates and shows a value entry dialog.
	 *
	 * @param value the current value
	 * @param desc the description of the value to be entered
	 * @return the dialog object for event attachment and eventual show()
	 */
	public static ComplexEntryDialog create(final ComplexValue value,
											final ComplexEntryDescriptions desc) {
		final ComplexEntryDialog dialog;
		if (value != null) {
			// Create dialog and set description/value
			dialog = new ComplexEntryDialog();
			dialog.setDescription(desc);
			dialog.setValue(value);
		} else
			dialog = null;
		return dialog;
	}
	/**
	 * Formats the number for display in the 4 text fields.
	 *
	 * @param value the value to display
	 * @return the value in significand form to 6 places
	 */
	private static String decFormat(final double value) {
		return EngineeringValue.significandToString(value, 6);
	}
	/**
	 * Calculates a raw value from the unit selected and significand.
	 *
	 * @param value the value significand
	 * @param unit the unit index selected
	 * @return the raw value
	 */
	private static double entryToString(final EditText value, final Spinner unit) {
		return EngineeringValue.valueFromSigExp(Double.parseDouble(value.getText().
			toString()), unit.getSelectedItemPosition());
	}

	/**
	 * The title of this dialog describing the value to be entered.
	 */
	private ComplexEntryDescriptions desc;
	/**
	 * The optional listener to be fired when the value is changed.
	 */
	private OnCalculateListener listener;
	/**
	 * Reference to the edit box containing the user's new imaginary part.
	 */
	private EditText imagEntry;
	/**
	 * Reference to the edit box containing the user's new magnitude.
	 */
	private EditText magEntry;
	/**
	 * Magnitude/phase mode selection checkbox (if clear, must be in re/im mode!)
	 */
	private RadioButton magPhaseMode;
	/**
	 * Panel displaying the magnitude and phase entry boxes.
	 */
	private View magPhaPanel;
	/**
	 * Reference to the edit box containing the user's new phase angle.
	 */
	private EditText phaEntry;
	/**
	 * Reference to the edit box containing the user's new real part.
	 */
	private EditText realEntry;
	/**
	 * Panel displaying the real and imaginary entry boxes.
	 */
	private View reImPanel;
	/**
	 * Reference to the drop-down list of unit selections. There are 3 of these, one for each
	 * of magnitude, real and imaginary part.
	 */
	private Spinner[] unitSelect;
	/**
	 * The last successfully entered or set value.
	 */
	private ComplexValue value;

	public ComplexEntryDialog() {
		desc = new ComplexEntryDescriptions("Enter new value");
		unitSelect = new Spinner[3];
		imagEntry = null;
		magEntry = null;
		phaEntry = null;
		realEntry = null;
		listener = null;
		value = new ComplexValue(0.0, 0.0);
	}
	/**
	 * Fires the recalculate method of the attached listener, if it exists.
	 */
	protected void callOnCalculateListener() {
		if (listener != null)
			listener.onValueChange(getValue());
	}
	/**
	 * Returns the imaginary part currently entered in the fields.
	 *
	 * @return the imaginary part entered by the user
	 * @throws NumberFormatException if the value in the numeric field is not valid
	 */
	private double getEnteredImag() {
		return entryToString(imagEntry, unitSelect[2]);
	}
	/**
	 * Returns the magnitude currently entered in the fields.
	 *
	 * @return the magnitude entered by the user
	 * @throws NumberFormatException if the value in the numeric field is not valid
	 */
	private double getEnteredMagnitude() {
		return entryToString(magEntry, unitSelect[0]);
	}
	/**
	 * Returns the phase currently entered in the fields.
	 *
	 * @return the phase entered by the user
	 * @throws NumberFormatException if the value in the numeric field is not valid
	 */
	private double getEnteredPhase() {
		return Double.parseDouble(phaEntry.getText().toString());
	}
	/**
	 * Returns the real part currently entered in the fields.
	 *
	 * @return the real part entered by the user
	 * @throws NumberFormatException if the value in the numeric field is not valid
	 */
	private double getEnteredReal() {
		return entryToString(realEntry, unitSelect[1]);
	}
	/**
	 * Get the value entered by the user; if no value was entered, or if Cancel was selected,
	 * the last preset value is returned.
	 *
	 * @return the value entered in this dialog
	 */
	public ComplexValue getValue() {
		return value;
	}
	/**
	 * Loads the data from the raw engineering value into the dialog box.
	 */
	private void loadData() {
		// Create fake values for the real and imaginary parts
		final EngineeringValue rePart = value.newValue(value.getReal());
		final EngineeringValue imPart = value.newValue(value.getImaginary());
		unitSelect[0].setSelection(value.getSIPrefixCode());
		unitSelect[1].setSelection(rePart.getSIPrefixCode());
		unitSelect[2].setSelection(imPart.getSIPrefixCode());
		// Load magnitude/phase, allow editing of a few more sigfigs than usual
		magEntry.setText(decFormat(value.getSignificand()));
		phaEntry.setText(String.format("%.3f", value.getAngle()));
		// Load real/imaginary
		realEntry.setText(decFormat(rePart.getSignificand()));
		imagEntry.setText(decFormat(imPart.getSignificand()));
	}
	public void onClick(DialogInterface dialog, int which) {
		// Load value and unit
		if (magEntry != null && unitSelect != null)
			try {
				// Load the new value
				if (magPhaseMode.isChecked())
					value = value.newValue(getEnteredMagnitude(), getEnteredPhase());
				else
					value = value.newRectangularValue(getEnteredReal(), getEnteredImag());
				// If we fail, do not close the dialog
				callOnCalculateListener();
				dismiss();
			} catch (NumberFormatException ignore) { }
	}
	public void onClick(View v) {
		boolean magPhase = magPhaseMode.isChecked();
		magPhaPanel.setVisibility(magPhase ? View.VISIBLE : View.GONE);
		reImPanel.setVisibility(magPhase ? View.GONE : View.VISIBLE);
		try {
			final ComplexValue tempValue;
			// Exception here is non fatal, it will just result in the fields being mis matched
			if (magPhase)
				// Copy Re/Im over
				tempValue = value.newRectangularValue(getEnteredReal(), getEnteredImag());
			else
				// Copy Mag/Pha over
				tempValue = value.newValue(getEnteredMagnitude(), getEnteredPhase());
			setValue(tempValue);
			loadData();
		} catch (NumberFormatException ignore) { }
	}
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Activity act = getActivity();
		final AlertDialog.Builder builder = new AlertDialog.Builder(act);
		// Load layout
		final View dialog = act.getLayoutInflater().inflate(R.layout.cplxentry, null);
		builder.setView(dialog);
		// Create array adapter shared between all 3 instances
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
			android.R.layout.simple_spinner_item, EngineeringValue.buildUnitChoices(
			value.getUnits()));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Assign to dropdowns
		unitSelect[0] = (Spinner)dialog.findViewById(R.id.guiValueMagExp);
		unitSelect[1] = (Spinner)dialog.findViewById(R.id.guiValueReExp);
		unitSelect[2] = (Spinner)dialog.findViewById(R.id.guiValueImExp);
		for (Spinner us : unitSelect)
			us.setAdapter(adapter);
		// Get references to important fields
		magEntry = (EditText)dialog.findViewById(R.id.guiValueMag);
		phaEntry = (EditText)dialog.findViewById(R.id.guiValuePha);
		realEntry = (EditText)dialog.findViewById(R.id.guiValueRe);
		imagEntry = (EditText)dialog.findViewById(R.id.guiValueIm);
		magPhaseMode = (RadioButton)dialog.findViewById(R.id.guiValueModeMagPha);
		magPhaseMode.setOnClickListener(this);
		magPhaPanel = dialog.findViewById(R.id.guiValueMagPha);
		reImPanel = dialog.findViewById(R.id.guiValueReIm);
		dialog.findViewById(R.id.guiValueModeReIm).setOnClickListener(this);
		loadData();
		// Show the keyboard
		magEntry.selectAll();
		UIFunctions.initShowSoftKeyboard(magEntry);
		// Split up and load all descriptions (must have at least one element in return!)
		builder.setTitle(Html.fromHtml(desc.getTitle()));
		UIFunctions.setLabelText(dialog, R.id.guiValueMagDesc, desc.getMagnitudeDescription());
		UIFunctions.setLabelText(dialog, R.id.guiValuePhaDesc, desc.getPhaseDescription());
		UIFunctions.setLabelText(dialog, R.id.guiValueReDesc, desc.getRealDescription());
		UIFunctions.setLabelText(dialog, R.id.guiValueImDesc, desc.getImaginaryDescription());
		// Create OK and Cancel buttons
		builder.setPositiveButton(R.string.ok, this);
		builder.setNegativeButton(R.string.cancel, new IgnoreOnClickListener());
		// Load the values
		return builder.create();
	}
	/**
	 * Changes the description of this dialog box.
	 *
	 * @param desc A short description of the value to be entered
	 */
	protected void setDescription(final ComplexEntryDescriptions desc) {
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
	protected void setValue(final ComplexValue value) {
		if (value != null)
			this.value = value;
	}

	/**
	 * A shell class which carries around the 4 extended descriptions for the individual
	 * entry fields of a ComplexEntryDialog.
	 */
	public static class ComplexEntryDescriptions {
		/**
		 * Description of the imaginary part field, e.g. Reactance.
		 */
		protected final String imagDesc;
		/**
		 * Description of the magnitude field, e.g. Impedance.
		 */
		protected final String magDesc;
		/**
		 * Description of the phase field, e.g. Phase.
		 */
		protected final String phaDesc;
		/**
		 * Description of the real part field, e.g. Resistance.
		 */
		protected final String realDesc;
		/**
		 * The dialog title.
		 */
		protected final String title;

		/**
		 * Creates a wrapper for the complex field descriptions, but with all values except the
		 * title set to the empty string.
		 *
		 * @param title the dialog title
		 */
		public ComplexEntryDescriptions(final String title) {
			this(null, null, null, null, title);
		}
		/**
		 * Creates a wrapper for the complex field descriptions.
		 *
		 * @param magDesc the magnitude description
		 * @param phaDesc the phase description
		 * @param realDesc the real part description
		 * @param imagDesc the imaginary part description
		 * @param title the dialog title
		 */
		public ComplexEntryDescriptions(final String magDesc, final String phaDesc,
										final String realDesc, final String imagDesc,
										final String title) {
			this.imagDesc = (imagDesc == null) ? "" : imagDesc;
			this.magDesc = (magDesc == null) ? "" : magDesc;
			this.phaDesc = (phaDesc == null) ? "" : phaDesc;
			this.realDesc = (realDesc == null) ? "" : realDesc;
			this.title = (title == null) ? "" : title;
		}
		/**
		 * Retrieves the description of the imaginary part field.
		 *
		 * @return the imaginary part description
		 */
		public String getImaginaryDescription() {
			return imagDesc;
		}
		/**
		 * Retrieves the description of the magnitude field.
		 *
		 * @return the magnitude description
		 */
		public String getMagnitudeDescription() {
			return magDesc;
		}
		/**
		 * Retrieves the description of the phase field.
		 *
		 * @return the phase description
		 */
		public String getPhaseDescription() {
			return phaDesc;
		}
		/**
		 * Retrieves the description of the real part field.
		 *
		 * @return the real part description
		 */
		public String getRealDescription() {
			return realDesc;
		}
		/**
		 * Retrieves the dialog title.
		 *
		 * @return the dialog title
		 */
		public String getTitle() {
			return title;
		}
	}
	/**
	 * Since ComplexEntryDialog is usually created and left for dead, the Calculatable listener
	 * will do us no good as the receiver will have no records of our existence (sob). So we
	 * use a different listener that includes the ComplexValue entered.
	 */
	public interface OnCalculateListener {
		/**
		 * Invoked when the value is changed.
		 *
		 * @param newValue the new value
		 */
		void onValueChange(ComplexValue newValue);
	}
}
