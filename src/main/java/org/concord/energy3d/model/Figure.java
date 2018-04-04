package org.concord.energy3d.model;

/**
 * @author Charles Xie
 *
 */
public class Figure {

	private final String name;
	private final boolean male;
	private final double width;
	private final double height;

	public Figure(final String name, final boolean male, final double width, final double height) {
		this.name = name;
		this.male = male;
		this.width = width;
		this.height = height;
	}

	public String getName() {
		return name;
	}

	public boolean isMale() {
		return male;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

}
