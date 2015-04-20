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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Make it your pastime, make it your mission!
 */
public final class ECEActivity extends FragmentActivity {
	/**
	 * The ohm symbol. Do not localize!
	 */
	public static final String OHM_SYMBOL = "\u03A9";
	/**
	 * The plus/minus symbol. Do not localize.
	 */
	public static final String P_M_SYMBOL = "\u00B1";

	/**
	 * The prefix the unit gets for each cut-off below.
	 */
	public static final String[] ENGR_NAMES = {
		"f", "n", "\u03BC", "m", "", "K", "M", "G", "T"
	};
	/**
	 * Cut-off values for engineering formatting.
	 */
	public static final double[] ENGR_THRESHOLD = {
		1e-12, 1e-9, 1e-6, 1e-3, 1, 1e3, 1e6, 1e9, 1e12, Double.MAX_VALUE
	};

	/**
	 * Formats the string as an engineering value (with K, M, G, ...)
	 *
	 * @param value the value to format
	 * @param suffix the suffix (F, V, A, ...)
	 * @return the string formatted very nicely
	 */
	public static String engineeringFormat(final double value, final String suffix) {
		String prefix = "";
		double engr = value;
		int sf = 0;
		// Look for the prefix
		for (int i = 0; i < ENGR_THRESHOLD.length && i < ENGR_NAMES.length; i++)
			if (value < ENGR_THRESHOLD[i + 1]) {
				// Found correct prefix
				prefix = ENGR_NAMES[i];
				engr = value / ENGR_THRESHOLD[i];
				break;
			}
		// Calculate sig figs
		if (engr >= 100.0)
			sf = 0;
		else if (engr >= 10.0)
			sf = 1;
		else if (engr >= 1.0)
			sf = 2;
		else
			sf = 3;
		return String.format("%." + Integer.toString(sf) + "f %s%s", engr, prefix, suffix);
	}
	/**
	 * Displays an error message popup.
	 *
	 * @param activity the activity showing the error
	 * @param messageID the string ID of the message
	 */
	public static void errorMessage(final Activity activity, final int messageID) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(messageID);
		builder.setPositiveButton(R.string.ok, new IgnoreOnClickListener());
		builder.create().show();
	}
	/**
	 * Formats a resistance value.
	 *
	 * @param value the resistor value to format
	 * @return the value with engineering format and the ohm symbol
	 */
	public static String formatResistance(final double value) {
		return engineeringFormat(value, OHM_SYMBOL);
	}
	/**
	 * Shows the soft keyboard after a short delay. Why android why?
	 *
	 * @param view the view which is requesting IME changes
	 */
	public static void initShowSoftKeyboard(final View view) {
		view.postDelayed(new Runnable() {
			public void run() {
				view.requestFocus();
				showSoftKeyboard(view, true);
			}
		}, 200L);
	}
	/**
	 * Shows or hides the soft keyboard for a given view.
	 *
	 * @param view the view which is requesting IME changes
	 * @param show whether the keyboard should be shown
	 */
	public static void showSoftKeyboard(final View view, final boolean show) {
		final Context context;
		if (view != null && (context = view.getContext()) != null) {
			// Mixed reviews as to this or setSoftInputMode
			final InputMethodManager manager = (InputMethodManager)context.
				getSystemService(Context.INPUT_METHOD_SERVICE);
			if (manager != null) {
				// Show or hide the keyboard
				if (show)
					manager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
				else
					manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}
	}

	/**
	 * Adds a tab to the menu.
	 *
	 * @param resId string resource ID of the tab name
	 */
	private <T extends Fragment> ActionBar.Tab addTab(final int resId, Class<T> target) {
		final ActionBar tabBar = getActionBar();
		// Uh?
		assert(tabBar != null);
		// Create a tab
		final ActionBar.Tab newTab = tabBar.newTab();
		newTab.setText(resId);
		newTab.setTabListener(new FragmentTabListener<T>(this, "", target));
		tabBar.addTab(newTab);
		return newTab;
	}
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar tabBar = getActionBar();
		// Uh?
		assert(tabBar != null);
		tabBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Analog, digital, circuits, pinouts, resources tabs
		tabBar.selectTab(addTab(R.string.guiTabAnalog, AnalogFragment.class));
		addTab(R.string.guiTabDigital, DigitalFragment.class);
	}
}
