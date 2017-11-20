package com.stcarlso.goece.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import com.stcarlso.goece.R;
import com.stcarlso.goece.ui.ButtonSwapListener;
import com.stcarlso.goece.ui.ChildFragment;
import com.stcarlso.goece.ui.ValueGroup;
import com.stcarlso.goece.ui.ValueTextBox;

import java.io.UnsupportedEncodingException;

/**
 * An activity which handles floating-point, decimal, octal, and hexadecimal conversions of
 * binary values, along with ASCII conversions to boot.
 */
public class ByteConversionFragment extends ChildFragment implements View.OnClickListener {
	/**
	 * Converts "ASCII" (uses Windows-1252 if possible, otherwise the platform default) bytes
	 * into a string. Since ASCII is actually 7-bit, use an encoding with displayable characters
	 * in as many positions as possible.
	 *
	 * @param data the data to convert
	 * @return a string representation of the data
	 */
	public static String asciiBytesToString(final byte[] data) {
		String result;
		try {
			result = new String(data, "windows-1252");
		} catch (UnsupportedEncodingException e) {
			result = new String(data);
		}
		return result;
	}
	/**
	 * Reverses the bytes in the specified integer (swaps endianness).
	 *
	 * @param value the value to swap
	 * @param bytes the number of bytes to swap, starting from the least significant byte
	 * @return the integer with the bytes swapped
	 */
	public static long reverseBytes(long value, final int bytes) {
		// Error out if less than 1 byte or more than 8
		if (bytes < 1 || bytes > 8)
			throw new IllegalArgumentException("1 <= bytes <= 8");
		long newValue = 0L;
		for (int i = 0; i < bytes; i++) {
			// Grab the Nth octet
			newValue = (newValue << 8) | (value & 0xFFL);
			value >>= 8;
		}
		return newValue;
	}
	/**
	 * Sign-extends the data in value residing in the lowest significant bytes across the
	 * remaining bytes. Any data in the most significant bytes is discarded.
	 *
	 * @param value the value to extend
	 * @param bytes the number of bytes of significant data; the bit at position (bytes * 8 - 1)
	 *              will be copied across all bit positions greater than (bytes * 8)
	 * @return the value after sign extension
	 */
	public static long signExtend(long value, final int bytes) {
		// Error out if less than 1 byte or more than 8
		if (bytes < 1 || bytes > 8)
			throw new IllegalArgumentException("1 <= bytes <= 8");
		// Mask is used to exclude the old bits and include the new ones
		// -1L = 0xFFFF....FFFFL
		if (bytes < 8) {
			final long idx = 8L * (long) bytes, mask = (1L << idx) - 1L;
			long extension = 0L;
			if ((value & (1L << (idx - 1L))) != 0L)
				extension = ~mask;
			value = (value & mask) | extension;
		}
		return value;
	}
	/**
	 * Converts a string into "ASCII" (uses Windows-1252 if possible, otherwise the platform
	 * default) bytes into a string. See note on asciiBytesToString for more details.
	 *
	 * @param data the data to convert
	 * @return the byte representation of the data
	 */
	public static byte[] stringToAsciiBytes(final String data) {
		byte[] result;
		try {
			result = data.getBytes("windows-1252");
		} catch (UnsupportedEncodingException e) {
			result = data.getBytes();
		}
		return result;
	}

	/**
	 * References to the 8, 16, 32, and 64 bit radio buttons.
	 */
	private final RadioButton[] bitLength;
	/**
	 * Reference to the big endian select checkbox.
	 */
	private CheckBox endian;
	/**
	 * Suppresses events which occur during application loading to avoid a crash on resume.
	 */
	private volatile boolean loaded;
	/**
	 * Reference to the number value displayed as ASCII.
	 */
	private ValueTextBox numberAscii;
	/**
	 * Reference to the number value displayed as binary.
	 */
	private ValueTextBox numberBin;
	/**
	 * Reference to the number value displayed as decimal.
	 */
	private ValueTextBox numberDec;
	/**
	 * Reference to the number value displayed as a floating point number.
	 */
	private ValueTextBox numberFloat;
	/**
	 * Reference to the number value displayed as hexadecimal.
	 */
	private ValueTextBox numberHex;
	/**
	 * Reference to the number value displayed as octal.
	 */
	private ValueTextBox numberOct;
	/**
	 * Reference to the check box determining whether the (decimal value only!) is signed.
	 */
	private CheckBox signed;

	public ByteConversionFragment() {
		bitLength = new RadioButton[4];
		loaded = false;
	}
	/**
	 * Adds one of the output text boxes to the adjustable list for value save/restore. Since
	 * these are not EngineeringValue compatible, ValueBoxContainer cannot be used
	 *
	 * @param view the parent view
	 * @param id the ID of the text box
	 * @return the view that was added
	 */
	private ValueTextBox addTextBox(final View view, final int id) {
		final ValueTextBox box = (ValueTextBox)view.findViewById(id);
		registerAdjustable(box);
		box.addTextChangedListener(new TextWatcherListener(box));
		return box;
	}
	/**
	 * Gets the byte width selected in the UI.
	 *
	 * @return the number of bytes to use for conversions
	 */
	private int getByteCount() {
		int bytes = 1, len = bitLength.length;
		// Determine the number of bytes (and thus the number of bits)
		for (int i = 1; i < len; i++)
			if (bitLength[i].isChecked()) {
				bytes = 1 << i;
				break;
			}
		return bytes;
	}
	@Override
	protected String getTitle(Context parent) {
		return parent.getString(R.string.guiByteConv);
	}
	/**
	 * Handles numeric input into the hex, octal, binary, or decimal input boxes.
	 *
	 * @param input the text box where input occurred
	 * @param radix the radix to use for conversion
	 * @param swap whether to swap the byte order calculated
	 * @param errorID the ID of the error string to display if the input cannot be parsed
	 */
	private void handleUpdate(final ValueTextBox input, final int radix, final boolean swap,
	                          final int errorID) {
		try {
			long value = Long.parseLong(input.getText().toString(), radix);
			if (swap)
				value = reverseBytes(value, getByteCount());
			// Update the others
			updateAll(value, input.getId());
			input.setError(null);
		} catch (NumberFormatException e) {
			// Bang out the field with the matching error popup
			input.setError(getString(errorID));
		}
	}
	@Override
	protected void loadCustomPrefs(SharedPreferences prefs) {
		loadPrefsCheckBox(prefs, R.id.guiByteSign);
		loadPrefsCheckBox(prefs, R.id.guiByteSize8);
		loadPrefsCheckBox(prefs, R.id.guiByteSize16);
		loadPrefsCheckBox(prefs, R.id.guiByteSize32);
		loadPrefsCheckBox(prefs, R.id.guiByteSize64);
		loadPrefsCheckBox(prefs, R.id.guiByteEndian);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loaded = true;
	}
	@Override
	public void onClick(View view) {
		recalculate(groups.get("all"));
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.byteconv, container, false);
		// Update references
		endian = asCheckBox(view, R.id.guiByteEndian);
		signed = asCheckBox(view, R.id.guiByteSign);
		bitLength[0] = asRadioButton(view, R.id.guiByteSize8);
		bitLength[1] = asRadioButton(view, R.id.guiByteSize16);
		bitLength[2] = asRadioButton(view, R.id.guiByteSize32);
		bitLength[3] = asRadioButton(view, R.id.guiByteSize64);
		// Manually handle radio buttons since RadioGroup cannot do a grid layout
		final ButtonSwapListener listener = new ButtonSwapListener(this, bitLength);
		for (final RadioButton button : bitLength)
			button.setOnClickListener(listener);
		endian.setOnClickListener(this);
		signed.setOnClickListener(this);
		// Register input boxes
		numberBin = addTextBox(view, R.id.guiByteBin);
		numberHex = addTextBox(view, R.id.guiByteHex);
		numberFloat = addTextBox(view, R.id.guiByteFloat);
		numberOct = addTextBox(view, R.id.guiByteOct);
		numberAscii = addTextBox(view, R.id.guiByteAscii);
		numberDec = addTextBox(view, R.id.guiByteDec);
		return view;
	}
	@Override
	protected void recalculate(ValueGroup group) {
		final int id = group.mostRecentlyUsed(), bytes = getByteCount();
		final boolean bigEndian = endian.isChecked();
		long newValue = 0L;
		// Update all except most recently used
		switch (id) {
		case R.id.guiByteBin:
			// Binary
			handleUpdate(numberBin, 2, false, R.string.guiByteBadBin);
			break;
		case R.id.guiByteDec:
			// Decimal, no endian used here
			handleUpdate(numberDec, 10, !bigEndian, R.string.guiByteBadDec);
			break;
		case R.id.guiByteHex:
			// Hex
			handleUpdate(numberHex, 16, false, R.string.guiByteBadHex);
			break;
		case R.id.guiByteOct:
			// Octal
			handleUpdate(numberOct, 8, false, R.string.guiByteBadOctal);
			break;
		case R.id.guiByteAscii:
			// ASCII
			final byte[] octets = stringToAsciiBytes(numberAscii.getText().toString());
			// Calculate how many bytes will actually be converted
			int upperBound = bytes;
			if (upperBound > octets.length)
				upperBound = octets.length;
			// Only 8 bytes can be converted before overflowing
			if (upperBound > 8)
				upperBound = 8;
			for (int i = 0; i < upperBound; i++)
				newValue = (newValue << 8) | (octets[i] & 0xFFL);
			updateAll(newValue, id);
			break;
		case R.id.guiByteFloat:
			// Float
			final String fltValue = numberFloat.getText().toString();
			try {
				// Use correct representation based on byte count
				if (bytes > 4)
					newValue = Double.doubleToRawLongBits(Double.parseDouble(fltValue));
				else
					newValue = Float.floatToRawIntBits(Float.parseFloat(fltValue));
				if (!bigEndian)
					newValue = reverseBytes(newValue, bytes);
				// Unbang the field
				numberFloat.setError(null);
				updateAll(newValue, id);
			} catch (NumberFormatException e) {
				// Bang out the float field
				numberFloat.setError(getString(R.string.guiByteBadFloat));
			}
			break;
		default:
			// Error
			break;
		}
	}
	@Override
	protected void saveCustomPrefs(SharedPreferences.Editor prefs) {
		savePrefsCheckBox(prefs, R.id.guiByteSign);
		savePrefsCheckBox(prefs, R.id.guiByteSize8);
		savePrefsCheckBox(prefs, R.id.guiByteSize16);
		savePrefsCheckBox(prefs, R.id.guiByteSize32);
		savePrefsCheckBox(prefs, R.id.guiByteSize64);
		savePrefsCheckBox(prefs, R.id.guiByteEndian);
	}
	@Override
	protected void update(ValueGroup group) {
	}
	/**
	 * Updates all text boxes with the specified value, respecting the sign and magnitude
	 * settings, except for the ID specified
	 *
	 * SetTextI18n is suppressed because String.format() does not support binary
	 *
	 * @param value the value to display
	 * @param skip the ID of the field not to update, or 0 to update all
	 */
	@SuppressLint("SetTextI18n")
	private void updateAll(final long value, final int skip) {
		long maskValue = value, endValue;
		final int bytes = getByteCount();
		final boolean bigEndian = endian.isChecked();
		// Mask with the right constant
		// Android Studio wants to flag this as "integer multiplication cast to long" even
		// though the RHS cannot be more than 64...
		if (bytes < 8)
			maskValue &= (1L << (8L * (long)bytes)) - 1L;
		endValue = maskValue;
		// Swap endianness for decimal and float
		if (!bigEndian)
			endValue = reverseBytes(endValue, bytes);
		// Unbang fields as they are recomputed
		if (skip != R.id.guiByteBin) {
			numberBin.setError(null);
			numberBin.setText(Long.toString(maskValue, 2));
		}
		if (skip != R.id.guiByteAscii) {
			// Convert data to constituent bytes
			final byte[] data = new byte[bytes];
			long newValue = maskValue;
			// Value needs to be byte-swapped to make it line up
			for (int i = 0; i < bytes; i++) {
				data[bytes - i - 1] = (byte)(newValue & 0xFFL);
				newValue >>= 8;
			}
			numberAscii.setError(null);
			numberAscii.setText(asciiBytesToString(data));
		}
		if (skip != R.id.guiByteDec) {
			long sxtValue = endValue;
			// Sign-extend the value if checked
			if (signed.isChecked())
				sxtValue = signExtend(sxtValue, bytes);
			numberDec.setError(null);
			numberDec.setText(Long.toString(sxtValue, 10));
		}
		if (skip != R.id.guiByteFloat) {
			numberFloat.setError(null);
			// Use Float if converting 8/16/32, Double for 64
			if (bytes <= 4)
				numberFloat.setText(Float.toString(Float.intBitsToFloat((int)endValue)));
			else
				numberFloat.setText(Double.toString(Double.longBitsToDouble(endValue)));
		}
		if (skip != R.id.guiByteHex) {
			numberHex.setError(null);
			numberHex.setText(Long.toString(maskValue, 16).toUpperCase());
		}
		if (skip != R.id.guiByteOct) {
			numberOct.setError(null);
			numberOct.setText(Long.toString(maskValue, 8));
		}
	}

	/**
	 * A delegate class for listening and reporting changes in text fields.
	 */
	private final class TextWatcherListener implements TextWatcher {
		/**
		 * The text box which this class is watching
		 */
		private final ValueTextBox fieldFor;

		public TextWatcherListener(final ValueTextBox fieldFor) {
			if (fieldFor == null)
				throw new NullPointerException("fieldFor");
			this.fieldFor = fieldFor;
		}
		@Override
		public void afterTextChanged(Editable editable) {
			// The control which has focus is the one being edited
			if (fieldFor.hasFocus() && loaded)
				recalculate(fieldFor);
		}
		@Override
		public void beforeTextChanged(CharSequence text, int start, int count, int after) {
			// Do not recalculate before text changed
		}
		@Override
		public void onTextChanged(CharSequence text, int start, int count, int after) {
			// afterTextChanged gives more useful parameters
		}
	}
}
