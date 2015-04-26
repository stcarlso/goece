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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.*;

/**
 * An activity parent which handles the up action gracefully and fixes the gross problem with
 * value entry boxes needing a parent.
 */
public abstract class ChildActivity extends Activity implements Calculatable {
	/**
	 * Changes the value of the specified value entry box.
	 *
	 * @param box the entry box to modify
	 * @param newValue the value to be set
	 */
	protected static void setValueEntry(final ValueEntryBox box, final double newValue) {
		box.setValue(box.getValue().newValue(newValue));
		box.setError(null);
	}

	/**
	 * History of fields that were changed, with the most recently changed at the front.
	 */
	private LinkedList<Integer> recalcHistory;

	protected ChildActivity() {
		recalcHistory = new LinkedList<Integer>();
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
	 * Method for subclasses to override for restoration of standard Android objects not loaded
	 * automatically by the loadPrefs method (not Restorable).
	 *
	 * @param prefs the preference location to read the preferences
	 */
	protected void loadCustomPrefs(SharedPreferences prefs) { }
	/**
	 * Loads from application settings the values of all fields registered with
	 * registerAdjustable.
	 */
	protected void loadPrefs() {
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		for (int id : recalcHistory)
			// Restore all items marked as Restorable
			((Restorable)findViewById(id)).loadState(prefs);
		loadCustomPrefs(prefs);
	}
	/**
	 * Loads the state of a CheckBox object from the preferences.
	 *
	 * @param prefs the preference location to read the preferences
	 * @param id the ID of the control to load
	 */
	protected void loadPrefsCheckBox(final SharedPreferences prefs, final int id) {
		final CheckBox view = (CheckBox)findViewById(id);
		final String idS = Integer.toString(id);
		// Only change if the preferences are initialized
		if (prefs.contains(idS))
			view.setChecked(prefs.getBoolean(idS, false));
	}
	/**
	 * Loads the text of a TextView object from the preferences. This works for EditText too.
	 *
	 * @param prefs the preference location to read the preferences
	 * @param id the ID of the control to load
	 */
	protected void loadPrefsTextView(final SharedPreferences prefs, final int id) {
		final TextView view = (TextView)findViewById(id);
		final String idS = Integer.toString(id);
		// Only change if the preferences are initialized
		if (prefs.contains(idS))
			view.setText(prefs.getString(idS, ""));
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
	protected void onPause() {
		super.onPause();
		savePrefs();
	}
	protected void onResume() {
		super.onResume();
		loadPrefs();
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
	 * Method for subclasses to override for the saving of standard Android objects not stored
	 * automatically by the savePrefs method (not Restorable).
	 *
	 * @param prefs the preference location to store the preferences
	 */
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) { }
	/**
	 * Saves to application settings the values of all fields registered with
	 * registerAdjustable.
	 */
	protected void savePrefs() {
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		// Save all items registered as Restorable
		for (int id : recalcHistory)
			((Restorable)findViewById(id)).saveState(editor);
		saveCustomPrefs(editor);
		editor.apply();
	}
	/**
	 * Saves the state of a CheckBox object in the preferences.
	 *
	 * @param prefs the preference location to store the preferences
	 * @param id the ID of the control to save
	 */
	protected void savePrefsCheckBox(final SharedPreferences.Editor prefs, final int id) {
		final CheckBox view = (CheckBox)findViewById(id);
		prefs.putBoolean(Integer.toString(id), view.isChecked());
	}
	/**
	 * Saves the text of a TextView object in the preferences. This works for EditText too.
	 *
	 * @param prefs the preference location to store the preferences
	 * @param id the ID of the control to save
	 */
	protected void savePrefsTextView(final SharedPreferences.Editor prefs, final int id) {
		final TextView view = (TextView)findViewById(id);
		prefs.putString(Integer.toString(id), view.getText().toString());
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
	 * Shows a red indicator on the value indicating that an error occurred during calculation.
	 * Less intrusive than a dialog yet still noticeable.
	 *
	 * @param box the entry box to show the error
	 * @return the current value in that box
	 */
	protected EngineeringValue setErrorEntry(final ValueEntryBox box, final int errorID) {
		box.setError(getString(errorID));
		return box.getValue();
	}
}
