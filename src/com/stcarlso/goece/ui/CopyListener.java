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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.EngineeringValue;

/**
 * A listener which can be added for long-presses to present a "Copy" dialog allowing the user
 * to copy the value of the field (textview, edittext, ValueEntryBox) to the clipboard.
 */
public class CopyListener implements View.OnLongClickListener {
	/**
	 * The parent activity used to display dialogs.
	 */
	private final Activity activity;
	/**
	 * The description of the copied value.
	 */
	private final String description;
	/**
	 * The value to be copied when invoked.
	 */
	private EngineeringValue value;

	/**
	 * Creates a new copy listener with no value.
	 *
	 * @param activity the parent activity
	 * @param description a short description of the value when copied
	 */
	public CopyListener(final Activity activity, final String description) {
		if (activity == null)
			throw new NullPointerException("activity");
		this.activity = activity;
		this.description = description;
		value = new EngineeringValue(0.0);
	}
	/**
	 * Gets the value saved in this listener.
	 *
	 * @return the last set value
	 */
	public EngineeringValue getValue() {
		return value;
	}
	public boolean onLongClick(View v) {
		// "Copy" options
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final String[] options = new String[value.getTolerance() > 0.0 ? 4 : 3],
			copyOptions = new String[options.length];
		// Zero out the tolerance to allow unit copy without carrying that
		final EngineeringValue noTol = new EngineeringValue(value.getValue(), 0.0,
			value.getSigfigs(), value.getUnits());
		final String copy = activity.getString(R.string.copy);
		builder.setTitle(R.string.copy);
		// Build copied text
		options[0] = activity.getString(R.string.copyRaw, value.getValue());
		options[1] = activity.getString(R.string.copySig, value.getSignificand());
		options[2] = activity.getString(R.string.copyUnits, noTol);
		if (options.length > 3)
			options[3] = activity.getString(R.string.copyAll, value);
		// Build text presented to user
		for (int i = 0; i < copyOptions.length; i++)
			copyOptions[i] = copy + " \"" + options[i] + "\"";
		builder.setItems(copyOptions, new CopyItemsListener(options));
		final AlertDialog dialog = builder.create();
		dialog.show();
		return true;
	}
	/**
	 * Changes the value which will be copied upon long press.
	 *
	 * @param value the value to be copied
	 */
	public void setValue(EngineeringValue value) {
		this.value = value;
	}


	/**
	 * A listener which copies text when an option is selected in a dialog.
	 */
	private class CopyItemsListener implements Dialog.OnClickListener {
		/**
		 * The text available to copy.
		 */
		private String[] copyText;

		protected CopyItemsListener(final String[] copyText) {
			this.copyText = copyText;
		}
		public void onClick(DialogInterface dialog, int which) {
			if (which >= 0 && which < copyText.length) {
				// Copy the text to the system clipboard
				final ClipboardManager manager = (ClipboardManager)activity.getSystemService(
					Context.CLIPBOARD_SERVICE);
				manager.setPrimaryClip(ClipData.newPlainText(description, copyText[which]));
			}
			dialog.dismiss();
		}
	}
}
