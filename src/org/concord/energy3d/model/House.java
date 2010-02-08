package org.concord.energy3d.model;

import java.util.ArrayList;

public class House {
	private static House instance;
	private ArrayList<HousePart> parts = new ArrayList<HousePart>();

	public static House getInstance() {
		if (instance == null)
			instance = new House();
		return instance;
	}

	private House() {

	}

	public boolean add(HousePart e) {
		return parts.add(e);
	}

	public void clear() {
		parts.clear();
	}

	public boolean remove(Object o) {
		return parts.remove(o);
	}

	public int size() {
		return parts.size();
	}

	public ArrayList<HousePart> getParts() {
		return parts;
	}

}
