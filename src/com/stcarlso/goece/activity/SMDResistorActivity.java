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

package com.stcarlso.goece.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.*;
import com.stcarlso.goece.utility.EIATable;
import com.stcarlso.goece.utility.EIAValue;
import com.stcarlso.goece.utility.UIFunctions;

/**
 * An activity for calculating SMD resistor (and capacitor, but those are never marked!) codes.
 */
public class SMDResistorActivity extends ChildActivity implements View.OnClickListener {
	/**
	 * Parse a code in the form "3R00" or "10R5" into a value, where R is the decimal point
	 * location. The code must contain exactly one capital letter 'R'
	 *
	 * @param code the value to parse
	 * @return the parsed value
	 * @throws NumberFormatException if the value could not be parsed in this way
	 */
	public static double parseRValue(final String code) {
		return Double.parseDouble(code.replace('R', '.'));
	}
	/**
	 * Parses a 3-letter SMD resistor code.
	 *
	 * @param code the SMD resistor code
	 * @return the resistor value, or null if the code cannot be parsed
	 */
	public static EIAValue parse3LetterCode(final String code) {
		EIAValue value = null;
		if (code != null && code.length() == 3) {
			final char multChar = code.charAt(2);
			try {
				if (code.indexOf('R') >= 0)
					// 4R7
					value = new EIAValue(parseRValue(code), EIAValue.E24);
				else {
					// The vast majority of resistors fit this pattern
					final int prefix = Integer.parseInt(code.substring(0, 2));
					if (Character.isDigit(multChar) && multChar < '8')
						// 10 ^ x
						value = new EIAValue(prefix * Math.pow(10.0, multChar - '0'),
							EIAValue.E24);
					else {
						// E96 new generation
						final double vv = EIATable.e96SMDCode(prefix) *
							EIATable.letterToMultiplier(multChar);
						if (vv != 0.0)
							// This is a 1% resistor!
							value = new EIAValue(vv, EIAValue.E96);
					}
				}
			} catch (NumberFormatException ignore) { }
		}
		return value;
	}
	/**
	 * Parses a 4-letter SMD resistor code.
	 *
	 * @param code the SMD resistor code
	 * @return the resistor value, or null if the code cannot be parsed
	 */
	public static EIAValue parse4LetterCode(final String code) {
		EIAValue value = null;
		if (code != null && code.length() == 4) {
			final char multChar = code.charAt(3);
			try {
				if (code.indexOf('R') >= 0)
					// 33R0
					value = new EIAValue(parseRValue(code), EIAValue.E96);
				else if (Character.isDigit(multChar) && multChar < '8')
					// The vast majority of resistors fit this pattern
					value = new EIAValue(Integer.parseInt(code.substring(0, 3)) *
						Math.pow(10.0, multChar - '0'), EIAValue.E96);
			} catch (NumberFormatException ignore) { }
		}
		return value;
	}

	/**
	 * Handles long presses on the output text box.
	 */
	private final CopyPasteListener copyPasteListener;
	/**
	 * The last successfully calculated resistor code.
	 */
	private String lastCode;
	/**
	 * Cached reference to the output text box.
	 */
	private TextView outputCtrl;
	/**
	 * Cached reference to the standard value box.
	 */
	private TextView stdCtrl;
	/**
	 * Cached reference to the underline selection control.
	 */
	private CheckBox underlineCtrl;

	public SMDResistorActivity() {
		copyPasteListener = new CopyPasteListener(this, "Resistance");
		lastCode = "000";
	}
	/**
	 * Calculates and displays the resistor value.
	 *
	 * @param code the SMD resistor code to calculate
	 * @param showErrors whether error messages should be shown
	 */
	private void calculate(final String code, final boolean showErrors) {
		final EIAValue value;
		// 3 or 4 characters?
		switch (code.length()) {
		case 3:
			// 3 char
			value = parse3LetterCode(code);
			break;
		case 4:
			// 4 char
			value = parse4LetterCode(code);
			break;
		default:
			value = null;
			break;
		}
		if (value == null) {
			// Oh no!
			if (showErrors)
				UIFunctions.errorMessage(this, R.string.guiResInvalid);
		} else {
			// Display it!
			showValue(value);
			lastCode = code;
		}
	}
	@Override
	protected void loadCustomPrefs(SharedPreferences prefs) {
		loadPrefsCheckBox(prefs, R.id.guiResLine);
		if (prefs.contains("lastCode"))
			lastCode = prefs.getString("lastCode", lastCode);
	}
	/**
	 * Receive click events from the calculate button and recalculate.
	 *
	 * @param v the source view
	 */
	@Override
	public void onClick(View v) {
		recalculate(findValueById(R.id.guiResSMDCode));
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smdresistor);
		underlineCtrl = asCheckBox(R.id.guiResLine);
		// Load and register the input code box
		final ValueTextBox codeIn = (ValueTextBox)findViewById(R.id.guiResSMDCode);
		outputCtrl = asTextView(R.id.guiResValue);
		outputCtrl.setOnLongClickListener(copyPasteListener);
		stdCtrl = asTextView(R.id.guiResIsStandard);
		registerAdjustable(codeIn);
		// Add listener to calculate on press
		EnterKeyListener.addListener(this, R.id.guiResSMDCode, this);
		loadPrefs();
		if (savedInstanceState != null && savedInstanceState.containsKey("lastCode")) {
			// Retrieve the code from a saved execution
			lastCode = savedInstanceState.getString("lastCode", lastCode);
			calculate(lastCode, false);
		} else {
			// Will set the last value appropriately
			String code = codeIn.getText().toString();
			if (code.length() < 1)
				code = lastCode;
			calculate(code, false);
		}
	}
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		lastCode = state.getString("lastCode", lastCode);
		calculate(lastCode, false);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("lastCode", lastCode);
	}
	@Override
	public void recalculate(final ValueGroup source) {
		// Group has one item
		final ValueTextBox input = (ValueTextBox)source.get(R.id.guiResSMDCode);
		// If underlined, prepend "R"
		String code = input.getText().toString();
		if (underlineCtrl.isChecked())
			code = "R" + code;
		calculate(code, true);
	}
	@Override
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		savePrefsCheckBox(prefs, R.id.guiResLine);
		prefs.putString("lastCode", lastCode);
	}
	/**
	 * Display the resistor value on screen.
	 *
	 * @param value the calculated value
	 */
	private void showValue(final EIAValue value) {
		outputCtrl.setText(value.toString());
		UIFunctions.checkEIATable(value, stdCtrl);
		copyPasteListener.setValue(value);
	}
	// All work is done in recalculate()
	@Override
	protected void update(ValueGroup group) { }
}