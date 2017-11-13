package org.concord.energy3d.agents;

import org.concord.energy3d.undo.MyAbstractUndoableEdit;

/**
 * @author Charles Xie
 *
 */
public interface Agent {

	public void sense(MyAbstractUndoableEdit edit);

	public void actuate();

}
