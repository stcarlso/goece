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
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import com.stcarlso.goece.R;
import com.stcarlso.goece.activity.ECEActivity;
import com.stcarlso.goece.utility.ValueControl;

/**
 * A version of EditText which implements value control semantics, allowing easy save/restore
 * and group edit effects.
 */
public class ValueTextBox extends EditText implements ValueControl {
	/**
	 * When this box is changed, this field is used to determine which group is affected.
	 */
	private String affects;
	/**
	 * The group assigned to this textbox. All members in a group are LRUed to determine which
	 * one is changed when the group is affected.
	 */
	private String group;

	public ValueTextBox(Context context) {
		super(context);
		init(context, null);
	}
	public ValueTextBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public ValueTextBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	public String getAffects() {
		return affects;
	}
	public String getGroup() {
		return group;
	}
	private void init(final Context context, final AttributeSet attrs) {
		String newGroup = "", willAffect = "";
		if (attrs != null) {
			// Read attributes for affects and group
			final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ValueTextBox, 0, 0);
			try {
				// Read the values and substitute defaults
				newGroup = values.getString(R.styleable.ValueTextBox_group);
				willAffect = values.getString(R.styleable.ValueTextBox_affects);
			} catch (Exception e) {
				Log.e("ValueTextBox", "Invalid attributes:", e);
			}
		}
		group = newGroup;
		affects = willAffect;
	}
	public void loadState(SharedPreferences prefs) {
		final String tag = ECEActivity.getTag(this);
		// Only change if the preferences are initialized
		if (prefs.contains(tag))
			setText(prefs.getString(tag, ""));
	}
	public void saveState(SharedPreferences.Editor prefs) {
		prefs.putString(ECEActivity.getTag(this), getText().toString());
	}
}
