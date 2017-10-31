package org.concord.energy3d.simulation;

import java.awt.Dimension;

/**
 * @author Charles Xie
 *
 */
public class PvModuleSpecs {

	private final String model;
	private String brand;
	private String cellType; // monocrystalline or polycrystalline
	private float cellEfficiency; // %
	private int length; // mm
	private int width; // mm
	private int thickness; // mm
	private Dimension layout;
	private float pmax; // W
	private float vmpp; // V
	private float impp; // A
	private float voc; // V
	private float isc; // A
	private float pmaxTc; // % / degree C
	private float noct; // degree C
	private float weight; // kg

	@Override
	public String toString() {
		return brand + ", " + cellType + ", " + cellEfficiency + ", " + length + ", " + width + ", " + thickness + ", " + layout + ", " + pmax + ", " + vmpp + ", " + impp + ", " + voc + ", " + isc + ", " + pmaxTc + ", " + noct + ", " + weight;
	}

	public PvModuleSpecs(final String model) {
		this.model = model;
	}

	public String getModel() {
		return model;
	}

	public void setBrand(final String brand) {
		this.brand = brand;
	}

	public String getBrand() {
		return brand;
	}

	public void setCellType(final String cellType) {
		this.cellType = cellType;
	}

	public String getCellType() {
		return cellType;
	}

	public void setCellEfficiency(final float cellEfficiency) {
		this.cellEfficiency = cellEfficiency;
	}

	public float getCelLEfficiency() {
		return cellEfficiency;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setThickness(final int thickness) {
		this.thickness = thickness;
	}

	public int getThickness() {
		return thickness;
	}

	public void setLayout(final int m, final int n) {
		layout = new Dimension(m, n);
	}

	public Dimension getLayout() {
		return layout;
	}

	public void setPmax(final float pmax) {
		this.pmax = pmax;
	}

	public float getPmax() {
		return pmax;
	}

	public void setVmpp(final float vmpp) {
		this.vmpp = vmpp;
	}

	public float getVmpp() {
		return vmpp;
	}

	public void setImpp(final float impp) {
		this.impp = impp;
	}

	public float getImpp() {
		return impp;
	}

	public void setVoc(final float voc) {
		this.voc = voc;
	}

	public float getVoc() {
		return voc;
	}

	public void setIsc(final float isc) {
		this.isc = isc;
	}

	public float getIsc() {
		return isc;
	}

	public void setPmaxTc(final float pmaxTc) {
		this.pmaxTc = pmaxTc;
	}

	public float getPmaxTc() {
		return pmaxTc;
	}

	public void setNoct(final float noct) {
		this.noct = noct;
	}

	public float getNoct() {
		return noct;
	}

	public void setWeight(final float weight) {
		this.weight = weight;
	}

	public float getWeight() {
		return weight;
	}

}
