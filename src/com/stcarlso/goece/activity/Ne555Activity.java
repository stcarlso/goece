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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.AbstractEntryBox;
import com.stcarlso.goece.ui.ChildActivity;
import com.stcarlso.goece.ui.ValueGroup;

/**
 * Calculates passive component values for the requested timing intervals when building a
 * circuit with the classic 555 timer IC.
 */
public class Ne555Activity extends ChildActivity implements View.OnClickListener {
	/**
	 * ln(0.67) - ln(0.33), the multiplier used for charging the capacitor from 33% to 67%
	 */
	private static final double CHARGE_FACTOR = 0.693147180559945;
	/**
	 * The cut-off from 0 to 1 between the two different methods of duty cycle calculation.
	 */
	private static final double DUTY_THRES = 0.5;
	/**
	 * 0.0 - ln(0.33), the multiplier used for charging the capacitor from 0% to 67%
	 */
	private static final double MONO_FACTOR = 1.09861228866811;

	/**
	 * Cached reference to the monostable mode UI option.
	 */
	private RadioButton modeMonostableCtrl;
	/**
	 * Cached reference to the image of the current 555 circuit diagram.
	 */
	private ImageView pcbImageCtrl;

	/**
	 * Reports the user-entered duty cycle from 0 (0%) to 1 (100%). Sets the error message on
	 * the dialog box if necessary.
	 *
	 * @return the duty cycle from 0 to 1
	 */
	private double getDutyCycle() {
		final AbstractEntryBox<?> dutyBox = controls.get(R.id.gui555Duty);
		double duty = dutyBox.getRawValue() * 0.01;
		// Error popup if duty is invalid
		if (duty >= 1.0) {
			dutyBox.setError(getString(R.string.gui555BadDuty));
			duty = 0.99;
		} else
			dutyBox.setError(null);
		return duty;
	}
	@Override
	protected void loadCustomPrefs(SharedPreferences prefs) {
		loadPrefsCheckBox(prefs, R.id.gui555Monostable);
		loadPrefsCheckBox(prefs, R.id.gui555Astable);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ne555);
		modeMonostableCtrl = asRadioButton(R.id.gui555Monostable);
		pcbImageCtrl = asImageView(R.id.gui555Image);
		// Register value entry boxes
		controls.add(findViewById(R.id.gui555C1));
		controls.add(findViewById(R.id.gui555R1));
		controls.add(findViewById(R.id.gui555R2));
		controls.add(findViewById(R.id.gui555Delay));
		controls.add(findViewById(R.id.gui555Freq));
		controls.add(findViewById(R.id.gui555Duty));
		controls.setupAll(this);
		loadPrefs();
		updateView();
		recalculate(controls.get(R.id.gui555Freq));
	}
	@Override
	public void onClick(View source) {
		updateView();
		recalculate(controls.get(R.id.gui555Freq));
	}
	/**
	 * Recalculates the UI for the astable 555 mode.
	 *
	 * @param group the group to be calculated
	 */
	private void recalcAstable(final ValueGroup group) {
		// Raw values
		final double c = controls.getRawValue(R.id.gui555C1);
		final double freq = controls.getRawValue(R.id.gui555Freq);
		final double r1 = controls.getRawValue(R.id.gui555R1), r2 = controls.getRawValue(
			R.id.gui555R2);
		final double duty = getDutyCycle();
		switch (group.leastRecentlyUsed()) {
		case R.id.gui555C1:
			// Capacitance
			final double newC;
			if (duty > DUTY_THRES)
				// >50%
				newC = 1.0 / (freq * CHARGE_FACTOR * (r1 + 2.0 * r2));
			else
				// <50%
				newC = 1.0 / (freq * CHARGE_FACTOR * (r1 + r2));
			controls.setRawValue(R.id.gui555C1, newC);
			break;
		case R.id.gui555R1:
		case R.id.gui555R2:
			// Resistance
			recalcR1R2();
			break;
		case R.id.gui555Duty:
		case R.id.gui555Freq:
		case R.id.gui555Delay:
			// Duty cycle and frequency (need to be careful about 50% boundary)
			final double newFreq, newDuty;
			if (duty > DUTY_THRES) {
				// >50%
				newDuty = (r1 + r2) / Math.max(1.0, r1 + 2.0 * r2);
				newFreq = 1.0 / (CHARGE_FACTOR * c * (r1 + 2.0 * r2));
			} else {
				// <50%
				newDuty = r1 / Math.max(1.0, r1 + r2);
				newFreq = 1.0 / (CHARGE_FACTOR * c * (r1 + r2));
			}
			controls.setRawValue(R.id.gui555Duty, newDuty * 100.0);
			controls.setRawValue(R.id.gui555Freq, newFreq);
			// Drawing may have changed
			if ((newDuty <= DUTY_THRES && duty > DUTY_THRES) || (newDuty > DUTY_THRES &&
					duty <= DUTY_THRES))
				updateView();
			break;
		default:
			// Invalid
			break;
		}
	}
	/**
	 * Recalculates R1 and R2 if the duty cycle is updated. This can also be invoked if the
	 * capacitance is fixed and the frequency is changed.
	 */
	private void recalcR1R2() {
		// Raw values
		final double r1, r2, c = controls.getRawValue(R.id.gui555C1);
		final double freq = controls.getRawValue(R.id.gui555Freq), duty = getDutyCycle();
		if (duty > DUTY_THRES) {
			// >50%, R1 + 2*R2 can be derived from frequency and C
			final double rSum2 = 1.0 / (freq * CHARGE_FACTOR * c);
			// R1 + R2 now can be pulled from the duty
			final double rSum = duty * rSum2;
			// R2 = rSum2 - rSum, R1 = rSum - R2
			r2 = rSum2 - rSum;
			r1 = rSum - r2;
		} else {
			// <50%, R1 + R2 can be derived from frequency and C
			final double rSum = 1.0 / (freq * CHARGE_FACTOR * c);
			// R1 can be pulled from the duty
			r1 = duty * rSum;
			r2 = rSum - r1;
		}
		// These could be outside of the recommended range if C is not appropriate
		controls.setRawValue(R.id.gui555R1, r1);
		controls.setRawValue(R.id.gui555R2, r2);
	}
	@Override
	protected void recalculate(ValueGroup group) {
		if (modeMonostableCtrl.isChecked()) {
			final double r = controls.getRawValue(R.id.gui555R1);
			final double c = controls.getRawValue(R.id.gui555C1);
			final double delay = controls.getRawValue(R.id.gui555Delay);
			switch (group.leastRecentlyUsed()) {
			case R.id.gui555Delay:
			case R.id.gui555Freq:
			case R.id.gui555Duty:
				// Delay
				controls.setRawValue(R.id.gui555Delay, MONO_FACTOR * r * c);
				break;
			case R.id.gui555R1:
			case R.id.gui555R2:
				// Resistance
				controls.setRawValue(R.id.gui555R1, (c > 0.0) ? delay / (MONO_FACTOR * c) :
					Double.NaN);
				break;
			case R.id.gui555C1:
				// Capacitance
				controls.setRawValue(R.id.gui555C1, (r > 0.0) ? delay / (MONO_FACTOR * r) :
					Double.NaN);
				break;
			default:
				// Invalid
				break;
			}
		} else
			recalcAstable(group);
		showR1R2Error();
	}
	@Override
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		savePrefsCheckBox(prefs, R.id.gui555Monostable);
		savePrefsCheckBox(prefs, R.id.gui555Astable);
	}
	/**
	 * Displays the right error messages if R1 and/or R2 are out of range.
	 */
	private void showR1R2Error() {
		final AbstractEntryBox<?> r1Box = controls.get(R.id.gui555R1);
		final AbstractEntryBox<?> r2Box = controls.get(R.id.gui555R2);
		final double r1 = r1Box.getRawValue(), r2 = r2Box.getRawValue();
		final String error;
		// Check R1 and R2 values to ensure they are in the workable range
		if (modeMonostableCtrl.isChecked()) {
			error = getString(R.string.gui555BadRMono);
			r1Box.setError((Double.isInfinite(r1) || r1 < 1.0e3 || r1 > 1.0e6) ? error : null);
		} else {
			error = getString(R.string.gui555BadRAst);
			r1Box.setError((Double.isInfinite(r1) || r1 < 1.0e3 || r1 > 10.0e6) ? error : null);
			r2Box.setError((Double.isInfinite(r2) || r2 < 1.0e3 || r2 > 10.0e6) ? error : null);
		}
	}
	@Override
	protected void update(ValueGroup group) {
		if (group.mostRecentlyUsed() == R.id.gui555Duty) {
			// If duty is updated, R1/R2 must be recalculated even if the capacitance is oldest
			recalcR1R2();
			showR1R2Error();
		}
	}
	/**
	 * Updates the schematic image shown on the screen and the rest of the UI. This is slow, so
	 * should only be called when necessary.
	 */
	private void updateView() {
		final AbstractEntryBox<?> dutyCtrl = controls.get(R.id.gui555Duty);
		final double duty = dutyCtrl.getRawValue() * 0.01;
		final boolean mono = modeMonostableCtrl.isChecked();
		// Update image
		if (mono)
			pcbImageCtrl.setImageResource(R.drawable.ic555mstable);
		else
			pcbImageCtrl.setImageResource((duty > DUTY_THRES) ? R.drawable.ic555astable :
				R.drawable.ic555astable50);
		// Show/hide the entries accordingly
		controls.get(R.id.gui555R2).setEnabled(mono);
		controls.get(R.id.gui555Delay).setVisibility(mono ? View.VISIBLE : View.GONE);
		controls.get(R.id.gui555Freq).setVisibility(mono ? View.GONE : View.VISIBLE);
		// Duty has controls depending on it, so it needs to exist even if not relevant
		final ViewGroup.LayoutParams params = dutyCtrl.getLayoutParams();
		// Swap between 0 width and appropriate width so not to knock out of center
		params.width = mono ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
		dutyCtrl.setLayoutParams(params);
		dutyCtrl.setVisibility(mono ? View.INVISIBLE : View.VISIBLE);
	}
}