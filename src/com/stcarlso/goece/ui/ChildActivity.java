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

import android.R;
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
import android.widget.CompoundButton;
import android.widget.TextView;
import com.stcarlso.goece.activity.ECEActivity;
import com.stcarlso.goece.utility.Calculatable;
import com.stcarlso.goece.utility.EngineeringValue;
import com.stcarlso.goece.utility.Restorable;

import java.util.*;

/**
 * An activity parent which handles the up action gracefully and fixes the gross problem with
 * value entry boxes needing a parent.
 */
public abstract class ChildActivity extends Activity implements Calculatable {
	/**
	 * List of fields registered for save/restore using registerAdjustable.
	 */
	protected LinkedList<Integer> fields;

	/**
	 * Initialize this activity.
	 */
	protected ChildActivity() {
		fields = new LinkedList<Integer>();
	}
	/**
	 * Implementation behind pushAdjustment in subclasses
	 *
	 * @param list the list to search
	 * @param id the ID of the view which was mutated
	 * @return the ID of the view to be updated
	 */
	protected int doPushAdjustment(final LinkedList<Integer> list, final int id) {
		int idx = -1;
		// Iterate through and pull it to the front
		final Iterator<Integer> lookup = list.iterator();
		while (idx < 0 && lookup.hasNext()) {
			final int newID = lookup.next();
			if (id == newID) {
				// Found it
				lookup.remove();
				list.addFirst(id);
				idx = list.getLast();
			}
		}
		return idx;
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
		for (int id : fields)
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
		final CompoundButton view = (CompoundButton)findViewById(id);
		final String tag = ECEActivity.getTag(view);
		// Only change if the preferences are initialized
		if (prefs.contains(tag))
			view.setChecked(prefs.getBoolean(tag, false));
	}
	/**
	 * Loads the text of a TextView object from the preferences. This works for EditText too.
	 *
	 * @param prefs the preference location to read the preferences
	 * @param id the ID of the control to load
	 */
	protected void loadPrefsTextView(final SharedPreferences prefs, final int id) {
		final TextView view = (TextView)findViewById(id);
		final String tag = ECEActivity.getTag(view);
		// Only change if the preferences are initialized
		if (prefs.contains(tag))
			view.setText(prefs.getString(tag, ""));
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
		case R.id.home:
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
	 * Registers the control to have its value saved and loaded.
	 *
	 * @param view the control to add to the list
	 */
	protected void registerAdjustable(final Restorable view) {
		fields.add(view.getId());
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
		for (int id : fields)
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
		final CompoundButton view = (CompoundButton)findViewById(id);
		prefs.putBoolean(ECEActivity.getTag(view), view.isChecked());
	}
	/**
	 * Saves the text of a TextView object in the preferences. This works for EditText too.
	 *
	 * @param prefs the preference location to store the preferences
	 * @param id the ID of the control to save
	 */
	protected void savePrefsTextView(final SharedPreferences.Editor prefs, final int id) {
		final TextView view = (TextView)findViewById(id);
		prefs.putString(ECEActivity.getTag(view), view.getText().toString());
	}
	/**
	 * Changes the value of the specified value entry box.
	 *
	 * @param box the entry box to modify
	 * @param newValue the value to be set
	 * @param errorID the error message to display if newValue is infinite or NaN
	 */
	protected void setValueEntry(final View box, final double newValue, final int errorID) {
		final ValueEntryBox ve = (ValueEntryBox)box;
		if (Double.isNaN(newValue) || Double.isInfinite(newValue))
			ve.setError(getString(errorID));
		else {
			ve.updateValue(newValue);
			ve.setError(null);
		}
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
