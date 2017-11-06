package org.concord.energy3d.simulation;

import java.awt.Point;

import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public abstract class ProjectCost {

	static Point windowLocation = new Point();

	/** The material and installation costs are partly based on http://www.homewyse.com, but should be considered as largely fictitious. */
	public static double getPartCost(final HousePart part) {

		// According to http://www.homewyse.com/services/cost_to_insulate_your_home.html
		// As of 2015, a 1000 square feet wall insulation will cost as high as $1500 to insulate in Boston.
		// This translates into $16/m^2. We don't know what R-value this insulation will be. But let's assume it is R13 material that has a U-value of 0.44 W/m^2/C.
		// Let's also assume that the insulation cost is inversely proportional to the U-value.
		// The baseline cost for a wall is set to be $300/m^2, close to homewyse's estimates of masonry walls, interior framing, etc.
		if (part instanceof Wall) {
			final double uFactor = ((Wall) part).getUValue();
			final double unitPrice = 300.0 + 8.0 / uFactor;
			return part.getArea() * unitPrice;
		}

		// According to http://www.homewyse.com/costs/cost_of_double_pane_windows.html
		// A storm window of about 1 m^2 costs about $500. A double-pane window of about 1 m^2 costs about $700.
		if (part instanceof Window) {
			final double uFactor = ((Window) part).getUValue();
			final double unitPrice = 500.0 + 800.0 / uFactor;
			return part.getArea() * unitPrice;
		}

		// According to http://www.homewyse.com/services/cost_to_insulate_attic.html
		// As of 2015, a 1000 square feet of attic area costs as high as $3200 to insulate in Boston.
		// This translates into $34/m^2. We don't know the R-value of this insulation. But let's assume it is R22 material that has a U-value of 0.26 W/m^2/C.
		// Let's also assume that the insulation cost is inversely proportional to the U-value.
		// The baseline (that is, the structure without insulation) cost for a roof is set to be $100/m^2.
		if (part instanceof Roof) {
			final double uFactor = ((Roof) part).getUValue();
			final double unitPrice = 100.0 + 10.0 / uFactor;
			return part.getArea() * unitPrice;
		}

		// http://www.homewyse.com/costs/cost_of_floor_insulation.html
		// As of 2015, a 1000 square feet of floor area costs as high as $3000 to insulate in Boston. This translates into $32/m^2.
		// Now, we don't know what R-value this insulation is. But let's assume it is R25 material (minimum insulation recommended
		// for zone 5 by energystar.gov) that has a U-value of 0.23 W/m^2/C.
		// Let's also assume that the insulation cost is inversely proportional to the U-value.
		// The baseline cost (that is, the structure without insulation) for floor is set to be $100/m^2.
		// The foundation cost is set to be $200/m^2.
		if (part instanceof Foundation) {
			final Foundation foundation = (Foundation) part;
			final Building b = new Building(foundation);
			if (b.isWallComplete()) {
				b.calculate();
				final double uFactor = foundation.getUValue();
				final double unitPrice = 300.0 + 8.0 / uFactor;
				return b.getArea() * unitPrice;
			}
			return -1; // the building is incomplete yet, so we can assume the floor insulation isn't there yet
		}

		if (part instanceof Floor) {
			final double area = part.getArea();
			if (area > 0) {
				return part.getArea() * 100.0;
			}
			return -1;
		}

		// According to http://www.homewyse.com/costs/cost_of_exterior_doors.html
		if (part instanceof Door) {
			final double uFactor = ((Door) part).getUValue();
			final double unitPrice = 500.0 + 100.0 / uFactor;
			return part.getArea() * unitPrice;
		}

		if (part instanceof SolarPanel) {
			return Scene.getInstance().getPvCustomPrice().getTotalCost((SolarPanel) part);
		}

		if (part instanceof Rack) {
			return Scene.getInstance().getPvCustomPrice().getTotalCost((Rack) part);
		}

		if (part instanceof Mirror) {
			return Scene.getInstance().getCspCustomPrice().getMirrorUnitPrice() * part.getArea();
		}

		if (part instanceof ParabolicTrough) {
			return Scene.getInstance().getCspCustomPrice().getParabolicTroughUnitPrice() * part.getArea();
		}

		if (part instanceof ParabolicDish) {
			return Scene.getInstance().getCspCustomPrice().getParabolicDishUnitPrice() * part.getArea();
		}

		if (part instanceof FresnelReflector) {
			return Scene.getInstance().getCspCustomPrice().getFresnelReflectorUnitPrice() * part.getArea();
		}

		if (part instanceof Tree) {
			switch (((Tree) part).getTreeType()) {
			case Tree.LINDEN:
				return 3000;
			case Tree.COTTONWOOD:
				return 2500;
			case Tree.ELM:
				return 2000;
			case Tree.OAK:
				return 2000;
			case Tree.PINE:
				return 1500;
			case Tree.MAPLE:
				return 1000;
			default:
				return 500;
			}
		}
		return 0;
	}

}