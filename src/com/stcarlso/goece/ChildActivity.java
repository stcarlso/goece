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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;

import java.util.*;

/**
 * An activity parent which handles the up action gracefully and fixes the gross problem with
 * value entry boxes needing a parent.
 */
public abstract class ChildActivity extends Activity implements Calculatable {
	/**
	 * History of fields that were changed, with the most recently changed at the front.
	 */
	private LinkedList<Integer> recalcHistory;

	protected ChildActivity() {
		recalcHistory = new LinkedList<Integer>();
	}
	/**
	 * Retrieves the value of the specified value entry box.
	 *
	 * @param id the entry box to fetch
	 * @return the value in that box
	 */
	protected EngineeringValue getValueEntry(final int id) {
		return ((ValueEntryBox)findViewById(id)).getValue();
	}
	/**
	 * Returns the view ID of the registered view which was least recently changed.
	 *
	 * @return the ID of the registered view with the oldest value
	 */
	protected int leastRecentlyChanged() {
		return recalcHistory.getLast();
	}
	/**
	 * Returns the view ID of the registered view which was most recently changed.
	 *
	 * @return the ID of the view that was just changed
	 */
	protected int mostRecentlyChanged() {
		return recalcHistory.getFirst();
	}
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar bar = getActionBar();
		// Uh?
		assert (bar != null);
		bar.setDisplayHomeAsUpEnabled(true);
	}
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Find the parent activity
			final Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent))
				// Launched from another application, create a new task
				TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).
					startActivities();
			else
				// No need to create a new back stack
				NavUtils.navigateUpTo(this, upIntent);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * Call when a view is changed to moves it to the front of the adjustable list.
	 *
	 * @param view the view which was just modified
	 * @return the value of leastRecentlyChanged(), or -1 if the view argument was not found
	 */
	protected int pushAdjustment(final View view) {
		final int id = view.getId();
		int idx = -1;
		// Iterate through and pull it to the front
		final Iterator<Integer> lookup = recalcHistory.iterator();
		while (idx < 0 && lookup.hasNext()) {
			final int newID = lookup.next();
			if (id == newID) {
				// Found it
				lookup.remove();
				recalcHistory.addFirst(id);
				idx = leastRecentlyChanged();
			}
		}
		return idx;
	}
	/**
	 * Registers the control as an adjustable that will follow the LRU rules (least recently
	 * used) rules to determine the value to be calculated.
	 *
	 * @param view the control to add to the list; the last control added initially is the
	 * first to be adjusted!
	 */
	protected void registerAdjustable(final View view) {
		recalcHistory.add(view.getId());
	}
	/**
	 * Sets the listener and parent activity of the specified value entry box. Useful for the
	 * vast majority of activities.
	 *
	 * @param id the entry box to configure
	 */
	protected void setupValueEntryBox(final int id) {
		final ValueEntryBox box = ((ValueEntryBox)findViewById(id));
		box.setParentActivity(this);
		box.setOnCalculateListener(this);
		registerAdjustable(box);
	}
	/**
	 * Changes the value of the specified value entry box.
	 *
	 * @param id the entry box to modify
	 * @param newValue the value to be set
	 * @return the old value of that box
	 */
	protected EngineeringValue setValueEntry(final int id, final EngineeringValue newValue) {
		final ValueEntryBox box = ((ValueEntryBox)findViewById(id));
		final EngineeringValue oldValue = box.getValue();
		box.setValue(newValue);
		return oldValue;
	}
}
