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

/**
 * An activity parent which handles the up action gracefully and fixes the gross problem with
 * value entry boxes needing a parent.
 */
public abstract class ChildActivity extends Activity implements Calculatable {
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
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// Launched from another application, create a new task
				TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).
					startActivities();
			} else
				// No need to create a new back stack
				NavUtils.navigateUpTo(this, upIntent);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * Sets the listener and parent activity of the specified value entry box. Useful for the
	 * vast majority of activities.
	 *
	 * @param id the entry box to configure
	 */
	public void setupValueEntryBox(final int id) {
		final ValueEntryBox box = ((ValueEntryBox)findViewById(id));
		box.setParentActivity(this);
		box.setOnCalculateListener(this);
	}
}
