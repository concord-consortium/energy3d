package org.concord.energy3d.gui;

class SolarPanelNominalSize {

	private final int n = 5;
	private final String[] nominalStrings = new String[n];
	private final double[] nominalWidths = new double[n];
	private final double[] nominalHeights = new double[n];
	private final int[] cellNx = new int[n];
	private final int[] cellNy = new int[n];

	SolarPanelNominalSize() {

		nominalWidths[0] = 0.99;
		nominalHeights[0] = 1.65;
		cellNx[0] = 6;
		cellNy[0] = 10;

		nominalWidths[1] = 1.05;
		nominalHeights[1] = 1.56;
		cellNx[1] = 8;
		cellNy[1] = 12;

		nominalWidths[2] = 0.99;
		nominalHeights[2] = 1.96;
		cellNx[2] = 6;
		cellNy[2] = 12;

		nominalWidths[3] = 1.07;
		nominalHeights[3] = 2.07;
		cellNx[3] = 8;
		cellNy[3] = 16;

		nominalWidths[4] = 0.6;
		nominalHeights[4] = 1.2;
		cellNx[4] = 10;
		cellNy[4] = 20;

		for (int i = 0; i < n; i++) {
			nominalStrings[i] = PopupMenuFactory.threeDecimalsFormat.format(nominalWidths[i]) + "m \u00D7 " + PopupMenuFactory.threeDecimalsFormat.format(nominalHeights[i]) + "m (" + cellNx[i] + " \u00D7 " + cellNy[i] + " cells)";
		}

	}

	int[] getCellNx() {
		return cellNx;
	}

	int[] getCellNy() {
		return cellNy;
	}

	double[] getNominalWidths() {
		return nominalWidths;
	}

	double[] getNominalHeights() {
		return nominalHeights;
	}

	String[] getStrings() {
		return nominalStrings;
	}

}
