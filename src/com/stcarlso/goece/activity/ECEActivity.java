/**
 * ********************************************************************************************
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
 * ********************************************************************************************
 */

package com.stcarlso.goece.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.FragmentTabListener;

/**
 * Make it your pastime, make it your mission!
 */
public final class ECEActivity extends FragmentActivity {
	/**
	 * Adds a tab to the menu.
	 *
	 * @param resId the string resource ID of the tab name
	 */
	private <T extends Fragment> ActionBar.Tab addTab(final int resId, Class<T> target) {
		final ActionBar tabBar = getActionBar();
		// Uh?
		assert (tabBar != null);
		// Create a tab
		final ActionBar.Tab newTab = tabBar.newTab();
		newTab.setText(resId);
		newTab.setTabListener(new FragmentTabListener<T>(this, target.getSimpleName(), target));
		tabBar.addTab(newTab);
		return newTab;
	}
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar tabBar = getActionBar();
		// Uh?
		assert (tabBar != null);
		tabBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Analog, digital, circuits, pinouts, resources tabs
		final ActionBar.Tab analogTab = addTab(R.string.guiTabAnalog, AnalogFragment.class);
		addTab(R.string.guiTabDigital, DigitalFragment.class);
		if (savedInstanceState == null || !savedInstanceState.containsKey("tab"))
			tabBar.selectTab(analogTab);
		else
			tabBar.setSelectedNavigationItem(savedInstanceState.getInt("tab"));
	}
	protected void onSaveInstanceState(Bundle outState) {
		// Save where the user was
		final ActionBar tabBar = getActionBar();
		assert (tabBar != null);
		outState.putInt("tab", tabBar.getSelectedTab().getPosition());
	}
}
