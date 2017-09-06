package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.SceneManager;

public class SetShapeForParabolicTroughsOnFoundationCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldLengths;
	private double[] newLengths;
	private final double[] oldWidths;
	private double[] newWidths;
	private final double[] oldSemilatusRecta;
	private double[] newSemilatusRecta;
	private final double[] oldModuleLengths;
	private double[] newModuleLengths;
	private final Foundation foundation;
	private final List<ParabolicTrough> troughs;

	public SetShapeForParabolicTroughsOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		troughs = foundation.getParabolicTroughs();
		final int n = troughs.size();
		oldLengths = new double[n];
		oldWidths = new double[n];
		oldSemilatusRecta = new double[n];
		oldModuleLengths = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicTrough t = troughs.get(i);
			oldLengths[i] = t.getTroughLength();
			oldWidths[i] = t.getApertureWidth();
			oldSemilatusRecta[i] = t.getSemilatusRectum();
			oldModuleLengths[i] = t.getModuleLength();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = troughs.size();
		newLengths = new double[n];
		newWidths = new double[n];
		newSemilatusRecta = new double[n];
		newModuleLengths = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicTrough t = troughs.get(i);
			newLengths[i] = t.getTroughLength();
			newWidths[i] = t.getApertureWidth();
			newSemilatusRecta[i] = t.getSemilatusRectum();
			newModuleLengths[i] = t.getModuleLength();
			t.setTroughLength(oldLengths[i]);
			t.setApertureWidth(oldWidths[i]);
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
			t.setApertureWidth(newWidths[i]);
			t.setSemilatusRectum(newSemilatusRecta[i]);
			t.setModuleLength(newModuleLengths[i]);
			t.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Shape for All Parabolic Troughs on Selected Foundation";
	}

}
