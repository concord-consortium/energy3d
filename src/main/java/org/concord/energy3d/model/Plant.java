package org.concord.energy3d.model;

/**
 * @author Charles Xie
 *
 */
public class Plant {

	private final String name;
	private final boolean evergreen;
	private final double width;
	private final double height;
	private final double cost;

	public Plant(final String name, final boolean evergreen, final double width, final double height, final double cost) {
		this.name = name;
		this.evergreen = evergreen;
		this.width = width;
		this.height = height;
		this.cost = cost;
	}

	public String getName() {
		return name;
	}

	public boolean isEvergreen() {
		return evergreen;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getCost() {
		return cost;
	}

}
