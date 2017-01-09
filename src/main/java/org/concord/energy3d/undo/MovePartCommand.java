package org.concord.energy3d.undo;

import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.math.Vector3;

public class MovePartCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final HousePart part;
	private final Vector3 displacement;

	public MovePartCommand(final HousePart part, final Vector3 displacement) {
		this.part = part;
		this.displacement = displacement;
	}

	public HousePart getPart() {
		return part;
	}

	public Vector3 getDisplacement() {
		return displacement;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		move(displacement.negate(null));
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		move(displacement);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	private void move(final Vector3 v) {
		SceneManager.getInstance().setSelectedPart(part);
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if (part == null) {
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Foundation) {
							((Foundation) p).move(v, p.getGridSize());
						}
					}
				} else if (part instanceof Foundation) {
					final Foundation f = (Foundation) part;
					if (f.isGroupMaster()) {
						final List<Foundation> g = Scene.getInstance().getFoundationGroup(f);
						for (final Foundation x : g) {
							x.move(v, part.getGridSize());
						}
					} else {
						f.move(v, part.getGridSize());
					}
				} else if (part instanceof Window) {
					final Window w = (Window) part;
					w.move(v);
					w.draw();
				} else if (part instanceof Mirror) {
					final Mirror m = (Mirror) part;
					m.move(v, part.getGridSize());
					m.draw();
				} else if (part instanceof SolarPanel) {
					final SolarPanel s = (SolarPanel) part;
					s.move(v, part.getGridSize());
					s.draw();
				} else if (part instanceof Rack) {
					final Rack r = (Rack) part;
					r.move(v, part.getGridSize());
					r.draw();
				}
				return null;
			}
		});
	}

	@Override
	public String getPresentationName() {
		return "Move";
	}

}
