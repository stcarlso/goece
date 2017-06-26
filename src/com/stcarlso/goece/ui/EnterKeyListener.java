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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

/**
 * Convert "ENTER" or "Done" selections on the soft keyboard to a click action on a button.
 */
public class EnterKeyListener implements TextView.OnEditorActionListener {
	/**
	 * Arms a listener for done actions on the specified text field.
	 *
	 * @param view the parent view of the text field
	 * @param id the text field ID
	 * @param listener the listener to add
	 */
	public static void addListener(final View view, final int id,
								   final View.OnClickListener listener) {
		final TextView field = (TextView)view.findViewById(id);
		if (field != null)
			field.setOnEditorActionListener(new EnterKeyListener(listener));
	}

	/**
	 * The listener to fire.
	 */
	private final View.OnClickListener listener;

	/**
	 * Create an enter key listener that delegates to the given click listener.
	 *
	 * @param listener the listener (presumably on a button) that is fired when ENTER is pushed
	 */
	public EnterKeyListener(final View.OnClickListener listener) {
		if (listener == null)
			throw new NullPointerException("listener");
		this.listener = listener;
	}
	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE)
			// Click that button!
			listener.onClick(view);
		return true;
	}
}
