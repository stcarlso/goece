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

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import java.io.Serializable;

/**
 * Boilerplate code that allows our custom components to save state across screen rotations.
 */
public class ECESavedState<T extends Serializable> extends View.BaseSavedState {
	/**
	 * The saved state
	 */
	private T state;

	private ECESavedState(final Parcel source) {
		// Create from an object
		super(source);
		state = (T)source.readSerializable();
	}
	/**
	 * Create a new saved state with no value.
	 *
	 * @param superState the parent state
	 */
	public ECESavedState(final Parcelable superState) {
		this(superState, null);
	}
	/**
	 * Create a new saved state with an initial value.
	 *
	 * @param superState the parent state
	 * @param value the state to store
	 */
	public ECESavedState(final Parcelable superState, T value) {
		super(superState);
		setValue(value);
	}
	/**
	 * Retrieves the state from this object.
	 *
	 * @return the stored state, or null if none has been set
	 */
	public T getValue() {
		return state;
	}
	/**
	 * Stores the state in this object.
	 *
	 * @param value the state to store
	 */
	public void setValue(T value) {
		state = value;
	}
	// Write the parcel, then save our state
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeSerializable(state);
	}

	/**
	 * Boilerplate required field to generate saved states.
	 */
	public static final Parcelable.Creator<ECESavedState> CREATOR =
		new Parcelable.Creator<ECESavedState>() {
			public ECESavedState createFromParcel(Parcel source) {
				return new ECESavedState(source);
			}
			public ECESavedState[] newArray(int size) {
				return new ECESavedState[size];
			}
		};
}
