package com.stcarlso.goece.utility;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * A class representing an IC pinout. These are normally constructed from properties files.
 */
public class Pinout implements Iterable<String> {
	/**
	 * Renders the IC as a dual in-line package. Also applies to SOIC, SOT, DFN.
	 */
	public static final String SHAPE_DUAL = "dual";
	/**
	 * Renders the IC as a quad flat package. Also applies to QFN.
	 */
	public static final String SHAPE_QUAD = "quad";
	/**
	 * Renders the IC as a small outline transistor package. The number of pins is used to
	 * determine the shape used.
	 */
	public static final String SHAPE_SOT = "sot";
	/**
	 * Renders the IC as a TO-220 package with tab. Also applies to TO-263, TO-252.
	 */
	public static final String SHAPE_TO220 = "to220";

	/**
	 * The pinout identifier, conforming to Java identifier guidelines
	 */
	protected final String id;
	/**
	 * The pinout IC name displayed on screen, can use spaces, special characters...
	 */
	protected final String name;
	/**
	 * The pinout shape. Supported values include "quad", "dual", "to220", and "sot". If an
	 * unsupported shape is used, it will try to load the pinout as an image.
	 */
	protected String shape;
	/**
	 * Lists all pins of this device, mapping the pin number to the pin name. While it might be
	 * handy to give pins names like "TAB", the numbering functionality makes it easier to
	 * lay them out in a table, and individual renderers can special case pin numbers all they
	 * want.
	 */
	protected final List<String> pins;

	public Pinout(final String id, final String name) {
		if (id == null || id.length() < 1)
			throw new NullPointerException("id");
		this.id = id;
		if (name == null || name.length() < 1)
			this.name = id;
		else
			this.name = name;
		pins = new ArrayList<String>(64);
		shape = SHAPE_DUAL;
	}
	/**
	 * Adds the specified pin.
	 *
	 * @param pinName the pin name
	 */
	public void addPin(final String pinName) {
		if (pinName == null || pinName.length() < 1)
			throw new IllegalArgumentException("name");
		pins.add(pinName);
	}
	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof Pinout)) return false;
		final Pinout other = (Pinout)o;
		return other.getID().equals(getID());
	}
	/**
	 * Retrieves the pinout ID.
	 *
	 * @return the pinout ID
	 */
	public String getID() {
		return id;
	}
	/**
	 * Retrieves the pinout IC name as displayed on screen.
	 *
	 * @return the pinout name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Retrieves the pin name for the specified pin number. Pin numbers start at 0!
	 *
	 * @param pinNumber the pin number
	 * @return the pin name
	 */
	public String getPinNameAt(final int pinNumber) {
		if (pinNumber < 0 || pinNumber >= getPinCount())
			throw new IndexOutOfBoundsException("pin number");
		return pins.get(pinNumber);
	}
	/**
	 * Retrieves the number of pins defined.
	 *
	 * @return the pin count
	 */
	public int getPinCount() {
		return pins.size();
	}
	/**
	 * Retrieves the pinout shape.
	 *
	 * @return the pinout shape to be rendered
	 */
	public String getShape() {
		return shape;
	}
	@Override
	public int hashCode() {
		return getID().hashCode();
	}
	@Override
	public Iterator<String> iterator() {
		return pins.iterator();
	}
	/**
	 * Changes the pinout shape.
	 *
	 * @param shape the new pinout shape, must be a predefined shape or an image name
	 */
	public void setShape(final String shape) {
		if (shape == null || shape.length() < 1)
			throw new IllegalArgumentException("shape");
		this.shape = shape;
	}
	@Override
	public String toString() {
		return getName();
	}
}
