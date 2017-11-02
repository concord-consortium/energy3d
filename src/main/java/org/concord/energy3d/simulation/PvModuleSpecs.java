package org.concord.energy3d.simulation;

import java.awt.Dimension;
import java.io.Serializable;

/**
 * @author Charles Xie
 *
 */
public class PvModuleSpecs implements Serializable {

	private static final long serialVersionUID = 1L;
	private String model = "Custom";
	private String brand = "Custom";
	private String cellType = "Monocrystalline"; // monocrystalline, polycrystalline, thin film
	private double cellEfficiency = 0.1833; // peak solar cell efficiency (0-1, not %)
	private double length = 1.65; // m
	private double width = 0.99; // m
	private double nominalLength = 1.65; // m (Energy3D-specific GUI thing)
	private double nominalWidth = 0.99; // m (Energy3D-specific GUI thing)
	private double thickness = 0.04; // mm
	private Dimension layout = new Dimension(10, 6); // cell layout
	private double pmax = 300; // Maximum Power Point (W)
	private double vmpp = 32.6; // Voltage at Maximum Power Point (V)
	private double impp = 9.21; // Current at Maximum Power Point (A)
	private double voc = 40.1; // Voltage at Open Circuit (V). The output Voltage of a PV under no load.
	private double isc = 9.72; // Current under short-circuit conditions (A). Used for calculating wire size and circuit protection ratings.
	private double pmaxTc = -0.0039; // Temperature Coefficient of Pmax (0-1, not % / degree C)
	private double noct = 45; // Nominal operating cell temperature (degree C)
	private double weight = 19; // kg

	@Override
	public String toString() {
		return model + ", " + cellType + ", " + cellEfficiency + ", " + length + ", " + width + ", " + thickness + ", " + layout + ", " + pmax + ", " + vmpp + ", " + impp + ", " + voc + ", " + isc + ", " + pmaxTc + ", " + noct + ", " + weight;
	}

	public PvModuleSpecs() {
		model = "Custom";
	}

	public PvModuleSpecs(final String model) {
		this.model = model;
	}

	public void setModel(final String model) {
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

	public void setCellEfficiency(final double cellEfficiency) {
		this.cellEfficiency = cellEfficiency;
	}

	public double getCelLEfficiency() {
		return cellEfficiency;
	}

	public void setLength(final double length) {
		this.length = length;
	}

	public double getLength() {
		return length;
	}

	public void setWidth(final double width) {
		this.width = width;
	}

	public double getWidth() {
		return width;
	}

	public void setThickness(final double thickness) {
		this.thickness = thickness;
	}

	public double getThickness() {
		return thickness;
	}

	public void setNominalLength(final double nominalLength) {
		this.nominalLength = nominalLength;
	}

	public double getNominalLength() {
		return nominalLength;
	}

	public void setNominalWidth(final double nominalWidth) {
		this.nominalWidth = nominalWidth;
	}

	public double getNominalWidth() {
		return nominalWidth;
	}

	public void setLayout(final int m, final int n) {
		layout = new Dimension(m, n);
	}

	public Dimension getLayout() {
		return layout;
	}

	public void setPmax(final double pmax) {
		this.pmax = pmax;
	}

	public double getPmax() {
		return pmax;
	}

	public void setVmpp(final double vmpp) {
		this.vmpp = vmpp;
	}

	public double getVmpp() {
		return vmpp;
	}

	public void setImpp(final double impp) {
		this.impp = impp;
	}

	public double getImpp() {
		return impp;
	}

	public void setVoc(final double voc) {
		this.voc = voc;
	}

	public double getVoc() {
		return voc;
	}

	public void setIsc(final double isc) {
		this.isc = isc;
	}

	public double getIsc() {
		return isc;
	}

	public void setPmaxTc(final double pmaxTc) {
		this.pmaxTc = pmaxTc;
	}

	public double getPmaxTc() {
		return pmaxTc;
	}

	public void setNoct(final double noct) {
		this.noct = noct;
	}

	public double getNoct() {
		return noct;
	}

	public void setWeight(final double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

}
