/***********************************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Stephen Carlson
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

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.UIFunctions;

/**
 * An extension of TextView which accepts values to be displayed, allows them to be copied by
 * long press, and optionally displays a heading.
 */
public class ValueOutputField extends TextView implements CopyValueSource {
	/**
	 * Handles long presses on this button.
	 */
	private CopyPasteListener copyPasteListener;
	/**
	 * The description requested for this value field ("Max Current", "Power Usage", ...)
	 */
	private String description;
	/**
	 * The number of decimal places used to display the value on screen, or 0 to use the
	 * default EngineeringValue.toString()
	 */
	private int sigfigs;
	/**
	 * True if the description should be shown in the text view.
	 */
	private boolean showDescription;
	/**
	 * The value to be displayed or copied.
	 */
	private EngineeringValue value;

	public ValueOutputField(Context context) {
		super(context);
		init(context, null);
	}
	public ValueOutputField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public ValueOutputField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}
	/**
	 * Gets the description of this value box.
	 *
	 * @return the short description (shown on the button)
	 */
	@Override
	public String getDescription() {
		return description;
	}
	/**
	 * Retrieves the number of significant figures which are shown.
	 *
	 * @return the total number of significant digits, or 0 if the default is used
	 */
	public int getSigfigs() {
		return sigfigs;
	}
	/**
	 * Gets the current value shown in this value field.
	 *
	 * @return the current value
	 */
	public EngineeringValue getValue() {
		return value;
	}
	/**
	 * Initializes the component.
	 *
	 * @param context the owning application context
	 * @param attrs the attributes of this element
	 */
	private void init(final Context context, final AttributeSet attrs) {
		String desc = "";
		boolean shows = true;
		int sf = 0;
		if (attrs != null) {
			// Read attributes for units
			final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ValueOutputField, 0, 0);
			// Read the values and substitute defaults
			desc = values.getString(R.styleable.ValueOutputField_description);
			shows = values.getBoolean(R.styleable.ValueOutputField_showDesc, true);
			sf = values.getInt(R.styleable.ValueOutputField_sigfigs, 0);
			values.recycle();
		} else
			Log.w("ValueOutputField", "No attributes specified, using defaults");
		description = desc;
		showDescription = shows;
		sigfigs = Math.max(sf, 0);
		// Update the copy listener (no activity parent in edit mode!)
		if (!isInEditMode()) {
			copyPasteListener = new CopyPasteListener(this);
			setOnLongClickListener(copyPasteListener);
		}
	}
	@Override
	public boolean isEditable() {
		return false;
	}
	/**
	 * Changes the currently shown value.
	 *
	 * @param newValue the new value
	 */
	public void setValue(final EngineeringValue newValue) {
		if (newValue != null && (value == null || !value.equals(newValue))) {
			value = newValue;
			if (copyPasteListener != null)
				copyPasteListener.setValue(newValue);
			updateText();
		}
	}
	/**
	 * Update the field text.
	 */
	private void updateText() {
		final SpannableStringBuilder text = new SpannableStringBuilder();
		final String desc = getDescription();
		// Only add the description if it is not empty
		if (showDescription && desc != null && desc.length() > 0) {
			text.append(UIFunctions.fromHtml(desc));
			text.append(": ");
		}
		// Use appropriate display method
		final EngineeringValue v = getValue();
		if (sigfigs > 0)
			text.append(v.valueToString(sigfigs));
		else
			text.append(v.toString());
		setText(text);
	}
	@Override
	public void updateValueUser(double newValue) {
		throw new UnsupportedOperationException("Read-only object");
	}
}
