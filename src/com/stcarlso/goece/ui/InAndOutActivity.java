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
import com.stcarlso.goece.utility.Restorable;

import java.util.*;

/**
 * Abstract parent of activities which have "input" and "output" fields; a separate LRU list
 * is maintained for each.
 */
public abstract class InAndOutActivity extends ChildActivity {
	/**
	 * Input fields in LRU order.
	 */
	private LinkedList<Integer> ins;
	/**
	 * Output fields in LRU order.
	 */
	private LinkedList<Integer> outs;

	protected InAndOutActivity() {
		ins = new LinkedList<Integer>();
		outs = new LinkedList<Integer>();
	}
	/**
	 * Returns the view ID of the registered output view which was least recently changed.
	 *
	 * @return the ID of the registered output view with the oldest value
	 */
	protected int leastRecentlyChanged() {
		return outs.getLast();
	}
	/**
	 * Returns the view ID of the registered output view which was most recently changed.
	 *
	 * @return the ID of the output view that was just changed
	 */
	protected int mostRecentlyChanged() {
		return outs.getFirst();
	}
	/**
	 * Call when a view is changed to moves it to the front of the adjustable list. Finds the
	 * view to be updated as the least recently used in the bank which was not changed.
	 *
	 * @param view the view which was just modified
	 * @return the view to be updated, or -1 if the view argument was not found
	 */
	protected int pushAdjustment(final View view) {
		final int inId = doPushAdjustment(ins, view.getId()), retId;
		if (inId < 0) {
			// Maybe in the out bank?
			doPushAdjustment(outs, view.getId());
			retId = ins.getLast();
		} else
			retId = outs.getLast();
		return retId;
	}
	/**
	 * Registers the control to have its value saved and loaded.
	 *
	 * @param view the control to add to the list
	 * @param in true to register in the input list; false to register in output list
	 */
	protected void registerAdjustable(final Restorable view, final boolean in) {
		registerAdjustable(view);
		if (in)
			ins.add(view.getId());
		else
			outs.add(view.getId());
	}
	/**
	 * Sets the listener and parent activity of the specified value entry box. Useful for the
	 * vast majority of activities.
	 *
	 * @param id the entry box to configure
	 * @param in true to register in the input list; false to register in output list
	 */
	protected void setupValueEntryBox(final int id, final boolean in) {
		setupValueEntryBox(id);
		if (in)
			ins.add(id);
		else
			outs.add(id);
	}
}
