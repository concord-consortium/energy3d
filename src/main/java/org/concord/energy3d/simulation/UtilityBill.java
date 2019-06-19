package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 */
public class UtilityBill implements Serializable {

    private static final long serialVersionUID = 1L;
    private double[] monthlyEnergy = new double[12];

    public UtilityBill() {
        // use New England's as default, according to http://www.eia.gov/electricity/sales_revenue_price/pdf/table5_a.pdf
        for (int i = 0; i < 12; i++)
            monthlyEnergy[i] = 630;
    }

    public void setMonthlyEnergy(int month, double value) {
        monthlyEnergy[month] = value;
    }

    public double getMonthlyEnergy(int month) {
        return monthlyEnergy[month];
    }

    double[] getMonthlyEnergy() {
        return monthlyEnergy;
    }

}