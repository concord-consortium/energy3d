package org.concord.energy3d.gui;

class SolarPanelNominalSize {

    private final int n = 12;
    private final String[] nominalStrings = new String[n];
    private final double[] nominalWidths = new double[n];
    private final double[] nominalHeights = new double[n];
    private final int[] cellNx = new int[n];
    private final int[] cellNy = new int[n];

    SolarPanelNominalSize() {

        // common residential size
        nominalWidths[0] = 0.99;
        nominalHeights[0] = 1.65;
        cellNx[0] = 6;
        cellNy[0] = 10;

        // common commercial size
        nominalWidths[1] = 0.99;
        nominalHeights[1] = 1.96;
        cellNx[1] = 6;
        cellNy[1] = 12;

        // SunPower E and X Series
        nominalWidths[2] = 1.05;
        nominalHeights[2] = 1.56;
        cellNx[2] = 8;
        cellNy[2] = 12;

        // ASP
        nominalWidths[3] = 1.31;
        nominalHeights[3] = 1.96;
        cellNx[3] = 8;
        cellNy[3] = 12;

        // SunPower E20 COM Series
        nominalWidths[4] = 1.07;
        nominalHeights[4] = 2.07;
        cellNx[4] = 8;
        cellNy[4] = 16;

        // First Solar Series 2, 4
        nominalWidths[5] = 0.6;
        nominalHeights[5] = 1.2;
        cellNx[5] = 10;
        cellNy[5] = 20;

        // First Solar Series 6
        nominalWidths[6] = 1.2;
        nominalHeights[6] = 2.0;
        cellNx[6] = 10;
        cellNy[6] = 20;

        // SunPower P17 Series
        nominalWidths[7] = 1.0;
        nominalHeights[7] = 2.07;
        cellNx[7] = 6;
        cellNy[7] = 12;

        // SunPower E20-245, E19-235, X20-250-BLK
        nominalWidths[8] = 0.8;
        nominalHeights[8] = 1.56;
        cellNx[8] = 6;
        cellNy[8] = 12;

        // Sharp NT-175UC1
        nominalWidths[9] = 0.83;
        nominalHeights[9] = 1.58;
        cellNx[9] = 6;
        cellNy[9] = 12;

        // YL165P-23b
        nominalWidths[10] = 0.99;
        nominalHeights[10] = 1.31;
        cellNx[10] = 6;
        cellNy[10] = 8;

        // YL205P-26b
        nominalWidths[11] = 0.99;
        nominalHeights[11] = 1.5;
        cellNx[11] = 6;
        cellNy[11] = 9;

        for (int i = 0; i < n; i++) {
            nominalStrings[i] = PopupMenuFactory.threeDecimalsFormat.format(nominalWidths[i]) + "m \u00D7 " +
                    PopupMenuFactory.threeDecimalsFormat.format(nominalHeights[i]) + "m (" + cellNx[i] + " \u00D7 " + cellNy[i] + " cells)";
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