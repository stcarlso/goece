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

package com.stcarlso.goece.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ChildFragment;
import com.stcarlso.goece.ui.PinoutView;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.Pinout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An activity which displays IC pinouts as configured in the IC properties list. This is meant
 * to replace a large number of reference pages in ElectroDroid and related applications.
 */
public class PinoutFragment extends ChildFragment implements AdapterView.OnItemClickListener {
	/**
	 * Reference to the IC input box.
	 */
	private AutoCompleteTextView icList;
	/**
	 * Maps configured pinout IC names (not identifiers!) to pinouts.
	 */
	private final Map<String, Pinout> pinouts;
	/**
	 * Reference to the view containing the active pinout.
	 */
	private PinoutView pinoutView;

	public PinoutFragment() {
		pinouts = new TreeMap<String, Pinout>();
	}
	@Override
	protected String getTitle(Context parent) {
		return parent.getString(R.string.guiPinout);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.pinout, container, false);
		icList = (AutoCompleteTextView)view.findViewById(R.id.guiPinoutIC);
		pinoutView = (PinoutView)view.findViewById(R.id.guiPinoutView);
		// Create adapter with current pinout information, duping the map burns memory but
		// avoids having to reinvent ArrayAdapter
		final Pinout sf = new Pinout("7400", "7400 Quad 2-input NAND Gate");
		pinouts.put(sf.getName(), sf);
		final List<Pinout> items = new ArrayList<Pinout>(pinouts.values());
		icList.setAdapter(new ArrayAdapter<Pinout>(getActivity(), android.R.layout.
			simple_list_item_1, items));
		icList.setOnItemClickListener(this);
		// TEST
		sf.setShape(Pinout.SHAPE_QUAD);
		sf.addPin("A1");
		sf.addPin("B1");
		sf.addPin("Y1");
		sf.addPin("A2");
		sf.addPin("B2");
		sf.addPin("Y2");
		sf.addPin("GND");
		sf.addPin("Y3");
		sf.addPin("A3");
		sf.addPin("B3");
		sf.addPin("Y4");
		sf.addPin("A4");
		sf.addPin("B4");
		sf.addPin("VCC");
		sf.addPin("ABC");
		sf.addPin("DEF");
		icList.requestFocus();
		return view;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		// Triggered when an item is clicked from AutoCompleteTextView
		// Technically we could use TextWatcher and show the chart the moment it matches, but
		// that could lead to churn if many short IC names are very similar
		if (index >= 0 && index < pinouts.size()) {
			final Pinout pinout = (Pinout) parent.getAdapter().getItem(index);
			if (pinout != null)
				pinoutView.setPinout(pinout);
		}
	}
	@Override
	protected void recalculate(ValueGroup group) {
		// The pinouts view does no calculations
	}
	@Override
	protected void update(ValueGroup group) {
		// The pinouts view does no calculations
	}
}