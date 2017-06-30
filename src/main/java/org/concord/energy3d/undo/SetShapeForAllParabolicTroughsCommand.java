package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class SetShapeForAllParabolicTroughsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldLengths;
	private double[] newLengths;
	private final double[] oldWidths;
	private double[] newWidths;
	private final double[] oldSemilatusRecta;
	private double[] newSemilatusRecta;
	private final double[] oldUnitLengths;
	private double[] newUnitLengths;
	private final List<ParabolicTrough> troughs;

	public SetShapeForAllParabolicTroughsCommand() {
		troughs = Scene.getInstance().getAllParabolicTroughs();
		final int n = troughs.size();
		oldLengths = new double[n];
		oldWidths = new double[n];
		oldSemilatusRecta = new double[n];
		oldUnitLengths = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicTrough t = troughs.get(i);
			oldLengths[i] = t.getTroughLength();
			oldWidths[i] = t.getTroughWidth();
			oldSemilatusRecta[i] = t.getSemilatusRectum();
			oldUnitLengths[i] = t.getUnitLength();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = troughs.size();
		newLengths = new double[n];
		newWidths = new double[n];
		newSemilatusRecta = new double[n];
		newUnitLengths = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicTrough t = troughs.get(i);
			newLengths[i] = t.getTroughLength();
			newWidths[i] = t.getTroughWidth();
			newSemilatusRecta[i] = t.getSemilatusRectum();
			newUnitLengths[i] = t.getUnitLength();
			t.setTroughLength(oldLengths[i]);
			t.setTroughWidth(oldWidths[i]);
			t.setSemilatusRectum(oldSemilatusRecta[i]);
			t.setUnitLength(oldUnitLengths[i]);
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
			t.setTroughWidth(newWidths[i]);
			t.setSemilatusRectum(newSemilatusRecta[i]);
			t.setUnitLength(newUnitLengths[i]);
			t.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Shape for All Parabolic Troughs";
	}

}
