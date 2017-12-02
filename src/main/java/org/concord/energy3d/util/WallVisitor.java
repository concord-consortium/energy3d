package org.concord.energy3d.util;

import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Wall;

public interface WallVisitor {

	public void visit(final Wall wall, final Snap prev, final Snap next);

}
