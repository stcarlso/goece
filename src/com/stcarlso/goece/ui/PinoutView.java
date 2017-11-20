package com.stcarlso.goece.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import com.stcarlso.goece.R;
import com.stcarlso.goece.utility.Pinout;

/**
 * A custom view renderer to draw a Pinout on screen.
 */
public class PinoutView extends View {
	/**
	 * Attributes which are requested to determine the themed font size and color.
	 */
	private static final int[] ATTRS = new int[] { android.R.attr.textSize,
		android.R.attr.textColor };
	/**
	 * The minimum ratio between package height and width - the smaller one will be increased
	 * if disproportionate by more than this much.
	 */
	private static final float MIN_RATIO = 0.33f;

	/**
	 * Temporary rectangle used for drawing (Android Studio dislikes allocations in onDraw)
	 */
	private final RectF bounds;
	/**
	 * An object describing monospace font for painting the pin numbers.
	 */
	private final Paint monospace;
	/**
	 * The length of the physical pin on screen.
	 */
	private float pinLength;
	/**
	 * The margin between pin names/numbers and the border.
	 */
	private float pinMargin;
	/**
	 * The spacing between pins that are otherwise side by side.
	 */
	private float pinSpacing;
	/**
	 * The pinout currently being displayed.
	 */
	private PinoutWrapper pinoutWrapper;

	public PinoutView(Context context) {
		super(context);
		bounds = new RectF();
		monospace = new Paint();
		init(context, null);
	}
	public PinoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		bounds = new RectF();
		monospace = new Paint();
		init(context, attrs);
	}
	public PinoutView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		bounds = new RectF();
		monospace = new Paint();
		init(context, attrs);
	}
	/**
	 * Draws two rows of pin numbers and names. The numbers will fit inside the rectangle
	 * defined by the bounds member.
	 *
	 * @param canvas the canvas to draw
	 * @param count the number of pins to draw
	 * @param lower the starting pin index on the left side (0 based)
	 * @param upper the starting pin index on the right side (0 based)
	 */
	private void drawBothSides(final Canvas canvas, final int count, final int lower,
	                           final int upper) {
		final Pinout pinout = pinoutWrapper.getPinout();
		final float start = bounds.top, left = bounds.left, right = bounds.right;
		final int upperMax = upper + count;
		for (int i = 0; i < count; i++) {
			final float offset = start + (i + 1) * (pinoutWrapper.getMaxRowHeight() +
				pinSpacing);
			monospace.setTextAlign(Paint.Align.RIGHT);
			// Left name
			canvas.drawText(pinout.getPinNameAt(i + lower), left - pinLength - pinMargin,
				offset, monospace);
			// Right number
			canvas.drawText(Integer.toString(upperMax - i), right - pinMargin, offset,
				monospace);
			monospace.setTextAlign(Paint.Align.LEFT);
			// Left number
			canvas.drawText(Integer.toString(i + lower + 1), left + pinMargin, offset,
				monospace);
			// Right name
			canvas.drawText(pinout.getPinNameAt(upperMax - i - 1), right + pinMargin +
				pinLength, offset, monospace);
		}
	}
	/**
	 * Initializes the required state members for pinout rendering.
	 */
	private void init(final Context context, final AttributeSet attrs) {
		pinMargin = 1.f;
		pinSpacing = 1.f;
		pinLength = 2.f;
		monospace.setTypeface(Typeface.MONOSPACE);
		monospace.setStyle(Paint.Style.STROKE);
		if (attrs != null) {
			// Read attributes for units
			final TypedArray values = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.PinoutView, 0, 0);
			// Read the values and substitute defaults
			pinMargin = values.getDimension(R.styleable.PinoutView_pinMargin, 1.f);
			pinSpacing = values.getDimension(R.styleable.PinoutView_pinSpacing, 1.f);
			pinLength = values.getDimension(R.styleable.PinoutView_pinLength, 2.f);
			values.recycle();
		} else
			// Probably not good
			Log.w("PinoutView", "Default spacing of 1px used!");
		final TypedArray values = context.getTheme().obtainStyledAttributes(ATTRS);
		// Text size - element 0
		monospace.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
			values.getDimensionPixelSize(0, 12), getResources().getDisplayMetrics()));
		// Text color - element 1
		monospace.setColor(values.getColor(1, Color.WHITE));
		values.recycle();
		pinoutWrapper = null;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		if (pinoutWrapper != null) {
			final String shape = pinoutWrapper.getPinout().getShape();
			if (Pinout.SHAPE_DUAL.equals(shape))
				// DIP
				onDrawDIP(canvas);
			else if (Pinout.SHAPE_QUAD.equals(shape))
				// Quad
				onDrawQuad(canvas);
		}
	}
	/**
	 * Draws the current pinout as a dual in-line pinout.
	 *
	 * @param canvas the canvas to draw
	 */
	private void onDrawDIP(final Canvas canvas) {
		final int top = getPaddingTop(), left = getPaddingLeft();
		final Pinout pinout = pinoutWrapper.getPinout();
		final int count = pinout.getPinCount(), half = count >> 1;
		// Determine locations where text will be aligned
		float midSpace = 3.f * pinoutWrapper.getMaxPinWidth();
		final float lineLeft = pinoutWrapper.getMaxNameWidth() + left + pinLength + pinMargin,
			fullHeight = half * (pinoutWrapper.getMaxRowHeight() + pinSpacing);
		if (midSpace < fullHeight * MIN_RATIO)
			midSpace = fullHeight * MIN_RATIO;
		final float lineRight = lineLeft + pinLength + 2.f * pinMargin + midSpace;
		// Specify bounds of number box
		bounds.top = top;
		bounds.left = lineLeft;
		bounds.right = lineRight;
		drawBothSides(canvas, half, 0, half);
		// Outer box
		bounds.top = top;
		bounds.left = lineLeft;
		bounds.right = lineRight;
		bounds.bottom = top + fullHeight + pinSpacing;
		canvas.drawRect(bounds, monospace);
	}
	/**
	 * Draws the current pinout as a quad flat pinout.
	 *
	 * @param canvas the canvas to draw
	 */
	private void onDrawQuad(final Canvas canvas) {
		final int top = getPaddingTop(), left = getPaddingLeft();
		final Pinout pinout = pinoutWrapper.getPinout();
		final int count = pinout.getPinCount(), quart = count >> 2;
		// Determine locations where text willbe aligned
		// There is a NUMBER + MARGIN space above and below to avoid clashing at the corners
		final float lineLeft = pinoutWrapper.getMaxNameWidth() + left + pinLength + pinMargin,
			lineTop = pinoutWrapper.getMaxNameWidth() + top + pinLength + pinMargin;
		final float extraSpace = pinoutWrapper.getMaxPinWidth() + pinMargin + pinSpacing,
			fullHeight = quart * (pinoutWrapper.getMaxRowHeight() + pinSpacing) + 2.f *
			extraSpace;
		// Specify bounds of number box
		bounds.top = extraSpace + lineTop;
		bounds.left = lineLeft;
		bounds.right = lineLeft + fullHeight;
		drawBothSides(canvas, quart, 0, quart << 1);
		// Rotate once to avoid pushing/popping excessively, and draw the rest of the pins
		canvas.save();
		canvas.rotate(-90f, lineLeft + fullHeight * 0.5f, lineTop + fullHeight * 0.5f);
		bounds.top = extraSpace + lineLeft;
		bounds.left = lineTop;
		bounds.right = lineTop + fullHeight;
		drawBothSides(canvas, quart, quart, 3 * quart);
		canvas.restore();
		// Outer box
		bounds.top = lineTop;
		bounds.left = lineLeft;
		bounds.right = lineLeft + fullHeight;
		bounds.bottom = lineTop + fullHeight;
		canvas.drawRect(bounds, monospace);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Size will always include the padding
		int minw = getPaddingLeft() + getPaddingRight(), minh = getPaddingTop() +
			getPaddingBottom();
		if (pinoutWrapper != null) {
			final Pinout pinout = pinoutWrapper.getPinout();
			final String shape = pinout.getShape();
			// These have to be rounded off since the dimensions are in integer pixels
			final int pw = Math.round(pinoutWrapper.getMaxPinWidth()) + 1,
				pm = Math.round(pinMargin) + 1, ps = Math.round(pinSpacing) + 1;
			final int pn = Math.round(pinoutWrapper.getMaxNameWidth()) + 1,
				pl = Math.round(pinLength) + 1, ph = Math.round(pinoutWrapper.
				getMaxRowHeight()) + 1, count = pinout.getPinCount();
			if (Pinout.SHAPE_DUAL.equals(shape)) {
				// PADDING TEXT MARGIN PIN LINE MARGIN NUMBER*3 MARGIN LINE PIN MARGIN TEXT
				final int height = (count >> 1) * (ph + ps);
				minw += 2 * (pn + pw + 2 * pm + pl) + height;
				minh += height;
			} else if (Pinout.SHAPE_QUAD.equals(shape)) {
				// PADDING TEXT MARGIN PIN LINE MARGIN NUMBER SPACE NUMBER MARGIN LINE PIN
				// MARGIN TEXT
				final int size = (count >> 2) * (ph + ps) + 2 * (pw + pm + ps) + 2 * (pn + pw +
					pl + 2 * pm);
				minw += size;
				minh += size;
			}
		}
		// Measure and calculate new size
		int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
		int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
		setMeasuredDimension(w, h);
	}
	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		super.onSizeChanged(width, height, oldWidth, oldHeight);
		// Regenerate the wrapper
		if (pinoutWrapper != null)
			pinoutWrapper = new PinoutWrapper(pinoutWrapper.getPinout());
		invalidate();
	}
	/**
	 * Changes the pinout being displayed. Supplying null erases the pinout. In either case, the
	 * zoom is reset to the default value.
	 *
	 * @param pinout the pinout to display
	 */
	public void setPinout(final Pinout pinout) {
		pinoutWrapper = new PinoutWrapper(pinout);
		invalidate();
	}

	/**
	 * A class which wraps a Pinout and stores additional precalculated information for its
	 * display.
	 */
	private final class PinoutWrapper {
		/**
		 * The maximum number of pixels (note that onSizeChange already accounted for the SP
		 * unit change) required to render a pin number width.
		 */
		private final int maxPinWidth;
		/**
		 * The maximum number of pixels required to render a pin name width.
		 */
		private final int maxNameWidth;
		/**
		 * The maximum number of pixels required to render a name or number height.
		 */
		private final int maxRowHeight;
		/**
		 * The source pinoutWrapper wrapped by this object.
		 */
		private final Pinout pinout;

		public PinoutWrapper(final Pinout pinout) {
			final Rect textSize = new Rect();
			if (pinout == null)
				throw new NullPointerException("pinoutWrapper");
			this.pinout = pinout;
			final int count = pinout.getPinCount();
			// Calculate the width of the biggest pin number
			final String maxPin = Integer.toString(count);
			monospace.getTextBounds(maxPin, 0, maxPin.length(), textSize);
			maxPinWidth = textSize.width();
			int nameLen = 0, rowHeight = textSize.height();
			for (int i = 0; i < count; i++) {
				final String pinName = pinout.getPinNameAt(i);
				// Calculate size of this pin name
				monospace.getTextBounds(pinName, 0, pinName.length(), textSize);
				final int w = textSize.width(), h = textSize.height();
				// Find maximum dimensions of any one item
				if (w > nameLen)
					nameLen = w;
				if (h > rowHeight)
					rowHeight = h;
			}
			maxNameWidth = nameLen;
			maxRowHeight = rowHeight;
		}
		/**
		 * Retrieves the maximum pin number width.
		 *
		 * @return the number of pixels required to render a pin number (X direction)
		 */
		public int getMaxPinWidth() {
			return maxPinWidth;
		}
		/**
		 * Retrieves the maximum pin name width.
		 *
		 * @return the number of pixels required to render a pin name (X direction)
		 */
		public int getMaxNameWidth() {
			return maxNameWidth;
		}
		/**
		 * Retrieves the maximum row height.
		 *
		 * @return the number of pixels required to render a pin name and number (Y direction)
		 */
		public int getMaxRowHeight() {
			return maxRowHeight;
		}
		/**
		 * Retrieves the source pinoutWrapper.
		 *
		 * @return the pinoutWrapper
		 */
		public Pinout getPinout() {
			return pinout;
		}
		@Override
		public String toString() {
			return pinout.toString();
		}
	}
}
