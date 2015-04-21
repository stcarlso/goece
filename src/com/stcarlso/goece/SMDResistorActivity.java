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

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Tab for the SMD resistor code calculator.
 */
public class SMDResistorActivity extends ChildActivity implements View.OnClickListener {
	/**
	 * Generate an exception for invalid resistor codes.
	 *
	 * @param code the resistor code which was invalid
	 * @return an IllegalArgumentException ready to throw
	 */
	private static IllegalArgumentException forResistorCode(final String code) {
		return new IllegalArgumentException("SMD resistor code: " + code);
	}
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
	public static EIAResistorValue parse3LetterCode(final String code) {
		EIAResistorValue value = null;
		if (code != null && code.length() == 3) {
			final char multChar = code.charAt(2);
			try {
				if (code.indexOf('R') >= 0)
					// 4R7
					value = new EIAResistorValue(parseRValue(code), EIAResistorValue.E24);
				else {
					// The vast majority of resistors fit this pattern
					final int prefix = Integer.parseInt(code.substring(0, 2));
					if (Character.isDigit(multChar))
						// 10 ^ x
						value = new EIAResistorValue(prefix * Math.pow(10.0, multChar - '0'),
							EIAResistorValue.E24);
					else {
						// E96 new generation
						final double vv = EIAResistorTable.e96SMDCode(prefix) *
							EIAResistorTable.letterToMultiplier(multChar);
						if (vv != 0.0)
							// This is a 1% resistor!
							value = new EIAResistorValue(vv, EIAResistorValue.E96);
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
	public static EIAResistorValue parse4LetterCode(final String code) {
		EIAResistorValue value = null;
		if (code != null && code.length() == 4) {
			final char multChar = code.charAt(3);
			try {
				if (code.indexOf('R') >= 0)
					// 33R0
					value = new EIAResistorValue(parseRValue(code), EIAResistorValue.E96);
				else if (Character.isDigit(multChar))
					// The vast majority of resistors fit this pattern
					value = new EIAResistorValue(Integer.parseInt(code.substring(0, 3)) *
						Math.pow(10.0, multChar - '0'), EIAResistorValue.E96);
			} catch (NumberFormatException ignore) { }
		}
		return value;
	}

	/**
	 * The last successfully calculated resistor code.
	 */
	private String lastCode;

	public SMDResistorActivity() {
		lastCode = "000";
	}
	/**
	 * Calculates and displays the resistor value.
	 *
	 * @param code the SMD resistor code to calculate
	 * @param showErrors whether error messages should be shown
	 */
	private void calculate(final String code, final boolean showErrors) {
		final EIAResistorValue value;
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
				ECEActivity.errorMessage(this, R.string.guiResInvalid);
		} else {
			// Display it!
			showValue(value);
			lastCode = code;
		}
	}
	/**
	 * Receive click events from the calculate button and recalculate.
	 *
	 * @param v the source view
	 */
	public void onClick(View v) {
		recalculate(true);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smdresistor);
		EnterKeyListener.addListener(this, R.id.guiResSMDCode, this);
		if (savedInstanceState != null && savedInstanceState.containsKey("lastCode")) {
			// Retrieve the code from a saved execution
			lastCode = savedInstanceState.getString("lastCode", lastCode);
			calculate(lastCode, false);
		} else
			// Will set the last value appropriately
			recalculate(false);
		ECEActivity.initShowSoftKeyboard(findViewById(R.id.guiResSMDCode));
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
	/**
	 * Recalculates the resistor value and checks to see if it is actually available in that
	 * EIA tolerance series.
	 *
	 * @param showErrors true if error dialogs are to be shown, or false otherwise
	 */
	public void recalculate(final boolean showErrors) {
		final EditText input = (EditText)findViewById(R.id.guiResSMDCode);
		final boolean underline = ((CheckBox)findViewById(R.id.guiResLine)).isChecked();
		// If underlined, prepend "R"
		String code = input.getText().toString();
		if (underline)
			code = "R" + code;
		calculate(code, showErrors);
	}
	/**
	 * Display the resistor value on screen.
	 *
	 * @param value the calculated value
	 */
	private void showValue(final EIAResistorValue value) {
		((TextView)findViewById(R.id.guiResValue)).setText(value.toString());
		ResColorCodeActivity.checkEIATable(value, (TextView)findViewById(R.id.guiResIsStandard));
	}
}