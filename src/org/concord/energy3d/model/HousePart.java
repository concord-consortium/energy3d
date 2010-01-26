package org.concord.energy3d.model;

import java.util.ArrayList;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;

public abstract class HousePart {
	protected final Node root = new Node();
	protected boolean drawCompleted = false;
	protected final ArrayList<Vector3> points;
	private final int numOfDrawPoints;
	private int editPointIndex = -1;
	

	public HousePart(int n) {
		numOfDrawPoints = n;
		points = new ArrayList<Vector3>(n);
	}

	public boolean isDrawCompleted() {
		return drawCompleted;
	}

	public Node getRoot() {
		return root;
	}

	public void addPoint(Vector3 p) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");		
		if (editPointIndex == -1) {
			draw(p, points.size());
			points.add(p);
		} else
			draw(p, editPointIndex);
			
		if (points.size() >= numOfDrawPoints)
			drawCompleted = true;
	}
	
	public void editPoint(int i) {
		editPointIndex = i;
		drawCompleted = false;
	}

	public void setPreviewPoint(Vector3 p) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");
		if (editPointIndex == -1)
			draw(p, points.size());
		else 
			draw(p, editPointIndex);
	}

	protected abstract void draw(Vector3 p, int i);

}
