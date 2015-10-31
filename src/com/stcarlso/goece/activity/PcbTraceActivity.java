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

package com.stcarlso.goece.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.AbstractEntryBox;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ValueBoxContainer;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.Equation;
import com.stcarlso.goece.utility.EquationSolver;
import com.stcarlso.goece.utility.Units;

/**
 * An activity for PCB trace impedance calculation.
 */
public class PcbTraceActivity extends ChildActivity implements
		AdapterView.OnItemSelectedListener {
	/**
	 * Minimum width that the solver will attempt in mm.
	 */
	public static final double MIN_SOLVE_WIDTH = 0.01;
	/**
	 * Maximum width that the solver will attempt in mm.
	 */
	public static final double MAX_SOLVE_WIDTH = 100.0;

	/**
	 * Contains all data entry controls.
	 */
	private final ValueBoxContainer controls;
	/**
	 * Cached reference to the trace type selector control.
	 */
	private Spinner traceTypeCtrl;

	public PcbTraceActivity() {
		controls = new ValueBoxContainer();
	}
	protected void loadCustomPrefs(SharedPreferences prefs) {
		loadPrefsSpinner(prefs, R.id.guiPcbScenario);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pcbtrace);
		traceTypeCtrl = (Spinner)findViewById(R.id.guiPcbScenario);
		traceTypeCtrl.setOnItemSelectedListener(this);
		// Register value entry boxes
		controls.add(findViewById(R.id.guiPcbDielectric));
		controls.add(findViewById(R.id.guiPcbImpedance1));
		controls.add(findViewById(R.id.guiPcbImpedance2));
		controls.add(findViewById(R.id.guiPcbThickness));
		controls.add(findViewById(R.id.guiPcbTraceHeight));
		controls.add(findViewById(R.id.guiPcbTraceSpace));
		controls.add(findViewById(R.id.guiPcbTraceWidth));
		controls.setupAll(this);
		loadPrefs();
		recalculate(controls.get(R.id.guiPcbThickness));
	}
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Show/hide diff impedance based on selection
		final int scenario = traceTypeCtrl.getSelectedItemPosition(), show = (scenario > 1) ?
			View.VISIBLE : View.GONE;
		controls.get(R.id.guiPcbImpedance2).setVisibility(show);
		controls.get(R.id.guiPcbTraceSpace).setVisibility(show);
		recalculate(controls.get(R.id.guiPcbThickness));
	}
	public void onNothingSelected(AdapterView<?> parent) { }
	protected void recalculate(ValueGroup group) {
		final int scenario = traceTypeCtrl.getSelectedItemPosition();
		double value;
		final Equation equ;
		// Shared variables
		final double er = controls.getRawValue(R.id.guiPcbDielectric);
		final double h = controls.getRawValue(R.id.guiPcbThickness);
		final double t = controls.getRawValue(R.id.guiPcbTraceHeight);
		final AbstractEntryBox<?> w = controls.get(R.id.guiPcbTraceWidth);
		final AbstractEntryBox<?> z1 = controls.get(R.id.guiPcbImpedance1);
		switch (group.leastRecentlyUsed()) {
		case R.id.guiPcbImpedance1:
			// Impedance single-ended
			try {
				if (scenario == 0 || scenario == 2)
					equ = new MicrostripEquation(h, t, er, 0.0);
				else
					equ = new StriplineEquation(h, t, er, 0.0);
				value = equ.eval(w.getRawValue());
				// Update box
				if (Double.isNaN(value) || value <= 0.0)
					z1.setError(getString(R.string.guiPcbNoSolution));
				else
					z1.updateValue(value);
			} catch (IllegalArgumentException e) {
				// Bad inputs
				z1.setError(getString(R.string.guiPcbBadInput));
			}
			break;
		case R.id.guiPcbTraceWidth:
			// Trace width
			try {
				if (scenario == 0 || scenario == 2)
					equ = new MicrostripEquation(h, t, er, z1.getRawValue());
				else
					equ = new StriplineEquation(h, t, er, z1.getRawValue());
				value = new EquationSolver(equ).solve(MIN_SOLVE_WIDTH, MAX_SOLVE_WIDTH);
				// Update box
				if (Double.isNaN(value) || value <= 0.0)
					w.setError(getString(R.string.guiPcbNoSolution));
				else
					w.updateValue(value);
			} catch (IllegalArgumentException e) {
				// Bad inputs
				w.setError(getString(R.string.guiPcbBadInput));
			}
			break;
		default:
			// Invalid
			break;
		}
	}
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		savePrefsSpinner(prefs, R.id.guiPcbScenario);
	}
	protected void update(ValueGroup group) { }

	/**
	 * A class for EquationSolver to handle reverse solving of microstrip impedance.
	 *
	 * Trace width is the IV (x). All units in mm.
	 */
	private static class MicrostripEquation implements Equation {
		/**
		 * The desired impedance value for solving.
		 */
		private final double desired;
		/**
		 * The dielectric constant of the material.
		 */
		private final double er;
		/**
		 * The height of the trace above ground (PCB thickness).
		 */
		private final double h;
		/**
		 * The trace thickness.
		 */
		private final double t;
		/**
		 * Coefficients for effective width calculation.
		 */
		private transient final double weAdd;
		private transient final double weMul;
		/**
		 * Coefficients for impedance calculation.
		 */
		private transient final double zDenom;
		private transient final double zX2Add;

		/**
		 * Creates a new microstrip equation context for calculating single ended impedance.
		 *
		 * @param h the PCB thickness
		 * @param t the trace thickness
		 * @param er the relative dielectric permittivity
		 * @param desired the desired impedance (set to 0.0 for forward calculation)
		 */
		public MicrostripEquation(final double h, final double t, final double er,
								  final double desired) {
			if (er <= 1.0)
				throw new IllegalArgumentException("dielectric <= 1");
			if (t <= 0.0)
				throw new IllegalArgumentException("thickness <= 0");
			if (h <= 0.0)
				throw new IllegalArgumentException("height <= 0");
			if (desired < 0.0)
				throw new IllegalArgumentException("desired < 0");
			this.desired = desired;
			this.er = er;
			this.h = h;
			this.t = t;
			// Precompute
			weMul = t * (1.0 + 1.0 / er) * 0.5 * Units.PI_INV;
			weAdd = weMul * 1.6020599913279624;
			zDenom = Math.sqrt(1.0 + er) * 2.8284271247461902;
			zX2Add = (1.0 + 1.0 / er) * 0.5 * Math.PI * Math.PI;
		}
		public double eval(double x) {
			final double ret;
			if (x <= 0.0)
				ret = Double.NaN;
			else {
				// Calculate w_eff
				final double w_coeff = Units.PI_INV / (1.1 + x / t);
				final double w_eff_4h = 4.0 * h / (x + weAdd - weMul * 0.5 * Math.log(
					(t * t) / (h * h) + w_coeff * w_coeff));
				final double x1 = (14.0 + 8.0 / er) * w_eff_4h / 11.0;
				ret = Units.Z_0 * Math.log(1.0 + w_eff_4h * (x1 + Math.sqrt(x1 * x1 +
					zX2Add))) / zDenom - desired;
			}
			return ret;
		}
	}

	/**
	 * A class for EquationSolver to handle reverse solving of symmetric stripline impedance.
	 *
	 * Trace width is the IV (x). All units in mm.
	 */
	private static class StriplineEquation implements Equation {
		/**
		 * The desired impedance value for solving.
		 */
		private final double desired;
		/**
		 * The dielectric constant of the material.
		 */
		private final double er;
		/**
		 * The height of the trace above ground (PCB thickness).
		 */
		private final double h;
		/**
		 * The trace thickness.
		 */
		private final double t;
		/**
		 * Coefficients for effective width calculation.
		 */
		/*private transient final double weMul;
		private transient final double weT4h;
		private transient final double m;*/
		/**
		 * Coefficients for impedance calculation.
		 */
		private transient final double zNum;

		/**
		 * Creates a new stripline equation context for calculating single ended impedance.
		 *
		 * @param h the PCB thickness
		 * @param t the trace thickness
		 * @param er the relative dielectric permittivity
		 * @param desired the desired impedance (set to 0.0 for forward calculation)
		 */
		public StriplineEquation(final double h, final double t, final double er,
								 final double desired) {
			if (er <= 1.0)
				throw new IllegalArgumentException("dielectric <= 1");
			if (t <= 0.0)
				throw new IllegalArgumentException("thickness <= 0");
			if (h <= 0.0)
				throw new IllegalArgumentException("height <= 0");
			if (desired < 0.0)
				throw new IllegalArgumentException("desired < 0");
			this.desired = desired;
			this.er = er;
			this.h = h;
			this.t = t;
			// Precompute
			/*
			m = 6.0 * h / (3.0 * h + t);
			weMul = t * Units.PI_INV;
			weT4h = t / (4.0 * h + t);
			zNum = 0.5 * Units.Z_0 / Math.sqrt(er);
			*/
			zNum = 60.0 / Math.sqrt(er);
		}
		public double eval(double x) {
			final double ret;
			if (x <= 0.0)
				ret = Double.NaN;
			else {
				// Calculate w_eff
				/*
				final double w_eff_8h = 8.0 * h * Units.PI_INV / (x + weMul - weMul * 0.5 *
					Math.log(weT4h * weT4h + Math.pow(Math.PI * t / (4.0 * x + 4.4 * t), m)));
				final double zin = 2.0 * w_eff_8h + Math.sqrt(4.0 * w_eff_8h * w_eff_8h + 6.27);
				ret = zNum * Math.log(1.0 + w_eff_8h * zin) - desired;
				*/
				final double d = Math.PI * 0.5 * x * (1.0 + t * Units.PI_INV * (1.0 +
					Math.log(4.0 * Math.PI * x / t)) / x + 0.551 * t * t / (x * x));
				ret = Math.max(0.0, zNum * Math.log((8.0 * h + 4.0 * t) / d)) - desired;
			}
			return ret;
		}
	}
}