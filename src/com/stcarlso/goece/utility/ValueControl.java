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

package com.stcarlso.goece.utility;

import android.content.SharedPreferences;

/**
 * A simple marker interface common to custom controls which allows their state to be saved
 * and restored to the application preferences, along with group management.
 */
public interface ValueControl {
	/**
	 * Gets the group affected by this control.
	 *
	 * @return the group which should be updated when this control is changed
	 */
	String getAffects();
	/**
	 * Gets the group of this value box.
	 *
	 * @return the least-recently-used group of related values (inputs, outputs)
	 */
	String getGroup();
	/**
	 * Standard Android call. Used for type safety...
	 *
	 * @return the ID of this component
	 */
	int getId();
	/**
	 * Returns true if this control is enabled, or false otherwise.
	 *
	 * @return whether the control is able to accept input
	 */
	boolean isEnabled();
	/**
	 * Loads the state of this control. If no state is available, nothing should happen. The
	 * same ID as in saveState() must be used.
	 *
	 * @param prefs the location of the state to restore
	 */
	void loadState(SharedPreferences prefs);
	/**
	 * Save the state of this control. It should be saved with a unique ID, preferably the
	 * integer ID of the field.
	 *
	 * @param prefs the location where the state will be stored
	 */
	void saveState(SharedPreferences.Editor prefs);
}
