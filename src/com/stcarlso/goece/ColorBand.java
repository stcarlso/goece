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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * A component which displays colors on the screen and can be checked for the selected color.
 */
public class ColorBand extends LinearLayout implements View.OnClickListener, Restorable {
	/**
	 * Copies of the buttons on the screen.
	 */
	private Button[] colors;
	/**
	 * Called when a new value is selected
	 */
	private Calculatable listener;
	/**
	 * The currently selected button index.
	 */
	private int value;

	public ColorBand(Context context) {
		super(context);
		init(context, null);
	}
	public ColorBand(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public ColorBand(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	/**
	 * Fires the recalculate method of the attached listener, if it exists.
	 */
	protected void callOnCalculateListener() {
		if (listener != null)
			listener.recalculate(this);
	}
	private void init(final Context context, final AttributeSet attrs) {
		View.inflate(context, R.layout.colorband, this);
		colors = new Button[13];
		listener = null;
		value = 0;
		// Load all buttons
		colors[0] = (Button)findViewById(R.id.guiColor0);
		colors[1] = (Button)findViewById(R.id.guiColor1);
		colors[2] = (Button)findViewById(R.id.guiColor2);
		colors[3] = (Button)findViewById(R.id.guiColor3);
		colors[4] = (Button)findViewById(R.id.guiColor4);
		colors[5] = (Button)findViewById(R.id.guiColor5);
		colors[6] = (Button)findViewById(R.id.guiColor6);
		colors[7] = (Button)findViewById(R.id.guiColor7);
		colors[8] = (Button)findViewById(R.id.guiColor8);
		colors[9] = (Button)findViewById(R.id.guiColor9);
		colors[10] = (Button)findViewById(R.id.guiColor10);
		colors[11] = (Button)findViewById(R.id.guiColor11);
		colors[12] = (Button)findViewById(R.id.guiColor12);
		// Add click listeners
		for (Button color : colors)
			color.setOnClickListener(this);
		// Attribute parsing
		if (attrs != null)
			loadAttributes(context, attrs);
		else
			// Attributes were null!
			setValue(0);
	}
	private void loadAttributes(final Context context, final AttributeSet attrs) {
		final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
			R.styleable.ColorBand, 0, 0);
		int pos = 0;
		try {
			// Read the values and substitute defaults
			pos = values.getInteger(R.styleable.ColorBand_pos, 0);
		} catch (Exception e) {
			Log.e("ColorBand", "Invalid attributes:", e);
		}
		// Show or hide colors
		colors[0].setVisibility(pos >= 1 && pos <= 3 ? VISIBLE : INVISIBLE);
		for (int i = 1; i < 8; i++) {
			if (i == 3 || i == 4)
				// Orange and yellow not on last
				colors[i].setVisibility(pos <= 3 ? VISIBLE : INVISIBLE);
			else
				// Turn on brown, red, green, blue, violet
				colors[i].setVisibility(VISIBLE);
		}
		// Grey and white have special visibility
		colors[8].setVisibility(pos == 3 ? INVISIBLE : VISIBLE);
		colors[9].setVisibility(pos <= 2 ? VISIBLE : INVISIBLE);
		// Optional on band 3, 5
		colors[10].setVisibility((pos == 2 || pos == 4) ? VISIBLE : INVISIBLE);
		colors[11].setVisibility(pos >= 3 ? VISIBLE : INVISIBLE);
		colors[12].setVisibility(pos >= 3 ? VISIBLE : INVISIBLE);
		// Set the correct initial value
		switch (pos) {
		case 0:
			// First band cannot be black
			setValue(1);
			break;
		case 2:
			// Start with 4 bands
			setValue(10);
			break;
		case 4:
			// 5%
			setValue(11);
			break;
		default:
			setValue(0);
			break;
		}
	}
	public void loadState(SharedPreferences prefs) {
		final int idx = prefs.getInt(Integer.toString(getId()), -1);
		if (idx >= 0 && idx < colors.length)
			setValue(idx);
	}
	public void saveState(SharedPreferences.Editor prefs) {
		prefs.putInt(Integer.toString(getId()), getValue());
	}
	public void onClick(View view) {
		final int id = view.getId(), oldValue = value;
		for (int i = 0; i < colors.length; i++)
			if (colors[i].getId() == id) {
				// Found touched button
				setValue(i);
				break;
			}
		// Fire change event, if actually changed
		if (oldValue != value)
			callOnCalculateListener();
	}
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof ECESavedState) {
			final ECESavedState<Integer> ecess = (ECESavedState<Integer>)state;
			super.onRestoreInstanceState(ecess.getSuperState());
			// Attempt to restore our state
			final int v = ecess.getValue(), oldValue = value;
			if (v >= 0 && v < colors.length) {
				setValue(v);
				if (v != oldValue)
					callOnCalculateListener();
			}
		} else
			super.onRestoreInstanceState(state);
	}
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable parentState = super.onSaveInstanceState();
		return new ECESavedState<Integer>(parentState, value);
	}
	/**
	 * Gets the currently selected band index. 0-9 = colors, 10 = none, 11-12 = gold/silver
	 *
	 * @return the current value of this band
	 */
	public int getValue() {
		return value;
	}
	/**
	 * Changes the listener fired when the value is changed and recalculation is required.
	 *
	 * @param listener the listener to be fired
	 */
	public void setOnCalculateListener(final Calculatable listener) {
		this.listener = listener;
	}
	/**
	 * Changes the currently selected index. Does not fire the calculation listener.
	 *
	 * @param newValue the new value
	 */
	public void setValue(int newValue) {
		for (int i = 0; i < colors.length; i++)
			colors[i].setSelected(i == newValue);
		value = newValue;
	}
}
