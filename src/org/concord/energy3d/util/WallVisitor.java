package org.concord.energy3d.util;

import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Wall;

public interface WallVisitor {
	public void visit(Wall wall, Snap snap);
}
