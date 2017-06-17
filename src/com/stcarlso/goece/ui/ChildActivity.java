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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.*;
import com.stcarlso.goece.utility.Calculatable;
import com.stcarlso.goece.utility.UIFunctions;
import com.stcarlso.goece.utility.ValueControl;

import java.util.*;

/**
 * An activity parent which handles the up action gracefully and fixes the gross problem with
 * value entry boxes needing a parent.
 */
public abstract class ChildActivity extends Activity implements Calculatable {
	/**
	 * Contains all data entry controls.
	 */
	protected final ValueBoxContainer controls;
	/**
	 * List of fields registered with registerAdjustable.
	 */
	private final ValueGroup fields;
	/**
	 * List of fields again, but mapped by group.
	 */
	protected final Map<String, ValueGroup> groups;

	/**
	 * Initialize this activity.
	 */
	protected ChildActivity() {
		controls = new ValueBoxContainer();
		fields = new ValueGroup("");
		groups = new HashMap<String, ValueGroup>(32);
	}
	/**
	 * Retrieves a checkbox by its ID.
	 *
	 * @param id the ID of the checkbox
	 * @return the checkbox control
	 */
	protected CheckBox asCheckBox(final int id) {
		return (CheckBox)findViewById(id);
	}
	/**
	 * Retrieves an image by its ID.
	 *
	 * @param id the ID of the image
	 * @return the image view control
	 */
	protected ImageView asImageView(final int id) {
		return (ImageView)findViewById(id);
	}
	/**
	 * Retrieves a radio button by its ID.
	 *
	 * @param id the ID of the radio button
	 * @return the radio button control
	 */
	protected RadioButton asRadioButton(final int id) {
		return (RadioButton)findViewById(id);
	}
	/**
	 * Retrieves a spinner by its ID.
	 *
	 * @param id the ID of the spinner
	 * @return the spinner control
	 */
	protected Spinner asSpinner(final int id) {
		return (Spinner)findViewById(id);
	}
	/**
	 * Retrieves a label by its ID.
	 *
	 * @param id the ID of the label
	 * @return the label control
	 */
	protected TextView asTextView(final int id) {
		return (TextView)findViewById(id);
	}
	/**
	 * Retrieves a value field by its ID.
	 *
	 * @param id the ID of the output field (ValueOutputField class)
	 * @return the value label control
	 */
	protected ValueOutputField asValueField(final int id) {
		return (ValueOutputField)findViewById(id);
	}
	/**
	 * Slightly stronger typed version of findViewById that is also much faster, but only works
	 * on value controls registered with registerAdjustable.
	 *
	 * @param id the control ID to look up
	 * @return the control with that ID, or null if no registered control has that ID
	 */
	protected ValueControl findValueById(final int id) {
		return fields.get(id);
	}
	/**
	 * Method for subclasses to override for restoration of standard Android objects not loaded
	 * automatically by the loadPrefs method (not Restorable).
	 *
	 * @param prefs the preference location to read the preferences
	 */
	protected void loadCustomPrefs(SharedPreferences prefs) {
	}
	/**
	 * Loads from application settings the values of all fields registered with
	 * registerAdjustable.
	 */
	protected void loadPrefs() {
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		for (ValueControl control : fields)
			// Restore all items marked as Restorable
			control.loadState(prefs);
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
		final String tag = UIFunctions.getTag(view);
		// Only change if the preferences are initialized
		if (prefs.contains(tag))
			view.setChecked(prefs.getBoolean(tag, false));
	}
	/**
	 * Loads the state of a Spinner object from the preferences.
	 *
	 * @param prefs the preference location to read the preferences
	 * @param id the ID of the control to load
	 */
	protected void loadPrefsSpinner(final SharedPreferences prefs, final int id) {
		final Spinner view = asSpinner(id);
		final String tag = UIFunctions.getTag(view);
		// Only change if the preferences are initialized
		if (prefs.contains(tag))
			view.setSelection(prefs.getInt(tag, 0), false);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar bar = getActionBar();
		// Uh?
		if (bar != null)
			bar.setDisplayHomeAsUpEnabled(true);
	}
	@Override
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
	@Override
	protected void onPause() {
		super.onPause();
		savePrefs();
	}
	@Override
	protected void onResume() {
		super.onResume();
		loadPrefs();
	}
	@Override
	public void recalculate(ValueControl source) {
		// Extract the group and execute that group
		final String group = source.getGroup();
		final String targets = source.getAffects();
		if (group != null && targets != null) {
			final ValueGroup src = groups.get(group);
			if (src != null) {
				// Much faster than regex (split)
				final StringTokenizer str = new StringTokenizer(targets, ",");
				String target;
				// Update MRU list and request group update
				src.use(source.getId());
				update(src);
				// Allow multi-targeting
				while (str.hasMoreTokens() && (target = str.nextToken()) != null) {
					final ValueGroup dest = groups.get(target);
					if (dest != null)
						recalculate(dest);
					else
						// Target not found
						Log.w("ChildActivity", "Target \"" + target + "\" not found");
				}
			} else
				// Group not found
				Log.w("ChildActivity", "Group \"" + group + "\" not found");
		}
	}
	/**
	 * Recalculates the values in the specified group.
	 *
	 * @param group the value group to recalculate
	 */
	protected abstract void recalculate(ValueGroup group);
	/**
	 * Registers the control to have its value saved and loaded.
	 *
	 * @param view the control to add to the list
	 */
	protected void registerAdjustable(final ValueControl view) {
		final String group = view.getGroup();
		if (group != null) {
			// If the item has a group
			ValueGroup items = groups.get(group);
			if (items == null) {
				// New group
				items = new ValueGroup(group);
				groups.put(group, items);
			}
			items.add(view);
		}
		// Add to all fields list (if no group, will still be saved/restored)
		fields.add(view);
	}
	/**
	 * Method for subclasses to override for the saving of standard Android objects not stored
	 * automatically by the savePrefs method (not Restorable).
	 *
	 * @param prefs the preference location to store the preferences
	 */
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
	}
	/**
	 * Saves to application settings the values of all fields registered with
	 * registerAdjustable.
	 */
	private void savePrefs() {
		final SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		// Save all items registered as Restorable
		for (ValueControl control : fields)
			control.saveState(editor);
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
		prefs.putBoolean(UIFunctions.getTag(view), view.isChecked());
	}
	/**
	 * Saves the state of a Spinner object in the preferences.
	 *
	 * @param prefs the preference location to store the preferences
	 * @param id the ID of the control to save
	 */
	protected void savePrefsSpinner(final SharedPreferences.Editor prefs, final int id) {
		final Spinner view = asSpinner(id);
		prefs.putInt(UIFunctions.getTag(view), view.getSelectedItemPosition());
	}
	/**
	 * Sets the listener and parent activity of the specified value entry box. Useful for the
	 * vast majority of activities.
	 *
	 * @param id the entry box to configure
	 */
	protected void setupValueEntryBox(final int id) {
		final AbstractEntryBox<?> box = ((AbstractEntryBox<?>)findViewById(id));
		box.setOnCalculateListener(this);
		registerAdjustable(box);
	}
	/**
	 * Called when a control in a particular group is updated.
	 *
	 * @param group the value group to perform the update
	 */
	protected abstract void update(ValueGroup group);
}
