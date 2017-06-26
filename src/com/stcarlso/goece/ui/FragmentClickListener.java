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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

/**
 * Listens for a button click, then launches a preset fragment.
 */
public class FragmentClickListener implements View.OnClickListener {
	private final Class<? extends Fragment> destFragment;
	private final FragmentActivity parentActivity;

	public FragmentClickListener(final FragmentActivity parentActivity,
	                             final Class<? extends Fragment> destFragment) {
		this.destFragment = destFragment;
		this.parentActivity = parentActivity;
	}
	@Override
	public void onClick(View v) {
		final FragmentTransaction transaction = parentActivity.getSupportFragmentManager().
			beginTransaction();
		final Fragment target = Fragment.instantiate(parentActivity, destFragment.getName(),
			null);
		// Replace the entire content area with the target fragment
		transaction.add(android.R.id.content, target, target.getClass().getSimpleName());
		// Allow user to go back
		transaction.addToBackStack(null);
		transaction.commit();
		// Display home as up since we are now in a child fragment
		final ActionBar bar = parentActivity.getActionBar();
		if (bar != null) {
			bar.setDisplayHomeAsUpEnabled(true);
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
	}
}
