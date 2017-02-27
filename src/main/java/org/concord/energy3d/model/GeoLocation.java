package org.concord.energy3d.model;

import java.io.Serializable;

/**
 * Store the geo-location so that the model can be placed relative to others.
 * 
 * @author Charles Xie
 *
 */
public class GeoLocation implements Serializable {

	private static final long serialVersionUID = 1L;

	private double latitude;
	private double longitude;
	private int zoom;
	private String address;

	public GeoLocation(final double latitude, final double longitude) {
		setLatitude(latitude);
		setLongitude(longitude);
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setZoom(final int zoom) {
		this.zoom = zoom;
	}

	public int getZoom() {
		return zoom;
	}

}
