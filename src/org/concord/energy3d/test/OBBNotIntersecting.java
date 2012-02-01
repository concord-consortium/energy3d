package org.concord.energy3d.test;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.Vector3;

public class OBBNotIntersecting {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OrientedBoundingBox b1 = new OrientedBoundingBox();
		b1.setCenter(new Vector3(0.733333358168602, 0.0, -4.352324295043945));
		b1.setExtent(new Vector3(0.583333358168602, 0.0, 0.3023243248462677));
		b1.setXAxis(new Vector3(1.0, -5.551115123125783E-17, 0.0));
		b1.setYAxis(new Vector3(-5.551115123125783E-17, 1.0, 0.0));
		b1.setZAxis(new Vector3(0.0, 0.0, 1.0));
		
		OrientedBoundingBox b2 = new OrientedBoundingBox();
		b2.setCenter(new Vector3(0.7333333581686021, 0.0, -4.352324295043945));
		b2.setExtent(new Vector3(0.583333358168602, 0.0, 0.3023243248462677));
		b2.setXAxis(new Vector3(1.0, 5.551115123125783E-17, 0.0));
		b2.setYAxis(new Vector3(5.551115123125783E-17, 1.0, 0.0));
		b2.setZAxis(new Vector3(0.0, 0.0, 1.0));
		
		System.out.println(b1.intersects(b2));
		
	}

}
