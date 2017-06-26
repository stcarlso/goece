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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.AbstractEntryBox;
import com.stcarlso.goece.ui.ChildFragment;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.utility.ECECalc;
import com.stcarlso.goece.utility.Equation;
import com.stcarlso.goece.utility.EquationSolver;
import com.stcarlso.goece.utility.Units;

/**
 * An activity for PCB trace impedance calculation.
 */
public class PcbTraceFragment extends ChildFragment implements
		AdapterView.OnItemSelectedListener {
	/**
	 * Images for the user assist, indexed by the combo box option.
	 */
	private static final int[] IMAGES = new int[] {
		R.drawable.microstrip1,
		R.drawable.stripline1,
		R.drawable.microstrip2,
		R.drawable.stripline2
	};
	/**
	 * Minimum width that the solver will attempt in mm.
	 */
	private static final double MIN_SOLVE_WIDTH = 0.01;
	/**
	 * Maximum width that the solver will attempt in mm.
	 */
	private static final double MAX_SOLVE_WIDTH = 100.0;

	/**
	 * Cached reference to the user assist image of the current impedance scenario.
	 */
	private ImageView pcbImage;
	/**
	 * Cached reference to the trace type selector control.
	 */
	private Spinner traceTypeCtrl;

	/**
	 * Solves the equation and puts the result into the specified output box.
	 *
	 * @param equ the equation to solve, the bounds will be MIN_SOLVE_WIDTH to MAX_SOLVE_WIDTH
	 * @param out the location where the answer will be stored, or where the red error will be
	 * displayed if no solution can be found
	 */
	private void backwardSolve(final Equation equ, final AbstractEntryBox<?> out) {
		if (equ == null)
			// Invalid inputs
			out.setError(getString(R.string.guiPcbBadInput));
		else {
			final double value = new EquationSolver(equ).solve(MIN_SOLVE_WIDTH, MAX_SOLVE_WIDTH);
			// Update output on screen
			if (Double.isNaN(value) || value <= 0.0)
				out.setError(getString(R.string.guiPcbNoSolution));
			else {
				out.updateValue(value);
				out.setError(null);
			}
		}
	}
	/**
	 * Evaluates the equation at the given value and puts the result into the specified output
	 * box.
	 *
	 * @param equ the equation to evaluate
	 * @param iv the value at which to evaluate equ
	 * @param out the location where the answer will be stored, or where the red error will be
	 * displayed if no solution can be found
	 */
	private void forwardEval(final Equation equ, final AbstractEntryBox<?> out,
							 final double iv) {
		if (equ == null)
			// Invalid inputs
			out.setError(getString(R.string.guiPcbBadInput));
		else {
			final double value = equ.eval(iv);
			// Update output on screen
			if (Double.isNaN(value) || value <= 0.0)
				out.setError(getString(R.string.guiPcbNoSolution));
			else {
				out.updateValue(value);
				out.setError(null);
			}
		}
	}
	@Override
	protected void loadCustomPrefs(SharedPreferences prefs) {
		loadPrefsSpinner(prefs, R.id.guiPcbScenario);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadPrefs();
		recalculate(controls.get(R.id.guiPcbThickness));
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.pcbtrace, container, false);
		pcbImage = asImageView(view, R.id.guiPcbImage);
		traceTypeCtrl = asSpinner(view, R.id.guiPcbScenario);
		traceTypeCtrl.setOnItemSelectedListener(this);
		// Register value entry boxes
		controls.add(view, R.id.guiPcbDielectric, R.id.guiPcbImpedance1, R.id.guiPcbImpedance2,
			R.id.guiPcbThickness, R.id.guiPcbTraceHeight, R.id.guiPcbTraceSpace,
			R.id.guiPcbTraceWidth);
		controls.setupAll(this);
		return view;
	}
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Show/hide diff impedance based on selection
		final int scenario = traceTypeCtrl.getSelectedItemPosition();
		final boolean show = scenario > 1;
		controls.get(R.id.guiPcbImpedance2).setVisibility(show ? View.VISIBLE : View.GONE);
		controls.get(R.id.guiPcbTraceSpace).setEnabled(show);
		if (scenario >= 0 && scenario < IMAGES.length)
			// Update the image
			pcbImage.setImageResource(IMAGES[scenario]);
		recalculate(controls.get(R.id.guiPcbThickness));
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) { }
	@Override
	protected void recalculate(ValueGroup group) {
		final int scenario = traceTypeCtrl.getSelectedItemPosition();
		Equation equ = null;
		// Shared variables
		final double er = controls.getRawValue(R.id.guiPcbDielectric);
		final double h = controls.getRawValue(R.id.guiPcbThickness);
		final double t = controls.getRawValue(R.id.guiPcbTraceHeight);
		final AbstractEntryBox<?> s = controls.get(R.id.guiPcbTraceSpace);
		final AbstractEntryBox<?> w = controls.get(R.id.guiPcbTraceWidth);
		final AbstractEntryBox<?> z1 = controls.get(R.id.guiPcbImpedance1);
		final AbstractEntryBox<?> z2 = controls.get(R.id.guiPcbImpedance2);
		final double wv = w.getRawValue();
		switch (group.leastRecentlyUsed()) {
		case R.id.guiPcbImpedance1:
			// Impedance single-ended
			try {
				if (scenario == 0 || scenario == 2)
					equ = new MicrostripEquation(h, t, er, 0.0);
				else
					equ = new StriplineEquation(h, t, er, 0.0);
			} catch (IllegalArgumentException ignore) { }
			forwardEval(equ, z1, w.getRawValue());
			break;
		case R.id.guiPcbImpedance2:
			// Impedance differential
			try {
				if (scenario == 2)
					equ = new DiffMicrostripEquation(h, wv, t, er, 0.0);
				else
					equ = new DiffStriplineEquation(h, wv, t, er, 0.0);
			} catch (IllegalArgumentException ignore) { }
			forwardEval(equ, z2, s.getRawValue());
			break;
		case R.id.guiPcbTraceWidth:
			// Trace width
			try {
				if (scenario == 0 || scenario == 2)
					equ = new MicrostripEquation(h, t, er, z1.getRawValue());
				else
					equ = new StriplineEquation(h, t, er, z1.getRawValue());
			} catch (IllegalArgumentException ignore) { }
			backwardSolve(equ, w);
			break;
		case R.id.guiPcbTraceSpace:
			// Trace spacing
			try {
				if (scenario == 2)
					equ = new DiffMicrostripEquation(h, wv, t, er, z2.getRawValue());
				else
					equ = new DiffStriplineEquation(h, wv, t, er, z2.getRawValue());
			} catch (IllegalArgumentException ignore) { }
			backwardSolve(equ, s);
			break;
		default:
			// Invalid
			break;
		}
	}
	@Override
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		savePrefsSpinner(prefs, R.id.guiPcbScenario);
	}
	@Override
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
		@Override
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
		 * The PCB thickness from the embedded trace to the nearest plane.
		 */
		private final double h;
		/**
		 * The trace thickness.
		 */
		private final double t;
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
			zNum = 60.0 / Math.sqrt(er);
		}
		@Override
		public double eval(double x) {
			final double ret;
			if (x <= 0.0)
				ret = Double.NaN;
			else {
				// Calculate w_eff
				final double d = Math.PI * 0.5 * x * (1.0 + t * Units.PI_INV * (1.0 +
					Math.log(4.0 * Math.PI * x / t)) / x + 0.551 * t * t / (x * x));
				ret = Math.max(0.0, zNum * Math.log((8.0 * h + 4.0 * t) / d)) - desired;
			}
			return ret;
		}
	}

	/**
	 * A class for EquationSolver to handle reverse solving of microstrip differential
	 * impedance.
	 *
	 * Trace spacing is the IV (x). All units in mm.
	 */
	private static class DiffMicrostripEquation implements Equation {
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
		 * The trace width.
		 */
		private final double w;
		/**
		 * Coefficients for impedance calculation.
		 */
		private transient final double a0;
		private transient final double b0;
		private transient final double c0;
		private transient final double d0;
		private transient final double er_eff;
		private transient final double q1;
		private transient final double zo_surf;
		private transient final double zom;

		/**
		 * Creates a new microstrip equation context for calculating differential impedance.
		 *
		 * @param h the PCB thickness
		 * @param w the trace width
		 * @param t the trace thickness
		 * @param er the relative dielectric permittivity
		 * @param desired the desired impedance (set to 0.0 for forward calculation)
		 */
		public DiffMicrostripEquation(final double h, final double w, final double t,
									  final double er, final double desired) {
			if (er <= 1.0)
				throw new IllegalArgumentException("dielectric <= 1");
			if (t <= 0.0)
				throw new IllegalArgumentException("thickness <= 0");
			if (h <= 0.0)
				throw new IllegalArgumentException("height <= 0");
			if (w <= 0.0)
				throw new IllegalArgumentException("width <= 0");
			if (desired < 0.0)
				throw new IllegalArgumentException("desired < 0");
			this.desired = desired;
			this.er = er;
			this.h = h;
			this.t = t;
			this.w = w;
			// Precompute
			// effective er
			double er_base = Math.sqrt(w / (w + 12.0 * h));
			if (w < h) {
				final double p = 1.0 - w / h;
				er_base += 0.04 * p * p;
			}
			er_eff = (er + 1.0) * 0.5 + (er - 1.0) * 0.5 * er_base;
			// a0 - d0
			final double u = w / h;
			// Formula page adds sqrt around the last term
			a0 = 0.7287 * (er_eff - 0.5 * (er + 1.0)) * (1.0 - Math.exp(-0.179 * u));
			b0 = (0.747 * er) / (0.15 + er);
			c0 = b0 - (b0 - .207) * Math.exp(-0.414 * u);
			d0 = 0.593 + 0.694 * Math.exp(-0.562 * u);
			// effective w
			final double p = t / (Math.PI * (w + 1.1 * t));
			final double er2 = (er_eff + 1.0) / (2.0 * er_eff);
			final double w_eff = w + (t / Math.PI) * (4.0 - 0.5 * Math.log(t * t / (h * h) +
				p * p)) * er2;
			// z0
			final double hw = 4.0 * h / w_eff, erMul = (14.0 * er_eff + 8.0) / (11.0 * er_eff);
			final double he = h * erMul / w_eff;
			zo_surf = Units.Z_0 * Math.log1p(hw * hw * erMul + Math.sqrt(16.0 * he * he + er2 *
				Math.PI * Math.PI) * hw) / (2.8284271247461902 * Math.sqrt(er_eff + 1.0));
			// q2-q10 are based on spacing
			q1 = 0.8695 * Math.pow(u, .194);
			zom = zo_surf * Math.sqrt(er_eff) / (Units.Z_0 * Math.PI);
		}
		@Override
		public double eval(double x) {
			final double ret;
			if (x <= 0.0)
				ret = Double.NaN;
			else {
				final double g = x / h, u = w / h, emg = Math.exp(-g), lg = Math.log(g);
				final double er_eff_o = (0.5 * er + 0.5 + a0 - er_eff) * Math.exp(-c0 *
					Math.pow(g, d0)) + er_eff;
				// Calculate q2 through q10 (why so hard?)
				// Formula page has 1.89 instead of 0.189
				final double q2 = 1.0 + 0.7519 * g + 0.189 * Math.pow(g, 2.31);
				final double uq3 = Math.pow(u, Math.pow(16.6 + Math.pow(8.4 / g, 6.0), -0.387) +
					0.004149377593360996 * (10.0 * lg - Math.log1p(Math.pow(g *
					0.294117647058823529, 10))) + 0.1975);
				final double q4 = 2.0 * q1 / (q2 * (emg * uq3 + (2.0 - emg) / uq3));
				// Hooray for log1p and log rules
				final double q5 = 1.794 + 1.14 * Math.log1p(0.638 / (g + 0.517 *
					Math.pow(g, 2.43)));
				final double q6 = 0.2305 + (10.0 * lg - Math.log1p(Math.pow(g *
					0.172413793103448276, 10.0))) * 0.003554923569143263 + Math.log1p(0.598 *
					Math.pow(g, 1.154)) * 0.19607843137254902;
				final double q7 = (10.0 + 190.0 * g * g) / (1.0 + 82.3 * g * g * g);
				// Consolidated q8 into q9
				final double q9 = Math.log(q7) * (Math.exp(-6.5 - 0.95 * lg - Math.pow(g *
					6.666666666666666667, 5.0)) + 0.060606060606060606);
				final double q10 = q4 - q5 * Math.pow(u, q6 * Math.pow(u, -q9)) / q2;
				// Calculate the final impedance... hope this works...
				ret = (zo_surf * 2.0 * Math.sqrt(er_eff / er_eff_o)) / (1.0 - zom * q10) -
					desired;
			}
			// XXX Rectify difference between altium and the web site (-10 ohms)
			return ret;
		}
	}

	/**
	 * A class for EquationSolver to handle reverse solving of stripline differential impedance.
	 *
	 * Trace spacing is the IV (x). All units in mm.
	 */
	private static class DiffStriplineEquation implements Equation {
		/**
		 * Used in impedance calculations.
		 */
		private static final double CF0 = 2.0 * Math.log(2.0);

		/**
		 * The desired impedance value for solving.
		 */
		private final double desired;
		/**
		 * The dielectric constant of the material.
		 */
		private final double er;
		/**
		 * The PCB thickness from the embedded trace to the nearest plane.
		 */
		private final double h;
		/**
		 * The trace thickness.
		 */
		private final double t;
		/**
		 * The trace width.
		 */
		private final double w;
		/**
		 * Coefficients for impedance calculation.
		 */
		private transient final double cfTB;
		private transient final double kmul;
		private transient final double z0i;
		private transient final double z0mul;
		private transient final double z0ss;

		/**
		 * Creates a new stripline equation context for calculating differential impedance.
		 *
		 * @param h the PCB thickness
		 * @param w the trace width
		 * @param t the trace thickness
		 * @param er the relative dielectric permittivity
		 * @param desired the desired impedance (set to 0.0 for forward calculation)
		 */
		public DiffStriplineEquation(final double h, final double w, final double t,
									 final double er, final double desired) {
			if (er <= 1.0)
				throw new IllegalArgumentException("dielectric <= 1");
			if (t <= 0.0)
				throw new IllegalArgumentException("thickness <= 0");
			if (h <= 0.0)
				throw new IllegalArgumentException("height <= 0");
			if (w <= 0.0)
				throw new IllegalArgumentException("width <= 0");
			if (desired < 0.0)
				throw new IllegalArgumentException("desired < 0");
			this.desired = desired;
			this.er = er;
			this.h = h;
			this.t = t;
			this.w = w;
			// Precompute
			final double b = 2.0 * h + t, ht = b - t;
			kmul = Math.tanh(0.5 * Math.PI * w / b);
			z0mul = 30.0 * Math.PI / Math.sqrt(er);
			z0ss = new StriplineEquation(h, t, er, 0.0).eval(w);
			// k' and ideal k
			final double kir = ECECalc.elliptic(1.0 / Math.cosh(Math.PI * w / (2 * b))) /
				ECECalc.elliptic(Math.tanh(Math.PI * w / (2 * b)));
			z0i = Units.Z_0 * Math.PI * 0.25 * kir / Math.sqrt(er);
			// Capacitance
			final double bht = b / ht;
			cfTB = 2.0 * bht * Math.log1p(bht) - t * Math.log(bht * bht - 1.0) / ht;
		}
		@Override
		public double eval(double x) {
			final double ret;
			if (x <= 0.0)
				ret = Double.NaN;
			else {
				final double b = 2.0 * h + t;
				// k
				final double ko = kmul / Math.tanh(0.5 * Math.PI * (w + x) / b);
				final double koP = Math.sqrt(1.0 - ko * ko);
				// z0
				final double z0o = z0mul * ECECalc.elliptic(koP) / ECECalc.elliptic(ko);
				// Differential impedance
				if (x / t >= 5.0)
					ret = 2.0 / (1.0 / z0ss - cfTB * (1.0 / z0o - 1.0 / z0i) / CF0);
				else
					ret = 1.0 / (1.0 / z0o - 0.5 / z0i - (0.0885 * (cfTB - CF0) / Math.PI -
						1.0 / x) / (Units.Z_0 * Math.PI));
			}
			return ret;
		}
	}
}