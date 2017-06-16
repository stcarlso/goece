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

import android.util.SparseArray;
import com.stcarlso.goece.utility.ValueControl;

import java.util.*;

/**
 * Represents a group (declared in the UI) of value controls. Also implements iteration over the
 * controls in the group for use with for-each loops.
 */
public class ValueGroup implements Iterable<ValueControl> {
	/**
	 * Maps control IDs to controls.
	 */
	private SparseArray<ValueControl> map;
	/**
	 * Most-recently used list of controls.
	 */
	private LinkedList<Integer> mru;
	/**
	 * The group name.
	 */
	private final String name;

	/**
	 * Creates an empty value group.
	 *
	 * @param name the group name
	 */
	public ValueGroup(final String name) {
		if (name == null)
			throw new NullPointerException("name");
		map = new SparseArray<ValueControl>(32);
		mru = new LinkedList<Integer>();
		this.name = name;
	}
	/**
	 * Adds a control to this group.
	 *
	 * @param control the value control to add
	 */
	public void add(final ValueControl control) {
		final int id = control.getId();
		map.put(id, control);
		mru.add(id);
	}
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueGroup controls = (ValueGroup)o;
		return getName().equals(controls.getName());
	}
	/**
	 * Gets a reference to the control with the specified ID.
	 *
	 * @param id the ID of the control
	 * @return the matching control, or null if no matching control is found in this group
	 */
	public ValueControl get(final int id) {
		return map.get(id);
	}
	/**
	 * Gets the name of this group.
	 *
	 * @return the group name
	 */
	public String getName() {
		return name;
	}
	public int hashCode() {
		return getName().hashCode();
	}
	/**
	 * Returns the control which was least recently used.
	 *
	 * @return the ID of the control with the oldest data
	 */
	public int leastRecentlyUsed() {
		return searchEnabledControl(mru.descendingIterator(), mru.getLast());
	}
	/**
	 * Returns the control which was most recently used.
	 *
	 * @return the ID of the control with the newest data
	 */
	public int mostRecentlyUsed() {
		return searchEnabledControl(mru.iterator(), mru.getFirst());
	}
	@Override
	public Iterator<ValueControl> iterator() {
		return new Iterator<ValueControl>() {
			private int index;
			private final int count = map.size();

			@Override
			public boolean hasNext() {
				return index < count;
			}
			@Override
			public ValueControl next() {
				if (index >= count)
					throw new NoSuchElementException("At end of list");
				return map.valueAt(index++);
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Read-only iterator");
			}
		};
	}
	/**
	 * Searches for an enabled control in the iterable elements
	 *
	 * @param it the list of controls to check
	 * @param lastResort the ID to use if no matches are found
	 * @return the ID of the first enabled control in the list
	 */
	protected int searchEnabledControl(final Iterator<Integer> it, final int lastResort) {
		int ctrl = lastResort;
		while (it.hasNext()) {
			final int id = it.next();
			if (get(id).isEnabled()) {
				ctrl = id;
				break;
			}
		}
		return ctrl;
	}
	/**
	 * Marks a control as used, updating the most-recently-used list.
	 *
	 * @param id the ID of the changed control
	 * @return the control which was changed longest ago (oldest data)
	 */
	public ValueControl use(final int id) {
		ValueControl control = null;
		// Iterate through and pull it to the front
		final Iterator<Integer> lookup = mru.iterator();
		while (control == null && lookup.hasNext()) {
			final int newID = lookup.next();
			if (id == newID) {
				// Found it, move it to most recent
				lookup.remove();
				mru.addFirst(id);
				control = get(mru.getLast());
			}
		}
		return control;
	}
	public String toString() {
		return getName();
	}
}
