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
	 * Parses a 3-letter SMD resistor code.
	 *
	 * @param code the SMD resistor code
	 * @return the resistor value
	 * @throws IllegalArgumentException if the code cannot be parsed
	 */
	public static double parse3LetterCode(final String code) {
		final char multChar;
		double value;
		if (code == null || code.length() != 3)
			throw forResistorCode(code);
		multChar = code.charAt(2);
		try {
			// The vast majority of resistors fit this pattern
			int firstTwo = Integer.parseInt(code.substring(0, 2));
			if (multChar == 'R')
				// 33R
				value = (double)firstTwo;
			else if (Character.isDigit(multChar))
				// 10 ^ x
				value = (double)firstTwo * Math.pow(10.0, (double)(multChar - '0'));
			else {
				// E96 new generation
				firstTwo = EIAResistorTable.e96SMDCode(firstTwo);
				value = (double)firstTwo * EIAResistorTable.letterToMultiplier(multChar);
				if (value == 0.0)
					throw forResistorCode(code);
			}
		} catch (NumberFormatException ignore) {
			// Has an R somewhere?
			final char c1 = code.charAt(0), c2 = code.charAt(1), c3 = code.charAt(2);
			if (c1 == 'R') {
				try {
					// R10 = 0.10 ohm
					value = (double)Integer.parseInt(code.substring(1)) * 0.01;
				} catch (NumberFormatException e) {
					throw forResistorCode(code);
				}
			} else if (c2 == 'R' && Character.isDigit(c1) && Character.isDigit(c3))
				// 3R3 = 3.3 ohm
				value = (double)(c1 - '0') + (double)(c3 - '0') * 0.1;
			else
				throw forResistorCode(code);
		}
		return value;
	}
	/**
	 * Parses a 4-letter SMD resistor code.
	 *
	 * @param code the SMD resistor code
	 * @return the resistor value
	 * @throws IllegalArgumentException if the code cannot be parsed
	 */
	public static double parse4LetterCode(final String code) {
		final char multChar;
		double value;
		if (code == null || code.length() != 4)
			throw forResistorCode(code);
		multChar = code.charAt(3);
		try {
			// The vast majority of resistors fit this pattern
			int firstThree = Integer.parseInt(code.substring(0, 3));
			if (multChar == 'R')
				// 330R
				value = (double)firstThree;
			else if (Character.isDigit(multChar))
				// 10 ^ x
				value = (double)firstThree * Math.pow(10.0, (double)(multChar - '0'));
			else
				throw forResistorCode(code);
		} catch (NumberFormatException ignore) {
			// Has an R somewhere?
			final char c1 = code.charAt(0), c2 = code.charAt(1), c3 = code.charAt(2),
				c4 = code.charAt(3);
			if (c1 == 'R') {
				try {
					// R100 = 0.100 ohm
					value = (double)Integer.parseInt(code.substring(1)) * 0.001;
				} catch (NumberFormatException ignored) {
					throw forResistorCode(code);
				}
			} else if (c2 == 'R' && Character.isDigit(c1)) {
				try {
					// 3R30 = 3.30 ohm
					value = (double)(c1 - '0') + (double)Integer.parseInt(code.substring(2)) *
						0.01;
				} catch (NumberFormatException ignored) {
					throw forResistorCode(code);
				}
			} else if (c3 == 'R' && Character.isDigit(c4)) {
				try {
					// 47R0 = 47.0 ohm
					value = (double)Integer.parseInt(code.substring(0, 2)) +
						(double)(c4 - '0') * 0.1;
				} catch (NumberFormatException ignored) {
					throw forResistorCode(code);
				}
			} else
				throw new IllegalArgumentException("SMD resistor code: " + code);
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
		double value = 0.0;
		boolean error = true;
		final int tolerance;
		// 3 or 4 characters?
		switch (code.length()) {
		case 3:
			// 3 char
			tolerance = EIAResistorTable.letterToMultiplier(code.charAt(2)) == 0.0 ? 5 : 1;
			try {
				value = parse3LetterCode(code);
				error = false;
			} catch (IllegalArgumentException ignore) { }
			break;
		case 4:
			// 4 char
			tolerance = 1;
			try {
				value = parse4LetterCode(code);
				error = false;
			} catch (IllegalArgumentException ignore) { }
			break;
		default:
			tolerance = 0;
			break;
		}
		if (error) {
			// Oh no!
			if (showErrors)
				ECEActivity.errorMessage(this, R.string.guiResInvalid);
		} else {
			// Display it!
			showValue(value, tolerance);
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
		// Will set the last value appropriately
		recalculate(false);
		ECEActivity.initShowSoftKeyboard(findViewById(R.id.guiResSMDCode));
	}
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		lastCode = state.getString("lastCode", lastCode);
		calculate(lastCode, false);
	}
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
	 * @param tolerance the tolerance in % points
	 */
	private void showValue(final double value, final int tolerance) {
		final EIAResistorTable.EIASeries series;
		// Update value
		final TextView output = (TextView)findViewById(R.id.guiResValue);
		// Guess 5% for 3-digit and 1% otherwise
		if (tolerance == 5)
			series = EIAResistorTable.EIASeries.E24;
		else
			series = EIAResistorTable.EIASeries.E96;
		output.setText(ECEActivity.formatResistance(value) + " " + ECEActivity.P_M_SYMBOL +
			Integer.toString(tolerance) + "%");
		// Check the EIA standard value
		ResColorCodeActivity.checkEIATable(value, series,
			(TextView)findViewById(R.id.guiResIsStandard));
	}
}