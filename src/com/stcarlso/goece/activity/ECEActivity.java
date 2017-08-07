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

package com.stcarlso.goece.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.FragmentTabListener;

/**
 * Make it your pastime, make it your mission!
 */
@SuppressWarnings("deprecation")
public final class ECEActivity extends FragmentActivity {
	/**
	 * Adds a tab to the menu. ActionBar tabs are deprecated but required on the old versions of
	 * Android this app still can target.
	 *
	 * @param resId the string resource ID of the tab name
	 */
	private <T extends Fragment> ActionBar.Tab addTab(final int resId, Class<T> target) {
		final ActionBar tabBar = getActionBar();
		final ActionBar.Tab newTab;
		if (tabBar != null) {
			// Create a tab
			newTab = tabBar.newTab();
			newTab.setText(resId);
			newTab.setTabListener(new FragmentTabListener<T>(this, target.getSimpleName(),
				target));
			tabBar.addTab(newTab);
		} else
			newTab = null;
		return newTab;
	}
	/**
	 * Closes the currently visible fragment.
	 *
	 * @return true if a fragment was closed in this way, or false otherwise
	 */
	private boolean closeFragment() {
		boolean closed = false;
		final FragmentManager manager = getSupportFragmentManager();
		final int count = manager.getBackStackEntryCount();
		// Pop fragment back stack if available
		if (count > 0) {
			final ActionBar bar = getActionBar();
			manager.popBackStack();
			if (count < 2 && bar != null) {
				// Back to main activity, clear display as-up
				bar.setDisplayHomeAsUpEnabled(false);
				bar.setDisplayShowHomeEnabled(false);
				bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			}
			closed = true;
		}
		return closed;
	}
	@Override
	public void onBackPressed() {
		// Pop fragment back stack if available, otherwise kill off activity
		if (!closeFragment())
			super.onBackPressed();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean done = false;
		switch (item.getItemId()) {
		case android.R.id.home:
			// Act as if BACK was pressed and pop the stack if available
			done = closeFragment();
			break;
		default:
			break;
		}
		return done || super.onOptionsItemSelected(item);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		final FragmentManager manager = getSupportFragmentManager();
		super.onConfigurationChanged(newConfig);
		final Fragment current = manager.findFragmentByTag("content");
		if (current != null) {
			// Detach and re-attach the fragment to re-layout any landscape views
			final FragmentTransaction transaction = manager.beginTransaction();
			transaction.detach(current);
			transaction.attach(current);
			// Do not allow user to go back, this transaction has no meaning for precedence
			transaction.commit();
		}
	}
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar tabBar = getActionBar();
		if (tabBar != null) {
			tabBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			// Analog, digital, circuits, pinouts, resources tabs
			final ActionBar.Tab analogTab = addTab(R.string.guiTabAnalog, AnalogFragment.class);
			addTab(R.string.guiTabDigital, DigitalFragment.class);
			addTab(R.string.guiTabCircuits, ICFragment.class);
		}
		restoreTab(savedInstanceState);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		restoreTab(savedInstanceState);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save where the user was
		final ActionBar tabBar = getActionBar();
		if (tabBar != null && tabBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS)
			outState.putInt("tab", tabBar.getSelectedTab().getPosition());
	}
	// Restores the tab where the user last was found
	private void restoreTab(final Bundle savedInstanceState) {
		final ActionBar tabBar = getActionBar();
		if (tabBar != null && tabBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
			// Only if the user has been here before, otherwise pick tab 0
			if (savedInstanceState == null || !savedInstanceState.containsKey("tab"))
				tabBar.setSelectedNavigationItem(0);
			else
				tabBar.setSelectedNavigationItem(savedInstanceState.getInt("tab"));
		}
	}
}
