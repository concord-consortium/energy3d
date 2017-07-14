package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Window;

public class SetPartSizeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldWidth, newWidth;
	private double oldHeight, newHeight;
	private double oldModuleLength, newModuleLength;
	private final HousePart part;

	public SetPartSizeCommand(final HousePart part) {
		this.part = part;
		if (part instanceof Mirror) {
			final Mirror m = (Mirror) part;
			oldWidth = m.getMirrorWidth();
			oldHeight = m.getMirrorHeight();
		} else if (part instanceof ParabolicTrough) {
			final ParabolicTrough t = (ParabolicTrough) part;
			oldWidth = t.getApertureWidth();
			oldHeight = t.getTroughLength();
			oldModuleLength = t.getModuleLength();
		} else if (part instanceof FresnelReflector) {
			final FresnelReflector r = (FresnelReflector) part;
			oldWidth = r.getModuleWidth();
			oldHeight = r.getLength();
			oldModuleLength = r.getModuleLength();
		} else if (part instanceof Rack) {
			final Rack r = (Rack) part;
			oldWidth = r.getRackWidth();
			oldHeight = r.getRackHeight();
		} else if (part instanceof Window) {
			final Window w = (Window) part;
			oldWidth = w.getWindowWidth();
			oldHeight = w.getWindowHeight();
		}
	}

	public HousePart getPart() {
		return part;
	}

	public double getOldWidth() {
		return oldWidth;
	}

	public double getOldHeight() {
		return oldHeight;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (part instanceof Mirror) {
			final Mirror m = (Mirror) part;
			newWidth = m.getMirrorWidth();
			newHeight = m.getMirrorHeight();
			m.setMirrorWidth(oldWidth);
			m.setMirrorHeight(oldHeight);
		} else if (part instanceof ParabolicTrough) {
			final ParabolicTrough t = (ParabolicTrough) part;
			newWidth = t.getApertureWidth();
			newHeight = t.getTroughLength();
			newModuleLength = t.getModuleLength();
			t.setApertureWidth(oldWidth);
			t.setTroughLength(oldHeight);
			t.setModuleLength(oldModuleLength);
		} else if (part instanceof FresnelReflector) {
			final FresnelReflector r = (FresnelReflector) part;
			newWidth = r.getModuleWidth();
			newHeight = r.getLength();
			newModuleLength = r.getModuleLength();
			r.setModuleWidth(oldWidth);
			r.setLength(oldHeight);
			r.setModuleLength(oldModuleLength);
		} else if (part instanceof Rack) {
			final Rack r = (Rack) part;
			newWidth = r.getRackWidth();
			newHeight = r.getRackHeight();
			r.setRackWidth(oldWidth);
			r.setRackHeight(oldHeight);
		} else if (part instanceof Window) {
			final Window w = (Window) part;
			newWidth = w.getWindowWidth();
			newHeight = w.getWindowHeight();
			w.setWindowWidth(oldWidth);
			w.setWindowHeight(oldHeight);
			w.getContainer().draw();
		}
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (part instanceof Mirror) {
			final Mirror m = (Mirror) part;
			m.setMirrorWidth(newWidth);
			m.setMirrorHeight(newHeight);
		} else if (part instanceof ParabolicTrough) {
			final ParabolicTrough t = (ParabolicTrough) part;
			t.setApertureWidth(newWidth);
			t.setTroughLength(newHeight);
			t.setModuleLength(newModuleLength);
		} else if (part instanceof FresnelReflector) {
			final FresnelReflector t = (FresnelReflector) part;
			t.setModuleWidth(newWidth);
			t.setLength(newHeight);
			t.setModuleLength(newModuleLength);
		} else if (part instanceof Rack) {
			final Rack r = (Rack) part;
			r.setRackWidth(newWidth);
			r.setRackHeight(newHeight);
		} else if (part instanceof Window) {
			final Window w = (Window) part;
			w.setWindowWidth(newWidth);
			w.setWindowHeight(newHeight);
			w.getContainer().draw();
		}
		part.draw();
	}

	@Override
	public String getPresentationName() {
		if (part instanceof Mirror) {
			return "Set Size for Selected Mirror";
		}
		if (part instanceof ParabolicTrough) {
			return "Set Size for Selected Parabolic Trough";
		}
		if (part instanceof FresnelReflector) {
			return "Set Size for Selected Fresnel Reflector";
		}
		if (part instanceof Rack) {
			return "Set Size for Selected Rack";
		}
		if (part instanceof Window) {
			return "Set Size for Selected Window";
		}
		return "Set Size";
	}

}
