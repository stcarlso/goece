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

import android.view.View;
import android.widget.Checkable;
import android.widget.ToggleButton;

/**
 * A listener used to emulate RadioGroup functionality for buttons not inside the same top
 * level layout.
 */
public class ButtonSwapListener implements View.OnClickListener {
	/**
	 * The button(s) to deselect when this one is set.
	 */
	private final Checkable[] other;
	/**
	 * Chained listener
	 */
	private final View.OnClickListener parent;

	/**
	 * Creates a button swap listener which will deactivate all of the specified buttons when
	 * the active one is checked.
	 *
	 * @param other the buttons to contain in this group
	 */
	public ButtonSwapListener(final Checkable... other) {
		this(null, other);
	}
	/**
	 * Creates a button swap listener which will deactivate all of the specified buttons when
	 * the active one is checked.
	 *
	 * @param parent a listener which will receive the event after this listener acts
	 * @param other the buttons to contain in this group
	 */
	public ButtonSwapListener(final View.OnClickListener parent, final Checkable... other) {
		if (other == null)
			throw new IllegalArgumentException("No other buttons");
		this.other = other;
		this.parent = parent;
	}
	@Override
	public void onClick(View view) {
		if (view instanceof Checkable) {
			if (((Checkable)view).isChecked())
				// If checked, unselect all other options
				for (final Checkable button : other)
					if (button != view)
						button.setChecked(false);
		}
		// Fire parent event
		if (parent != null)
			parent.onClick(view);
	}
}
