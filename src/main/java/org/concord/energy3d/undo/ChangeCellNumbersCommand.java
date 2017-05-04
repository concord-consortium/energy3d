package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;

public class ChangeCellNumbersCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int oldNx, newNx;
	private int oldNy, newNy;
	private final HousePart part;

	public ChangeCellNumbersCommand(final HousePart part) {
		this.part = part;
		if (part instanceof SolarPanel) {
			final SolarPanel s = (SolarPanel) part;
			oldNx = s.getNumberOfCellsInX();
			oldNy = s.getNumberOfCellsInY();
		} else if (part instanceof Rack) {
			final SolarPanel s = ((Rack) part).getSolarPanel();
			oldNx = s.getNumberOfCellsInX();
			oldNy = s.getNumberOfCellsInY();
		} else if (part instanceof Mirror) {
		}
	}

	public HousePart getPart() {
		return part;
	}

	public int[] getOldCellNumbers() {
		return new int[] { oldNx, oldNy };
	}

	public int[] getNewCellNumbers() {
		if (part instanceof SolarPanel) {
			final SolarPanel s = (SolarPanel) part;
			newNx = s.getNumberOfCellsInX();
			newNy = s.getNumberOfCellsInY();
		} else if (part instanceof Rack) {
			final SolarPanel s = ((Rack) part).getSolarPanel();
			newNx = s.getNumberOfCellsInX();
			newNy = s.getNumberOfCellsInY();
		} else if (part instanceof Mirror) {
		}
		return new int[] { newNx, newNy };
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (part instanceof SolarPanel) {
			final SolarPanel s = (SolarPanel) part;
			newNx = s.getNumberOfCellsInX();
			newNy = s.getNumberOfCellsInY();
			s.setNumberOfCellsInX(oldNx);
			s.setNumberOfCellsInY(oldNy);
		} else if (part instanceof Rack) {
			final SolarPanel s = ((Rack) part).getSolarPanel();
			newNx = s.getNumberOfCellsInX();
			newNy = s.getNumberOfCellsInY();
			s.setNumberOfCellsInX(oldNx);
			s.setNumberOfCellsInY(oldNy);
		} else if (part instanceof Mirror) {
		}
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (part instanceof SolarPanel) {
			final SolarPanel s = (SolarPanel) part;
			s.setNumberOfCellsInX(newNx);
			s.setNumberOfCellsInY(newNy);
		} else if (part instanceof Rack) {
			final SolarPanel s = ((Rack) part).getSolarPanel();
			s.setNumberOfCellsInX(newNx);
			s.setNumberOfCellsInY(newNy);
		} else if (part instanceof Mirror) {
		}
		part.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Cell Numbers";
	}

}
