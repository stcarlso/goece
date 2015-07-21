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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import com.stcarlso.goece.R;

/**
 * A text box with a related spinner that lets the user enter the value with a choice of units.
 * These units can be non-standard such as kcmil, inches, foot pounds, etc. Conversion factors
 * for the units need to be specified.
 *
 * In addition, this box can be placed into a string entry mode for items such as AWG (which has
 * 000 etc.)
 */
public class CustomEntryBox extends RelativeLayout {
	public CustomEntryBox(Context context) {
		super(context);
	}
	public CustomEntryBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public CustomEntryBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	private void init(final Context context, final AttributeSet attrs) {
		String units = "", iv = "", newGroup = "", willAffect = "";
		if (attrs != null) {
			// Read attributes for units
			final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.CustomEntryBox, 0, 0);
			try {
				// Read the values and substitute defaults
				units = values.getString(R.styleable.CustomEntryBox_units);
				iv = values.getString(R.styleable.CustomEntryBox_value);
				newGroup = values.getString(R.styleable.CustomEntryBox_group);
				willAffect = values.getString(R.styleable.CustomEntryBox_affects);
			} catch (Exception e) {
				Log.e("CustomEntryBox", "Invalid attributes:", e);
			}
		} else
			// Probably not good
			Log.w("CustomEntryBox", "No units specified, defaulting to unitless!");
	}
}