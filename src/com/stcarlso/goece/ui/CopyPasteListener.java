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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.View;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.UIFunctions;

import java.util.*;

/**
 * A listener which can be added for long-presses to present a "Copy" dialog allowing the user
 * to copy the value of the field (textview, edittext, ValueEntryBox) to the clipboard. If
 * the clipboard contains a previously copied value, it can be pasted.
 */
public class CopyPasteListener implements View.OnLongClickListener {
	/**
	 * The parent activity used to display dialogs.
	 */
	private final Activity activity;
	/**
	 * The description of the copied value.
	 */
	private final String description;
	/**
	 * If nonzero, changes the copy listener to "copy 3900.0 mm" instead of "copy 3.90 kmm" on
	 * fixed unit values, with the number of sig figs fixed at this value.
	 */
	private int sigFigOverride;
	/**
	 * When "Paste" is chosen, this will be updated
	 */
	private final AbstractEntryBox<?> target;
	/**
	 * The value to be copied when invoked.
	 */
	private EngineeringValue value;

	/**
	 * Creates a new copy and paste listener with no value.
	 *
	 * @param target the parent entry field
	 */
	public CopyPasteListener(final AbstractEntryBox<?> target) {
		if (target == null)
			throw new NullPointerException("target");
		activity = UIFunctions.getActivity(target);
		description = target.getDescription();
		sigFigOverride = 0;
		this.target = target;
		value = new EngineeringValue(0.0);
	}
	/**
	 * Creates a new copy listener with no value.
	 *
	 * @param activity the parent activity
	 * @param description a short description of the value when copied
	 */
	public CopyPasteListener(final Activity activity, final String description) {
		if (activity == null)
			throw new NullPointerException("activity");
		if (description == null)
			throw new NullPointerException("description");
		this.activity = activity;
		this.description = description;
		sigFigOverride = 0;
		target = null;
		value = new EngineeringValue(0.0);
	}
	/**
	 * Adds the "copy ..." options to the specified list.
	 *
	 * @param options the location where copy options will be placed
	 */
	private void addCopyOptions(final List<String> options) {
		final EngineeringValue v = getValue();
		final double angle = v.getAngle(), mag = v.getValue();
		// Zero out the tolerance to allow unit copy without carrying that
		final EngineeringValue noTol = new EngineeringValue(mag, 0.0, v.getSigfigs(),
			v.getUnits());
		// Build copied text
		options.add(activity.getString(R.string.copyRaw, mag));
		options.add(activity.getString(R.string.copyUnits, withSigFigOverride(noTol)));
		if (v.getTolerance() > 0.0)
			options.add(activity.getString(R.string.copyAll, withSigFigOverride(v)));
		// Complex value options
		if (angle != 0.0) {
			final double i = v.getImaginary();
			// Copy angle in degrees, mag + angle, re + im
			options.add(activity.getString(R.string.copyAngle, angle));
			options.add(activity.getString(R.string.copyMagPha, mag, angle));
			if (i > 0.0)
				options.add(activity.getString(R.string.copyReIm, v.getReal(), i));
			else
				// Avoid "1.0 + -1.0i"
				options.add(activity.getString(R.string.copyReImMinus, v.getReal(), -i));
		}
	}
	/**
	 * Checks to see if the clipboard contains a pastable value.
	 *
	 * @return the clipboard value parsed if it can be pasted, or null otherwise
	 */
	private EngineeringValue addPasteOptions() {
		final ClipboardManager manager = (ClipboardManager)activity.getSystemService(
			Context.CLIPBOARD_SERVICE);
		EngineeringValue tp = null;
		if (manager.hasPrimaryClip() && target != null) {
			// Clipboard has contents, might as well search all
			final ClipData data = manager.getPrimaryClip();
			for (int i = 0; i < data.getItemCount(); i++) {
				final CharSequence toPaste = data.getItemAt(i).getText();
				if (toPaste != null && toPaste.length() > 0)
					// Look for pastable text
					try {
						tp = value.newValue(Double.parseDouble(toPaste.toString()));
						break;
					} catch (NumberFormatException ignore) { }
			}
		}
		return tp;
	}
	/**
	 * Retrieves the significant figure override.
	 *
	 * If nonzero, changes the copy listener to "copy 3900.0 mm" instead of "copy 3.90 kmm" on
	 * fixed unit values, with the number of sig figs fixed at this value.
	 *
	 * @return the number of significant figures used, or 0 if the value is automatically
	 * formatted like all default types
	 */
	public int getSigFigOverride() {
		return sigFigOverride;
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
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final List<String> options = new ArrayList<String>(8);
		// "Copy" options
		final String copy = activity.getString(R.string.copy), paste = activity.getString(
			R.string.paste);
		final EngineeringValue pv;
		addCopyOptions(options);
		pv = addPasteOptions();
		// Build text presented to user
		final int sz = options.size();
		final CharSequence[] copyOptions = new CharSequence[sz + (pv != null ? 1 : 0)];
		for (int i = 0; i < sz; i++)
			copyOptions[i] = Html.fromHtml(copy + " \"" + options.get(i) + "\"");
		// Add "Paste ..."
		if (pv != null)
			copyOptions[sz] = Html.fromHtml(paste + " \"" + withSigFigOverride(pv) + "\"");
		builder.setItems(copyOptions, new CopyPasteItemsListener(options, pv));
		final AlertDialog dialog = builder.setTitle(Html.fromHtml(description)).create();
		dialog.show();
		return true;
	}
	/**
	 * Changes the significant figure override.
	 *
	 * If nonzero, changes the copy listener to "copy 3900.0 mm" instead of "copy 3.90 kmm" on
	 * fixed unit values, with the number of sig figs fixed at this value.
	 *
	 * @param sigFigOverride the number of significant figures to be used, or 0 if the value
	 * should be automatically formatted like all default types
	 */
	public void setSigFigOverride(final int sigFigOverride) {
		if (sigFigOverride < 0)
			throw new IllegalArgumentException("sigFigOverride >= 0");
		this.sigFigOverride = sigFigOverride;
	}
	/**
	 * Changes the value which will be copied upon long press.
	 *
	 * @param value the value to be copied
	 */
	public void setValue(final EngineeringValue value) {
		this.value = value;
	}
	/**
	 * Reports the specified engineering value with the sigFigOverride applied if necessary.
	 *
	 * @param v the value to format
	 * @return the value as a string
	 */
	private String withSigFigOverride(final EngineeringValue v) {
		final String valueText;
		final int sfo = getSigFigOverride();
		// Use exponential string if overridden
		if (sfo > 0)
			valueText = v.toExponentialString(sfo);
		else
			valueText = v.toString();
		return valueText;
	}

	/**
	 * A listener which copies text when an option is selected in a dialog.
	 */
	private class CopyPasteItemsListener implements Dialog.OnClickListener {
		/**
		 * The text available to copy.
		 */
		private final List<String> copyText;
		/**
		 * If a value can be pasted, this is it.
		 */
		private final EngineeringValue toPaste;

		protected CopyPasteItemsListener(final List<String> copyText,
										 final EngineeringValue toPaste) {
			this.copyText = copyText;
			this.toPaste = toPaste;
		}
		public void onClick(DialogInterface dialog, int which) {
			final int len = copyText.size();
			if (which >= 0 && which < len) {
				// Copy the text to the system clipboard
				final ClipboardManager manager = (ClipboardManager)activity.getSystemService(
					Context.CLIPBOARD_SERVICE);
				manager.setPrimaryClip(ClipData.newPlainText(description, copyText.get(which)));
			} else if (which == len && toPaste != null && target != null) {
				// Paste the text from the system clipboard
				// If we allow imaginary pasting in the future this will need to be updated
				target.updateValue(toPaste.getValue());
				target.callOnCalculateListener();
			}
			dialog.dismiss();
		}
	}
}
