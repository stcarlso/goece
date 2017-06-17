/***********************************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Stephen Carlson
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

import android.content.Context;

/**
 * A marker interface shared by classes which can act as value sources for CopyPasteListener.
 */
public interface CopyValueSource {
	/**
	 * Reports the context of this object. Used to find the parent Activity from
	 * CopyPasteListener, this method is already implemented in all View subclasses.
	 *
	 * @return the context of this object
	 */
	Context getContext();
	/**
	 * Returns a description of the object, used to title the dialog box.
	 *
	 * @return a description of the value to be copied/pasted
	 */
	String getDescription();
	/**
	 * Returns true if this value can be edited (whether updateValueUser will do anything).
	 *
	 * @return true if setValue can be used
	 */
	boolean isEditable();
	/**
	 * Changes the value of this object to the specified (pasted) value. This method is named
	 * to avoid clashing with AbstractEntryBox.updateValue which does not call on-calculate
	 * listeners.
	 *
	 * @param newValue the pasted value to set
	 */
	void updateValueUser(double newValue);
}
