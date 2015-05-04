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

import android.view.View;

import java.util.*;

/**
 * Abstract parent of all activities which use a single flat structure of calculation boxes
 * (where the least recently used is changed)
 */
public abstract class LIFOActivity extends ChildActivity {
	/**
	 * Returns the view ID of the registered view which was least recently changed.
	 *
	 * @return the ID of the registered view with the oldest value
	 */
	protected int leastRecentlyChanged() {
		return fields.getLast();
	}
	/**
	 * Returns the view ID of the registered view which was most recently changed.
	 *
	 * @return the ID of the view that was just changed
	 */
	protected int mostRecentlyChanged() {
		return fields.getFirst();
	}
	/**
	 * Call when a view is changed to moves it to the front of the adjustable list.
	 *
	 * @param view the view which was just modified
	 * @return the value of leastRecentlyChanged(), or -1 if the view argument was not found
	 */
	protected int pushAdjustment(final View view) {
		return doPushAdjustment(fields, view.getId());
	}
}
