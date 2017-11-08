package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class SetShapeForAllParabolicTroughsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldLengths;
	private double[] newLengths;
	private final double[] oldApertureWidths;
	private double[] newApertureWidths;
	private final double[] oldSemilatusRecta;
	private double[] newSemilatusRecta;
	private final double[] oldModuleLengths;
	private double[] newModuleLengths;
	private final List<ParabolicTrough> troughs;

	public SetShapeForAllParabolicTroughsCommand() {
		troughs = Scene.getInstance().getAllParabolicTroughs();
		final int n = troughs.size();
		oldLengths = new double[n];
		oldApertureWidths = new double[n];
		oldSemilatusRecta = new double[n];
		oldModuleLengths = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicTrough t = troughs.get(i);
			oldLengths[i] = t.getTroughLength();
			oldApertureWidths[i] = t.getApertureWidth();
			oldSemilatusRecta[i] = t.getSemilatusRectum();
			oldModuleLengths[i] = t.getModuleLength();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = troughs.size();
		newLengths = new double[n];
		newApertureWidths = new double[n];
		newSemilatusRecta = new double[n];
		newModuleLengths = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicTrough t = troughs.get(i);
			newLengths[i] = t.getTroughLength();
			newApertureWidths[i] = t.getApertureWidth();
			newSemilatusRecta[i] = t.getSemilatusRectum();
			newModuleLengths[i] = t.getModuleLength();
			t.setTroughLength(oldLengths[i]);
			t.setApertureWidth(oldApertureWidths[i]);
			t.setSemilatusRectum(oldSemilatusRecta[i]);
			t.setModuleLength(oldModuleLengths[i]);
			t.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = troughs.size();
		for (int i = 0; i < n; i++) {
			final ParabolicTrough t = troughs.get(i);
			t.setTroughLength(newLengths[i]);
			t.setApertureWidth(newApertureWidths[i]);
			t.setSemilatusRectum(newSemilatusRecta[i]);
			t.setModuleLength(newModuleLengths[i]);
			t.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Shape for All Parabolic Troughs";
	}

}
