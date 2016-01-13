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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * A mismash of code from http://stackoverflow.com/questions/9451917/double-fragment-rotating-android-with-actionbar
 * with parameterization to deal with a few casting issues.
 *
 * @param <T> the type of the tab which is instantiated
 */
public class FragmentTabListener<T extends Fragment> implements ActionBar.TabListener {
	private Fragment mFragment;
	private final FragmentActivity mActivity;
	private final String mTag;
	private final Class<T> mClass;
	private final int mfragmentContainerId;
	private final Bundle mfragmentArgs;

	// This version defaults to replacing the entire activity content area
	// new FragmentTabListener<SomeFragment>(this, "first", SomeFragment.class))
	public FragmentTabListener(FragmentActivity activity, String tag, Class<T> clz) {
		mActivity = activity;
		mTag = tag;
		mClass = clz;
		mfragmentContainerId = android.R.id.content;
		mfragmentArgs = new Bundle();
	}

	public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
		final FragmentManager manager = mActivity.getSupportFragmentManager();
		final FragmentTransaction sft = manager.beginTransaction();
		final Fragment visible = manager.findFragmentByTag(mTag);
		// Check if the fragment is already initialized
		if (visible == null) {
			// If not, instantiate and add it to the activity
			if (mFragment == null)
				mFragment = Fragment.instantiate(mActivity, mClass.getName(), mfragmentArgs);
			sft.add(mfragmentContainerId, mFragment, mTag);
		} else {
			// If it exists, simply attach it in order to show it
			sft.attach(visible);
		}
		sft.commit();
	}

	public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
		final FragmentManager manager = mActivity.getSupportFragmentManager();
		final FragmentTransaction sft = manager.beginTransaction();
		final Fragment visible = manager.findFragmentByTag(mTag);
		if (visible != null)
			// Detach the visible instance
			sft.detach(visible);
		else if (mFragment != null)
			// Detach the fragment, because another one is being attached
			sft.detach(mFragment);
		sft.commit();
	}

	public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
		// User selected the already selected tab. Usually do nothing.
	}
}
