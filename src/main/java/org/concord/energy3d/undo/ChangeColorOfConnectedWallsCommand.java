package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.util.WallVisitor;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeColorOfConnectedWallsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final ReadOnlyColorRGBA[] oldColors;
	private ReadOnlyColorRGBA[] newColors;
	private final Wall wall;
	private final List<Wall> walls;

	public ChangeColorOfConnectedWallsCommand(final Wall wall) {
		this.wall = wall;
		walls = new ArrayList<Wall>();
		wall.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				walls.add(currentWall);
			}
		});
		final int n = walls.size();
		oldColors = new ReadOnlyColorRGBA[n];
		for (int i = 0; i < n; i++) {
			oldColors[i] = walls.get(i).getColor();
		}
	}

	public Wall getWall() {
		return wall;
	}

	public List<Wall> getWalls() {
		return walls;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = walls.size();
		newColors = new ReadOnlyColorRGBA[n];
		for (int i = 0; i < n; i++) {
			final Wall w = walls.get(i);
			newColors[i] = w.getColor();
			w.setColor(oldColors[i]);
			w.draw();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = walls.size();
		for (int i = 0; i < n; i++) {
			final HousePart w = walls.get(i);
			w.setColor(newColors[i]);
			w.draw();
		}
	}

	@Override
	public String getPresentationName() {
		return "Color Change for Connected Walls";
	}

}
