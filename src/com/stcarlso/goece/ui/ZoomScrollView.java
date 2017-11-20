package com.stcarlso.goece.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.ScaleAnimation;
import android.widget.ScrollView;

/**
 * A ScrollView variant which allows zooming.
 */
public class ZoomScrollView extends ScrollView implements ScaleGestureDetector.
		OnScaleGestureListener {
	/**
	 * The maximum scale for a zoom scroll view.
	 */
	private static final float MAX_SCALE = 5.0f;
	/**
	 * The minimum scale for a zoom scroll view.
	 */
	private static final float MIN_SCALE = 0.2f;

	/**
	 * Approves gestures for scaling detection.
	 */
	private final GestureDetector gestureDetector;
	/**
	 * Pinouts can be zoomed, panning is handled by a parent ScrollView.
	 */
	private float scaleFactor;
	/**
	 * Detects scaling gestures when the user pinches the screen.
	 */
	private final ScaleGestureDetector scaleDetector;

	public ZoomScrollView(Context context) {
		super(context);
		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleDetector = new ScaleGestureDetector(context, this);
		scaleFactor = 1.0f;
	}
	public ZoomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// It is duplicate code, but no other way to make the detectors final
		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleDetector = new ScaleGestureDetector(context, this);
		scaleFactor = 1.0f;
	}
	public ZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleDetector = new ScaleGestureDetector(context, this);
		scaleFactor = 1.0f;
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		super.dispatchTouchEvent(event);
		scaleDetector.onTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
	}
	@Override
	public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
		final float scale = 1.f - scaleGestureDetector.getScaleFactor();
		float newScale = scaleFactor + scale;
		// Constrain into limits
		if (newScale < MIN_SCALE)
			newScale = MIN_SCALE;
		if (newScale > MAX_SCALE)
			newScale = MAX_SCALE;
		scaleFactor = newScale;
		// Create animation
		final float invScale = 1.f / scale, invNewScale = 1.f / newScale;
		final ScaleAnimation animation = new ScaleAnimation(invScale, invNewScale, invScale,
			invNewScale, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
		animation.setDuration(0L);
		animation.setFillAfter(true);
		startAnimation(animation);
		return true;
	}
	@Override
	public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
		// Allow scaling to start
		return true;
	}
	@Override
	public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
		// onScale handles the logic
	}

	/**
	 * Allows tap-down and double-tap events (which are the standard scale events) to be passed
	 * as gestures to ScaleGestureListener.
	 */
	private static class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			return true;
		}
	}
}
