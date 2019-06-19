package org.concord.energy3d.model;

public interface WallVisitor {

    void visit(final Wall wall, final Snap prev, final Snap next);

}