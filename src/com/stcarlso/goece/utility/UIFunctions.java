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

package com.stcarlso.goece.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.stcarlso.goece.R;

/**
 * Contains functions used in multiple locations in the user interface.
 */
public final class UIFunctions {
	/**
	 * Shared code between color code and SMD to indicate standard/non-standard values.
	 *
	 * @param value the component value
	 * @param std the text box to update
	 * @return true if the component was a standard value, or false otherwise
	 */
	public static boolean checkEIATable(final EIAValue value, final TextView std) {
		final double res = value.getValue();
		// Is it standard?
		final EIATable.EIASeries series = value.getSeries();
		final boolean isStandard = EIATable.isEIAValue(res, series);
		final String tolStr = EngineeringValue.toleranceToString(value.getTolerance());
		if (isStandard) {
			// In standard series, say so
			std.setTextColor(Color.GREEN);
			std.setText(String.format("Standard %s%% value", tolStr));
		} else {
			// Not in standard series, indicate closest value
			final double closest = EIATable.nearestEIAValue(res, series), errorPct;
			// Calculate % error
			if (res <= 0.0)
				errorPct = 0.0;
			else
				errorPct = 100.0 * (closest - res) / res;
			// Display appropriate message
			std.setTextColor(Color.RED);
			std.setText(String.format("Nearest %s%% value is %s [%+.1f%%]", tolStr,
				new EIAValue(closest, series, 0.0, value.getUnits()), errorPct));
		}
		return isStandard;
	}
	/**
	 * Displays an error message popup.
	 *
	 * @param activity the activity showing the error
	 * @param messageID the string ID of the message
	 */
	public static void errorMessage(final Activity activity, final int messageID) {
		if (activity != null) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(messageID);
			builder.setPositiveButton(R.string.ok, new IgnoreOnClickListener());
			builder.create().show();
		}
	}
	/**
	 * Reports the parent activity of a View, or null if the view has no parent activity.
	 *
	 * @param view the view to investigate
	 * @return the Activity object which is hosting the view
	 */
	public static Activity getActivity(final View view) {
		if (view == null)
			throw new NullPointerException("view");
		Context context = view.getContext();
		Activity parent = null;
		while (parent == null && (context instanceof ContextWrapper)) {
			if (context instanceof Activity)
				parent = (Activity)context;
			else
				context = ((ContextWrapper)context).getBaseContext();
		}
		return parent;
	}
	/**
	 * Retrieves the tag from a visual element.
	 *
	 * @param view the view to check
	 * @return the view tag, or the view ID as a string if no tag was defined (bad!)
	 */
	public static String getTag(final View view) {
		if (view == null)
			throw new NullPointerException("view");
		final Object tag = view.getTag();
		final String idS;
		if (tag != null)
			idS = tag.toString();
		else {
			idS = Integer.toString(view.getId());
			Log.w("UIFunctions", "Component is missing tag: " + view.getId());
		}
		return idS;
	}
	/**
	 * Hides the soft keyboard, used to silence warnings about inactive InputConnection.
	 *
	 * @param activity the current activity, use this method in onPause()
	 */
	public static void hideKeyboard(final Activity activity) {
		if (activity == null)
			throw new NullPointerException("activity");
		final InputMethodManager inputMethodManager = (InputMethodManager)activity.
			getSystemService(Context.INPUT_METHOD_SERVICE);
		// Look for the root view
		final View view = activity.findViewById(android.R.id.content);
		if (view != null)
			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	/**
	 * Assigns text to a static text field.
	 *
	 * @param view the root view
	 * @param id the ID of the text field
	 * @param text the text to load (<b>Will be parsed as HTML</b>)
	 */
	public static void setLabelText(final View view, final int id, final String text) {
		if (view == null)
			throw new NullPointerException("view");
		((TextView)view.findViewById(id)).setText(Html.fromHtml(text));
	}
}